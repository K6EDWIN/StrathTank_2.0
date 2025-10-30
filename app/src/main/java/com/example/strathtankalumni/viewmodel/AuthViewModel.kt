package com.example.strathtankalumni.viewmodel
import android.content.ContentResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.strathtankalumni.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// authentication state feedback
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String, val userRole: String? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // ✅ Current logged-in user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        val firebaseUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
                _currentUser.value = doc.toObject(User::class.java)
            } catch (e: Exception) {
                _currentUser.value = null
            }
        }
    }

    fun registerUser(user: User, password: String) {
        if (user.email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty.")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    firebaseUser.sendEmailVerification().await()

                    val newUser = user.copy(userId = firebaseUser.uid)
                    firestore.collection("users").document(firebaseUser.uid).set(newUser).await()

                    auth.signOut()
                    _authState.value = AuthState.Success("Registration successful! Please verify your email.")
                } else {
                    _authState.value = AuthState.Error("User creation failed.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty.")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    if (firebaseUser.isEmailVerified) {
                        val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
                        val user = doc.toObject(User::class.java)
                        _currentUser.value = user

                        when (user?.role) {
                            "alumni" -> _authState.value = AuthState.Success("Login successful!", "alumni")
                            "admin" -> _authState.value = AuthState.Success("Admin login successful!", "admin")
                            else -> _authState.value = AuthState.Error("Role not recognized.")
                        }
                    } else {
                        auth.signOut()
                        _authState.value = AuthState.Error("Please verify your email before logging in.")
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Login failed: Invalid email or password.")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    // ✅ Fetch user data from Firestore anytime
    fun fetchCurrentUser() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                val user = doc.toObject(User::class.java)
                _currentUser.value = user
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ✅ Update user profile fields (About, Experience, Skills, LinkedIn)
    fun updateUserProfile(
        about: String,
        experience: String,
        skills: List<String>,
        linkedinUrl: String,
        onResult: (Boolean) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onResult(false)
        viewModelScope.launch {
            try {
                val docRef = firestore.collection("users").document(uid)

                val updates = mapOf(
                    "about" to about,
                    "experience" to experience,
                    "skills" to skills,
                    "linkedinUrl" to linkedinUrl
                )

                // ✅ Use set(..., merge = true) so it updates or creates automatically
                docRef.set(updates, SetOptions.merge()).await()

                // ✅ Refresh cached user data
                fetchCurrentUser()
                onResult(true)

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to update profile: ${e.message}", e)
                onResult(false)
            }
        }
    }

    // ✅ Upload profile photo and update Firestore
    fun uploadProfilePhoto(uri: Uri, contentResolver: ContentResolver, onResult: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            Log.e("AuthViewModel", "Upload failed: No authenticated user")
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                val bucketUrl = "gs://strathtankalumniapp.firebasestorage.app"
                val storage = FirebaseStorage.getInstance(bucketUrl)
                // ✅ NEW upload
                val storageRef = storage.reference
                    .child("user_photos") // Matches the rule
                    .child(user.uid)      // Matches the {userId}
                    .child("profile_photo.jpg") // Example {fileName}
                Log.d("AuthViewModel", "Starting upload to: ${storageRef.path} (uri=${uri})")
                try {
                    storageRef.putFile(uri).await()
                    Log.d("AuthViewModel", "putFile succeeded for ${storageRef.path}")
                } catch (putFileEx: Exception) {
                    // Log and try fallback
                    Log.w("AuthViewModel", "putFile failed: ${putFileEx.message}", putFileEx)

                    // Fallback: open InputStream and use putStream
                    val stream = try {
                        withContext(Dispatchers.IO) {
                            contentResolver.openInputStream(uri)
                        }
                    } catch (openEx: Exception) {
                        Log.e("AuthViewModel", "Failed to open input stream for uri: $uri", openEx)
                        null
                    }

                    if (stream == null) {
                        Log.e("AuthViewModel", "Fallback failed: cannot open input stream for uri: $uri")
                        throw putFileEx // propagate original failure to outer catch
                    } else {
                        // use stream to upload
                        try {
                            storageRef.putStream(stream).await()
                            Log.d("AuthViewModel", "putStream fallback succeeded for ${storageRef.path}")
                        } catch (putStreamEx: Exception) {
                            Log.e("AuthViewModel", "putStream also failed", putStreamEx)
                            throw putStreamEx
                        } finally {
                            try { stream.close() } catch (_: Exception) { /* ignore */ }
                        }
                    }
                }

                // Get download URL
                val downloadUrl = storageRef.downloadUrl.await().toString()
                Log.d("AuthViewModel", "Profile photo uploaded, downloadUrl=$downloadUrl")

                // Update Firestore
                firestore.collection("users").document(user.uid)
                    .update("profilePhotoUrl", downloadUrl)
                    .await()

                // Refresh cached user
                fetchCurrentUser()

                onResult(true)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to upload profile photo: ${e.message}", e)
                onResult(false)
            }
        }
    }
}

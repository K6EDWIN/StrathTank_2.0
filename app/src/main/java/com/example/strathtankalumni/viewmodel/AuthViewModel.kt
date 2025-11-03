package com.example.strathtankalumni.viewmodel

import android.content.ContentResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.strathtankalumni.data.User
import com.example.strathtankalumni.data.Project
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

//authentication state feedback
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String, val userRole: String? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}

// Project Save State for UI feedback
sealed class ProjectState {
    object Idle : ProjectState()
    object Loading : ProjectState()
    data class Success(val message: String) : ProjectState()
    data class Error(val message: String) : ProjectState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // State for tracking project creation process
    private val _projectState = MutableStateFlow<ProjectState>(ProjectState.Idle)
    val projectState: StateFlow<ProjectState> = _projectState

    // urrent logged-in user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        // Sets up a listener for Firebase Auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                // If a user is logged in, fetch their profile data
                fetchCurrentUser()
            } else {
                // If no user is logged in, clear the current user state
                _currentUser.value = null
            }
        }
    }

    // State Reset Functions

    /**
     * Resets the authentication state back to Idle.
     */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    /**
     * Resets the project submission state back to Idle.
     */
    fun resetProjectState() {
        _projectState.value = ProjectState.Idle
    }

    // Authentication Functions

    /**
     * Registers a new user with Firebase Authentication and saves their profile data to Firestore.
     */
    fun registerUser(user: User, password: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        try {
            // 1. Create user in Firebase Auth
            val result = auth.createUserWithEmailAndPassword(user.email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                // Prepare user data for Firestore, including the UID
                // The User data class has a toMap() function for easy saving.
                val userToSave = user.copy(userId = firebaseUser.uid)

                // Save user data to Firestore
                firestore.collection("users").document(firebaseUser.uid)
                    .set(userToSave.toMap(), SetOptions.merge())
                    .await()

                _authState.value = AuthState.Success("Registration successful! Please log in.")
                Log.d("AuthViewModel", "User registered and profile saved: ${firebaseUser.uid}")
            } else {
                _authState.value = AuthState.Error("Registration failed: User is null after creation.")
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Registration failed: ${e.message}", e)
            _authState.value = AuthState.Error(e.localizedMessage ?: "Registration failed.")
        }
    }

    /**
     * Signs in a user with Firebase Authentication and fetches their role from Firestore.
     */
    fun signIn(email: String, password: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        try {
            // Sign in user with Firebase Auth
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                // Fetch the user's complete profile (including their role)
                val document = firestore.collection("users").document(firebaseUser.uid).get().await()
                val user = document.toObject(User::class.java)

                val userRole = user?.role ?: "alumni" // Default to 'alumni' if role is missing

                // Update AuthState with success and role for navigation
                _authState.value = AuthState.Success("Login successful!", userRole)
                Log.d("AuthViewModel", "User logged in: ${firebaseUser.uid} with role $userRole")
            } else {
                _authState.value = AuthState.Error("Login failed: User is null after sign-in.")
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Login failed: ${e.message}", e)
            _authState.value = AuthState.Error(e.localizedMessage ?: "Login failed.")
        }
    }

    /**
     * Signs out the current user.
     */
    fun signOut() = viewModelScope.launch {
        try {
            auth.signOut()
            _currentUser.value = null
            _authState.value = AuthState.Idle
            Log.d("AuthViewModel", "User signed out successfully.")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Sign out failed: ${e.message}", e)
        }
    }


    suspend fun checkLoggedInUser(): String? = withContext(Dispatchers.IO) {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            try {
                // Fetch user document from Firestore to get the role
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                val role = userDoc.getString("role")
                Log.d("AuthViewModel", "User ${firebaseUser.uid} logged in with role: $role")
                role // Return the role (String?)
            } catch (e: Exception) {
                // Handle potential errors and sign out the user if profile fetching fails
                Log.e("AuthViewModel", "Error fetching user role for ${firebaseUser.uid}: ${e.message}", e)
                auth.signOut()
                null
            }
        } else {
            null // No user currently logged in
        }
    }

    //User Profile Functions

    /**
     * Fetches the current authenticated user's data from Firestore and updates the StateFlow.
     */
    fun fetchCurrentUser() = viewModelScope.launch(Dispatchers.IO) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _currentUser.value = null
            return@launch
        }
        try {
            val document = firestore.collection("users").document(uid).get().await()
            val user = document.toObject(User::class.java)
            _currentUser.value = user
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error fetching user data: ${e.message}")
            _currentUser.value = null
        }
    }

    /**
     * Updates the user's profile information (About, Experience, Skills, LinkedIn URL).
     */
    fun updateUserProfile(
        about: String,
        experience: String,
        skills: List<String>,
        linkedinUrl: String,
        onResult: (Boolean) -> Unit
    ) = viewModelScope.launch {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onResult(false)
            return@launch
        }

        try {
            val updates = mapOf(
                "about" to about,
                "experience" to experience,
                "skills" to skills,
                "linkedinUrl" to linkedinUrl
            )

            firestore.collection("users").document(uid)
                .update(updates)
                .await()


            fetchCurrentUser()

            onResult(true)
            Log.d("AuthViewModel", "User profile updated successfully for $uid")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Failed to update user profile: ${e.message}", e)
            onResult(false)
        }
    }
    fun uploadProfilePhoto(
        uri: Uri,
        contentResolver: ContentResolver,
        onResult: (Boolean) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false)
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val imageFileName = "profile_photos/${user.uid}/photo.jpg"
                val storageRef = storage.reference.child(imageFileName)

                var stream: java.io.InputStream? = null
                try {

                    storageRef.putFile(uri).await()
                } catch (putFileEx: Exception) {

                    Log.e("AuthViewModel", "putFile failed for uri: $uri. Trying putStream.", putFileEx)
                    stream = withContext(Dispatchers.IO) {
                        contentResolver.openInputStream(uri)
                    }
                    if (stream == null) {
                        Log.e("AuthViewModel", "Failed to open input stream for uri: $uri")
                        throw putFileEx
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
                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to upload profile photo: ${e.message}", e)
                _authState.value = AuthState.Error(e.localizedMessage ?: "Failed to upload photo.")
                onResult(false)
            }
        }
    }

    //Project Management Functions

    fun saveProject(
        projectData: Project,
        imageUri: Uri?,
        contentResolver: ContentResolver,
        onResult: (Boolean) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            _projectState.value = ProjectState.Error("User not logged in.")
            onResult(false)
            return
        }

        _projectState.value = ProjectState.Loading
        viewModelScope.launch {
            try {
                var finalImageUrl = projectData.imageUrl
                var projectToSave = projectData.copy(userId = user.uid)

                //Upload Project Image if Uri is provided
                if (imageUri != null && imageUri != Uri.EMPTY) {
                    val imageFileName = "projects/${user.uid}/${UUID.randomUUID()}"
                    val storageRef = storage.reference.child(imageFileName)

                    var stream: java.io.InputStream? = null

                    try {
                        storageRef.putFile(imageUri).await()
                    } catch (putFileEx: Exception) {
                        Log.e("AuthViewModel", "putFile failed for uri: $imageUri. Trying putStream.", putFileEx)

                        stream = withContext(Dispatchers.IO) {
                            contentResolver.openInputStream(imageUri)
                        }

                        stream?.let {
                            try {
                                storageRef.putStream(it).await()
                                Log.d("AuthViewModel", "putStream fallback succeeded for ${storageRef.path}")
                            } catch (putStreamEx: Exception) {
                                Log.e("AuthViewModel", "putStream also failed", putStreamEx)
                                throw putStreamEx // Propagate error
                            }
                        } ?: throw putFileEx
                    } finally {

                        try { stream?.close() } catch (_: Exception) { /* ignore */ }
                    }

                    // Get download URL
                    finalImageUrl = storageRef.downloadUrl.await().toString()
                    projectToSave = projectToSave.copy(imageUrl = finalImageUrl)
                }

                //Save Project Document to Firestore
                Log.d("AuthViewModel", "Attempting to save project data: $projectToSave")

                firestore.collection("projects")
                    .add(projectToSave)
                    .await()

                // Success!
                _projectState.value = ProjectState.Success("Project added successfully!")
                onResult(true)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to save project or upload image: ${e.message}", e)
                _projectState.value = ProjectState.Error(e.localizedMessage ?: "Failed to save project.")
                onResult(false)
            }

        }
    }
}
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

// ✅ ADDED Imports for Connections and Notifications
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import com.example.strathtankalumni.data.Connection
import com.example.strathtankalumni.ui.alumni.NotificationItemData
import com.example.strathtankalumni.ui.alumni.NotificationType
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine


// authentication state feedback
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String, val userRole: String? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance() // ✅ Uses 'firestore'
    private val storage = FirebaseStorage.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // ✅ Current logged-in user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // ✅ List of all alumni
    private val _alumniList = MutableStateFlow<List<User>>(emptyList())
    val alumniList: StateFlow<List<User>> = _alumniList

    // ✅ ADDED: List of user's connections
    private val _connections = MutableStateFlow<List<Connection>>(emptyList())
    val connections: StateFlow<List<Connection>> = _connections

    // ✅ ADDED: List of generated notifications
    private val _notifications = MutableStateFlow<List<NotificationItemData>>(emptyList())
    val notifications: StateFlow<List<NotificationItemData>> = _notifications


    init {
        loadCurrentUser()
        // ✅ ADDED: Start all listeners
        loadConnections()
        fetchAllAlumni()
        observeNotifications()
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

    fun fetchUserById(userId: String, onResult: (User?) -> Unit) {
        if (userId.isEmpty()) {
            onResult(null)
            return
        }

        viewModelScope.launch {
            try {
                val document = firestore.collection("users").document(userId).get().await()
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    onResult(user)
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user by ID: $userId", e)
                onResult(null)
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

    // ✅ FUNCTION TO FETCH ALL ALUMNI
    fun fetchAllAlumni() {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    // This line makes sure you don't see yourself in the list
                    .whereNotEqualTo("userId", currentUserId)
                    .get()
                    .await()

                val users = snapshot.toObjects(User::class.java)
                _alumniList.value = users

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching alumni list", e)
                _alumniList.value = emptyList() // Clear on error
            }
        }
    }

    // ✅ --- ALL NEW FUNCTIONS FOR CONNECTIONS AND NOTIFICATIONS ---

    fun loadConnections() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                firestore.collection("connections")
                    .whereArrayContains("participantIds", uid)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w("AuthViewModel", "Listen failed.", e)
                            _connections.value = emptyList()
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val connectionList = snapshot.toObjects(Connection::class.java)
                            _connections.value = connectionList
                        } else {
                            _connections.value = emptyList()
                        }
                    }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading connections", e)
            }
        }
    }

    // Helper to create a consistent document ID
    private fun getConnectionId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    fun sendConnectionRequest(otherUser: User) {
        val currentUser = _currentUser.value ?: return
        val myId = currentUser.userId
        val otherId = otherUser.userId

        val connectionId = getConnectionId(myId, otherId)

        val newConnection = Connection(
            id = connectionId, // Set the ID explicitly
            participantIds = listOf(myId, otherId),
            senderId = myId,
            status = "pending"
        )

        viewModelScope.launch {
            try {
                firestore.collection("connections")
                    .document(connectionId)
                    .set(newConnection)
                    .await()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error sending request", e)
            }
        }
    }

    fun updateConnectionStatus(connection: Connection, newStatus: String) {
        if (connection.id.isBlank()) return
        viewModelScope.launch {
            try {
                firestore.collection("connections")
                    .document(connection.id)
                    .update(
                        mapOf(
                            "status" to newStatus,
                            "lastUpdated" to FieldValue.serverTimestamp() // Use FieldValue
                        )
                    ).await()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating status", e)
            }
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            // Wait for the user to be loaded
            currentUser.collect { user ->
                if (user != null) {
                    val myId = user.userId

                    // This flow will now automatically update
                    connections.combine(alumniList) { connectionsList, allAlumni ->
                        val notificationList = mutableListOf<NotificationItemData>()

                        // --- 1. Process Connection Requests ---
                        val pendingRequests = connectionsList.filter {
                            it.status == "pending" && it.senderId != myId
                        }

                        for (request in pendingRequests) {
                            val sender = allAlumni.find { it.userId == request.senderId }
                            val senderName = sender?.let { "${it.firstName} ${it.lastName}" } ?: "An Alumnus"

                            notificationList.add(
                                NotificationItemData(
                                    id = request.id, // Use connection ID
                                    icon = Icons.Default.PersonAdd,
                                    title = "Connection request from $senderName",
                                    subtitle = "Click to view profile", // TODO: Add real timestamp
                                    type = NotificationType.CONNECTION_REQUEST,
                                    referenceId = request.senderId // Store senderId for navigation
                                )
                            )
                        }

                        // --- 2. TODO: Process New Messages ---
                        // (You can add logic here to query new messages)

                        // --- 3. TODO: Process Events ---
                        // (You can add logic here to query new events)

                        // Sort by timestamp (if we had real ones) and assign
                        _notifications.value = notificationList

                    }.collect() // Keep the flow active
                }
            }
        }
    }
}
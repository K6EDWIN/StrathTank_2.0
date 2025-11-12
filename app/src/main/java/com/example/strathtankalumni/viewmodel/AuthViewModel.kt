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
// --- MERGED IMPORTS ---
import com.example.strathtankalumni.data.ExperienceItem // Your ExperienceItem
import com.example.strathtankalumni.data.Project // Ian's Project model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import com.example.strathtankalumni.data.Connection
import com.example.strathtankalumni.ui.alumni.NotificationItemData
import com.example.strathtankalumni.ui.alumni.NotificationType
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import java.util.UUID

// --- YOUR AUTH STATE ---
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String, val userRole: String? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}

// --- IAN'S PROJECT STATES ---
sealed class ProjectState {
    object Idle : ProjectState()
    object Loading : ProjectState()
    data class Success(val message: String) : ProjectState()
    data class Error(val message: String) : ProjectState()
}

sealed class ProjectsListState {
    object Loading : ProjectsListState()
    data class Success(val projects: List<Project>) : ProjectsListState()
    data class Error(val message: String) : ProjectsListState()
}

sealed class ProjectDetailState {
    object Idle : ProjectDetailState()
    object Loading : ProjectDetailState()
    data class Success(val project: Project) : ProjectDetailState()
    data class Error(val message: String) : ProjectDetailState()
}
// --- END STATES ---


class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // --- YOUR STATEFLOWS ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _alumniList = MutableStateFlow<List<User>>(emptyList())
    val alumniList: StateFlow<List<User>> = _alumniList

    private val _connections = MutableStateFlow<List<Connection>>(emptyList())
    val connections: StateFlow<List<Connection>> = _connections

    private val _notifications = MutableStateFlow<List<NotificationItemData>>(emptyList())
    val notifications: StateFlow<List<NotificationItemData>> = _notifications

    // --- IAN'S STATEFLOWS ---
    private val _projectState = MutableStateFlow<ProjectState>(ProjectState.Idle)
    val projectState: StateFlow<ProjectState> = _projectState

    private val _allProjectsState = MutableStateFlow<ProjectsListState>(ProjectsListState.Loading)
    val allProjectsState: StateFlow<ProjectsListState> = _allProjectsState

    private val _projectDetailState = MutableStateFlow<ProjectDetailState>(ProjectDetailState.Idle)
    val projectDetailState: StateFlow<ProjectDetailState> = _projectDetailState


    init {
        loadCurrentUser()
        // ✅ ADDED: Start all listeners
        loadConnections()
        fetchAllAlumni()
        observeNotifications()
        // Note: fetchAllProjects() is called from the ProjectsScreen LaunchEffect
    }

    // --- STATE RESET HELPERS ---
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun resetProjectState() {
        _projectState.value = ProjectState.Idle
    }

    // --- YOUR AUTH/USER FUNCTIONS ---
    // (registerUser, signIn, signOut)
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

    // --- YOUR (KYLE'S) VERSION OF updateUserProfile ---
    // This is the correct one that uses List<ExperienceItem>
    fun updateUserProfile(
        about: String,
        experience: List<ExperienceItem>,
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
                    "experience" to experience, // This correctly passes the list
                    "skills" to skills,
                    "linkedinUrl" to linkedinUrl
                )

                docRef.set(updates, SetOptions.merge()).await()
                fetchCurrentUser() // Refresh cached user data
                onResult(true)

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to update profile: ${e.message}", e)
                onResult(false)
            }
        }
    }

    // --- YOUR (KYLE'S) uploadProfilePhoto FUNCTION ---
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
                val storageRef = storage.reference
                    .child("user_photos")
                    .child(user.uid)
                    .child("profile_photo.jpg")

                Log.d("AuthViewModel", "Starting upload to: ${storageRef.path} (uri=${uri})")

                // Try putFile first
                try {
                    storageRef.putFile(uri).await()
                    Log.d("AuthViewModel", "putFile succeeded for ${storageRef.path}")
                } catch (putFileEx: Exception) {
                    Log.w("AuthViewModel", "putFile failed, trying putStream fallback: ${putFileEx.message}", putFileEx)

                    // Fallback: open InputStream and use putStream
                    val stream = withContext(Dispatchers.IO) {
                        contentResolver.openInputStream(uri)
                    }
                    if (stream == null) {
                        Log.e("AuthViewModel", "Fallback failed: cannot open input stream for uri: $uri")
                        throw putFileEx // propagate original failure
                    } else {
                        stream.use { storageRef.putStream(it).await() }
                        Log.d("AuthViewModel", "putStream fallback succeeded for ${storageRef.path}")
                    }
                }

                // Get download URL
                val downloadUrl = storageRef.downloadUrl.await().toString()
                Log.d("AuthViewModel", "Profile photo uploaded, downloadUrl=$downloadUrl")

                // Update Firestore
                firestore.collection("users").document(user.uid)
                    .update("profilePhotoUrl", downloadUrl)
                    .await()

                fetchCurrentUser() // Refresh cached user
                onResult(true)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to upload profile photo: ${e.message}", e)
                onResult(false)
            }
        }
    }

    // --- YOUR (KYLE'S) ALUMNI/CONNECTION FUNCTIONS ---
    fun fetchAllAlumni() {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
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
                            "lastUpdated" to FieldValue.serverTimestamp()
                        )
                    ).await()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating status", e)
            }
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    val myId = user.userId
                    connections.combine(alumniList) { connectionsList, allAlumni ->
                        val notificationList = mutableListOf<NotificationItemData>()

                        val pendingRequests = connectionsList.filter {
                            it.status == "pending" && it.senderId != myId
                        }

                        for (request in pendingRequests) {
                            val sender = allAlumni.find { it.userId == request.senderId }
                            val senderName = sender?.let { "${it.firstName} ${it.lastName}" } ?: "An Alumnus"

                            notificationList.add(
                                NotificationItemData(
                                    id = request.id,
                                    icon = Icons.Default.PersonAdd,
                                    title = senderName, // Use name for title
                                    subtitle = "Sent you a connection request", // More descriptive
                                    type = NotificationType.CONNECTION_REQUEST,
                                    referenceId = request.senderId
                                )
                            )
                        }

                        // TODO: Add NEW_MESSAGE notification logic here

                        _notifications.value = notificationList

                    }.collect()
                }
            }
        }
    }

    // --- IAN'S PROJECT FUNCTIONS ---

    private suspend fun uploadImage(imageUri: Uri?, userId: String): String? = withContext(Dispatchers.IO) {
        if (imageUri == null) return@withContext null
        return@withContext try {
            val imageRef = storage.reference.child("projects/$userId/cover_${UUID.randomUUID()}")
            imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Project cover image upload failed: ${e.message}", e)
            null
        }
    }

    private suspend fun uploadMultipleImages(imageUris: List<Uri>, userId: String): List<String> = withContext(Dispatchers.IO) {
        val urls = mutableListOf<String>()
        try {
            imageUris.forEach { uri ->
                val imageRef = storage.reference.child("projects/$userId/media_${UUID.randomUUID()}")
                imageRef.putFile(uri).await()
                val photoUrl = imageRef.downloadUrl.await().toString()
                urls.add(photoUrl)
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Multiple project image upload failed: ${e.message}", e)
        }
        return@withContext urls
    }

    private suspend fun uploadPdf(pdfUri: Uri?, userId: String): String? = withContext(Dispatchers.IO) {
        if (pdfUri == null) return@withContext null
        return@withContext try {
            val pdfRef = storage.reference.child("projects/$userId/doc_${UUID.randomUUID()}.pdf")
            pdfRef.putFile(pdfUri).await()
            pdfRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Project PDF upload failed: ${e.message}", e)
            null
        }
    }


    fun saveProject(
        title: String,
        description: String,
        projectUrl: String,
        githubUrl: String,
        projectType: String,
        imageUri: Uri?,
        mediaImageUris: List<Uri>,
        pdfUri: Uri?,
        categories: List<String>,
        programmingLanguages: List<String>,
        databaseUsed: List<String>,
        techStack: List<String>,
        onResult: (Boolean) -> Unit
    ) {
        _projectState.value = ProjectState.Loading
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _projectState.value = ProjectState.Error("User not logged in.")
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                val imageUrl = uploadImage(imageUri, userId)
                if (imageUri != null && imageUrl == null) {
                    _projectState.value = ProjectState.Error("Failed to upload project cover image.")
                    onResult(false)
                    return@launch
                }

                val mediaImageUrls = uploadMultipleImages(mediaImageUris, userId)
                val pdfUrl = uploadPdf(pdfUri, userId)

                val newProject = Project(
                    userId = userId,
                    title = title,
                    description = description,
                    projectUrl = projectUrl,
                    githubUrl = githubUrl,
                    projectType = projectType,
                    imageUrl = imageUrl ?: "",
                    categories = categories,
                    programmingLanguages = programmingLanguages,
                    databaseUsed = databaseUsed,
                    techStack = techStack,
                    mediaImageUrls = mediaImageUrls,
                    pdfUrl = pdfUrl ?: ""
                    // createdAt will be set by @ServerTimestamp
                )

                firestore.collection("projects").add(newProject).await()
                _projectState.value = ProjectState.Success("Project saved successfully!")
                onResult(true)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to save project or upload media: ${e.message}", e)
                _projectState.value = ProjectState.Error(e.localizedMessage ?: "Failed to save project.")
                onResult(false)
            }
        }
    }

    fun fetchAllProjects() {
        viewModelScope.launch {
            _allProjectsState.value = ProjectsListState.Loading
            try {
                val snapshot = firestore.collection("projects")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val projects = snapshot.documents.mapNotNull { document ->
                    document.toObject(Project::class.java)?.copy(id = document.id)
                }
                _allProjectsState.value = ProjectsListState.Success(projects)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching projects: ${e.message}", e)
                _allProjectsState.value = ProjectsListState.Error(e.localizedMessage ?: "Failed to fetch projects.")
            }
        }
    }

    fun fetchProjectById(projectId: String) {
        viewModelScope.launch {
            _projectDetailState.value = ProjectDetailState.Loading
            try {
                val document = firestore.collection("projects").document(projectId).get().await()
                val project = document.toObject(Project::class.java)?.copy(id = document.id)

                if (project != null) {
                    _projectDetailState.value = ProjectDetailState.Success(project)
                } else {
                    _projectDetailState.value = ProjectDetailState.Error("Project not found.")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching project by ID: ${e.message}", e)
                _projectDetailState.value = ProjectDetailState.Error(e.localizedMessage ?: "Failed to fetch project details.")
            }
        }
    }
}
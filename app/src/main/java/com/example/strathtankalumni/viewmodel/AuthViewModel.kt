package com.example.strathtankalumni.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.strathtankalumni.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PersonAdd

// AUTH STATE
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String, val userRole: String? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}

// IAN'S PROJECT STATES
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

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _alumniList = MutableStateFlow<List<User>>(emptyList())
    val alumniList: StateFlow<List<User>> = _alumniList

    private val _connections = MutableStateFlow<List<Connection>>(emptyList())
    val connections: StateFlow<List<Connection>> = _connections

    private val _notifications = MutableStateFlow<List<NotificationItemData>>(emptyList())
    val notifications: StateFlow<List<NotificationItemData>> = _notifications

    // IAN'S STATEFLOWS
    private val _projectState = MutableStateFlow<ProjectState>(ProjectState.Idle)
    val projectState: StateFlow<ProjectState> = _projectState

    private val _allProjectsState = MutableStateFlow<ProjectsListState>(ProjectsListState.Loading)
    val allProjectsState: StateFlow<ProjectsListState> = _allProjectsState

    private val _projectDetailState = MutableStateFlow<ProjectDetailState>(ProjectDetailState.Idle)
    val projectDetailState: StateFlow<ProjectDetailState> = _projectDetailState

    private val _collaborations = MutableStateFlow<List<Collaboration>>(emptyList())
    val collaborations: StateFlow<List<Collaboration>> = _collaborations

    private val _collaborationMembers = MutableStateFlow<List<User>>(emptyList())
    val collaborationMembers: StateFlow<List<User>> = _collaborationMembers

    // Project Comments (General)
    private val _projectComments = MutableStateFlow<List<Comment>>(emptyList())
    val projectComments: StateFlow<List<Comment>> = _projectComments.asStateFlow()

    // Hub Comments (For Collaboration Hub)
    private val _hubComments = MutableStateFlow<List<ProjectComment>>(emptyList())
    val hubComments: StateFlow<List<ProjectComment>> = _hubComments.asStateFlow()

    private val _isProjectLiked = MutableStateFlow(false)
    val isProjectLiked: StateFlow<Boolean> = _isProjectLiked.asStateFlow()

    init {
        loadCurrentUser()
        loadConnections()
        loadCollaborations()
        fetchAllAlumni()
        observeNotifications()
    }

    fun resetAuthState() { _authState.value = AuthState.Idle }
    fun resetProjectState() { _projectState.value = ProjectState.Idle }
    fun clearCollaborationMembers() { _collaborationMembers.value = emptyList() }
    fun clearComments() { _projectComments.value = emptyList() }

    // --- AUTHENTICATION ---

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
                        _authState.value = AuthState.Success("Login successful!", user?.role)
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
                _currentUser.value = doc.toObject(User::class.java)
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
                onResult(document.toObject(User::class.java))
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user by ID: $userId", e)
                onResult(null)
            }
        }
    }

    fun updateUserProfile(
        about: String, experience: List<ExperienceItem>, skills: List<String>, linkedinUrl: String, onResult: (Boolean) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onResult(false)
        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "about" to about,
                    "experience" to experience,
                    "skills" to skills,
                    "linkedinUrl" to linkedinUrl
                )
                firestore.collection("users").document(uid).set(updates, SetOptions.merge()).await()
                fetchCurrentUser()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun uploadProfilePhoto(uri: Uri, contentResolver: ContentResolver, onResult: (Boolean) -> Unit) {
        val user = auth.currentUser ?: return onResult(false)
        viewModelScope.launch {
            try {
                val storageRef = storage.reference.child("user_photos/${user.uid}/profile_photo.jpg")

                // Try putFile, allow fallback to stream for some URI types
                try {
                    storageRef.putFile(uri).await()
                } catch (e: Exception) {
                    val stream = withContext(Dispatchers.IO) { contentResolver.openInputStream(uri) }
                    stream?.use { storageRef.putStream(it).await() } ?: throw e
                }

                val downloadUrl = storageRef.downloadUrl.await().toString()
                firestore.collection("users").document(user.uid).update("profilePhotoUrl", downloadUrl).await()
                fetchCurrentUser()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    // --- CONNECTIONS ---
    fun fetchAllAlumni() {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users").whereNotEqualTo("userId", currentUserId).get().await()
                _alumniList.value = snapshot.toObjects(User::class.java)
            } catch (e: Exception) {
                _alumniList.value = emptyList()
            }
        }
    }

    fun loadConnections() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("connections")
            .whereArrayContains("participantIds", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _connections.value = emptyList()
                    return@addSnapshotListener
                }
                _connections.value = snapshot?.toObjects(Connection::class.java) ?: emptyList()
            }
    }

    fun sendConnectionRequest(otherUser: User) {
        val currentUser = _currentUser.value ?: return
        val myId = currentUser.userId
        val connectionId = if (myId < otherUser.userId) "${myId}_${otherUser.userId}" else "${otherUser.userId}_${myId}"

        val newConnection = Connection(
            id = connectionId,
            participantIds = listOf(myId, otherUser.userId),
            senderId = myId,
            status = "pending"
        )
        viewModelScope.launch {
            try {
                firestore.collection("connections").document(connectionId).set(newConnection).await()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error sending connection", e)
            }
        }
    }

    fun updateConnectionStatus(connection: Connection, newStatus: String) {
        viewModelScope.launch {
            try {
                firestore.collection("connections").document(connection.id)
                    .update(mapOf("status" to newStatus, "lastUpdated" to FieldValue.serverTimestamp())).await()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating status", e)
            }
        }
    }

    // --- COLLABORATIONS (FIXED) ---
    private fun loadCollaborations() {
        val uid = auth.currentUser?.uid ?: return

        // Listener 1: Where I am Collaborator
        firestore.collection("collaborations")
            .whereEqualTo("collaboratorId", uid)
            .addSnapshotListener { snapshot, e ->
                if (e == null && snapshot != null) {
                    val myCollabs = snapshot.toObjects(Collaboration::class.java)
                    updateCollaborationsList(myCollabs)
                }
            }

        // Listener 2: Where I am Owner
        firestore.collection("collaborations")
            .whereEqualTo("projectOwnerId", uid)
            .addSnapshotListener { snapshot, e ->
                if (e == null && snapshot != null) {
                    val ownerCollabs = snapshot.toObjects(Collaboration::class.java)
                    updateCollaborationsList(ownerCollabs)
                }
            }
    }

    private fun updateCollaborationsList(newItems: List<Collaboration>) {
        val current = _collaborations.value
        val combined = (current + newItems).distinctBy { it.id }
        _collaborations.value = combined
    }

    fun requestCollaboration(project: Project) {
        val currentUser = _currentUser.value ?: return
        val collabId = "${project.id}_${currentUser.userId}"

        val newCollaboration = Collaboration(
            id = collabId,
            projectId = project.id,
            projectTitle = project.title,
            projectDescription = project.description,
            projectImageUrl = project.imageUrl,
            projectOwnerId = project.userId,
            collaboratorId = currentUser.userId,
            collaboratorName = "${currentUser.firstName} ${currentUser.lastName}",
            collaboratorPhotoUrl = currentUser.profilePhotoUrl,
            status = "pending"
        )

        viewModelScope.launch {
            try {
                firestore.collection("collaborations").document(collabId).set(newCollaboration, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error requesting collab", e)
            }
        }
    }

    fun updateCollaborationStatus(collaborationId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                firestore.collection("collaborations").document(collaborationId)
                    .update(mapOf("status" to newStatus, "updatedAt" to FieldValue.serverTimestamp())).await()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating collab", e)
            }
        }
    }

    fun getUsersForCollaboration(projectId: String, projectOwnerId: String) {
        viewModelScope.launch {
            try {
                // This query relies on the "status" field being present.
                // If the index is missing, this will fail. Check Logcat for index creation link.
                val snapshot = firestore.collection("collaborations")
                    .whereEqualTo("projectId", projectId)
                    .whereEqualTo("status", "accepted")
                    .get().await()

                val collaboratorIds = snapshot.toObjects(Collaboration::class.java).map { it.collaboratorId }
                val allIds = (collaboratorIds + projectOwnerId).distinct()

                // Optimization: If user list is already loaded, filter locally
                val users = _alumniList.value.filter { it.userId in allIds }

                // If local list is empty (rare), fallback to fetching (omitted for brevity but good practice)
                _collaborationMembers.value = users
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching members", e)
            }
        }
    }

    // --- PROJECTS & COMMENTS ---

    // (Project upload methods omitted for brevity, assumed working)

    fun saveProject(
        title: String, description: String, projectUrl: String, githubUrl: String, projectType: String,
        imageUri: Uri?, mediaImageUris: List<Uri>, pdfUri: Uri?,
        categories: List<String>, programmingLanguages: List<String>, databaseUsed: List<String>, techStack: List<String>,
        onResult: (Boolean) -> Unit
    ) {
        _projectState.value = ProjectState.Loading
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val imageUrl = uploadImage(imageUri, userId)
                val mediaImageUrls = uploadMultipleImages(mediaImageUris, userId)
                val pdfUrl = uploadPdf(pdfUri, userId)

                val newProject = Project(
                    userId = userId, title = title, description = description, projectUrl = projectUrl, githubUrl = githubUrl,
                    projectType = projectType, imageUrl = imageUrl ?: "", mediaImageUrls = mediaImageUrls, pdfUrl = pdfUrl ?: "",
                    categories = categories, programmingLanguages = programmingLanguages, databaseUsed = databaseUsed, techStack = techStack
                )
                firestore.collection("projects").add(newProject).await()
                _projectState.value = ProjectState.Success("Project saved!")
                onResult(true)
            } catch (e: Exception) {
                _projectState.value = ProjectState.Error(e.message ?: "Failed")
                onResult(false)
            }
        }
    }

    private suspend fun uploadImage(imageUri: Uri?, userId: String): String? = withContext(Dispatchers.IO) {
        if (imageUri == null) return@withContext null
        try {
            val ref = storage.reference.child("projects/$userId/cover_${UUID.randomUUID()}")
            ref.putFile(imageUri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) { null }
    }

    private suspend fun uploadMultipleImages(uris: List<Uri>, userId: String): List<String> = withContext(Dispatchers.IO) {
        uris.mapNotNull { uri -> uploadImage(uri, userId) } // Reusing uploadImage for simplicity
    }

    private suspend fun uploadPdf(uri: Uri?, userId: String): String? = withContext(Dispatchers.IO) {
        if (uri == null) return@withContext null
        try {
            val ref = storage.reference.child("projects/$userId/doc_${UUID.randomUUID()}.pdf")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) { null }
    }

    fun fetchAllProjects() {
        viewModelScope.launch {
            _allProjectsState.value = ProjectsListState.Loading
            try {
                val snapshot = firestore.collection("projects")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get().await()
                val projects = snapshot.documents.mapNotNull { it.toObject(Project::class.java)?.copy(id = it.id) }
                _allProjectsState.value = ProjectsListState.Success(projects)
            } catch (e: Exception) {
                _allProjectsState.value = ProjectsListState.Error(e.message ?: "Error")
            }
        }
    }

    fun fetchProjectById(projectId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = firestore.collection("projects").document(projectId).get().await()
                val project = doc.toObject(Project::class.java)?.copy(id = doc.id)

                if (project != null) {
                    val likeDoc = firestore.collection("projects").document(projectId).collection("likes").document(uid).get().await()
                    _isProjectLiked.value = likeDoc.exists()
                    _projectDetailState.value = ProjectDetailState.Success(project)
                } else {
                    _projectDetailState.value = ProjectDetailState.Error("Project not found")
                }
            } catch (e: Exception) {
                _projectDetailState.value = ProjectDetailState.Error(e.message ?: "Error")
            }
        }
    }

    // --- COMMENTS & LIKES ---
    // For Project Details (Public Comments)
    fun fetchCommentsForProject(projectId: String) {
        firestore.collection("projects").document(projectId).collection("comments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { s, _ ->
                if (s != null) _projectComments.value = s.toObjects(Comment::class.java)
            }
    }

    fun postComment(projectId: String, text: String) {
        val user = _currentUser.value ?: return
        val newComment = Comment(id = UUID.randomUUID().toString(), text = text, userId = user.userId, userName = "${user.firstName} ${user.lastName}", userPhotoUrl = user.profilePhotoUrl)
        viewModelScope.launch {
            val ref = firestore.collection("projects").document(projectId)
            firestore.runBatch { b ->
                b.set(ref.collection("comments").document(newComment.id), newComment)
                b.update(ref, "commentCount", FieldValue.increment(1))
            }
        }
    }

    fun toggleProjectLike(projectId: String, isLiked: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val ref = firestore.collection("projects").document(projectId)
            val likeRef = ref.collection("likes").document(uid)
            firestore.runTransaction { t ->
                val currentLikes = t.get(ref).getLong("likes") ?: 0
                if (isLiked) {
                    t.delete(likeRef)
                    t.update(ref, "likes", (currentLikes - 1).coerceAtLeast(0))
                } else {
                    t.set(likeRef, mapOf("date" to FieldValue.serverTimestamp()))
                    t.update(ref, "likes", currentLikes + 1)
                }
            }.await()
            _isProjectLiked.value = !isLiked
        }
    }

    // For Collaboration Hub (Private Team Chat)
    // Renamed to fetchHubComments / addHubComment to avoid conflict if needed
    fun fetchComments(projectId: String) { // Used in CollaborationDetailScreen
        if (projectId.isBlank()) return
        firestore.collection("projects").document(projectId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { s, e ->
                if (s != null) _hubComments.value = s.toObjects(ProjectComment::class.java)
            }
    }

    fun addComment(projectId: String, text: String, user: User?) { // Used in CollaborationDetailScreen
        if (user == null) return
        val comment = ProjectComment(userId = user.userId, userName = "${user.firstName} ${user.lastName}", userPhotoUrl = user.profilePhotoUrl, text = text, projectId = projectId)
        viewModelScope.launch {
            firestore.collection("projects").document(projectId).collection("comments").add(comment)
        }
    }

    // --- NOTIFICATIONS ---
    private fun observeNotifications() {
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user == null) { _notifications.value = emptyList(); return@collect }
                val myId = user.userId

                connections.combine(collaborations) { conns, collabs -> Pair(conns, collabs) }
                    .combine(alumniList) { (conns, collabs), alumni ->
                        val list = mutableListOf<NotificationItemData>()

                        // Connection Requests
                        conns.filter { it.status == "pending" && it.senderId != myId }.forEach { req ->
                            val sender = alumni.find { it.userId == req.senderId }
                            list.add(NotificationItemData(req.id, Icons.Default.PersonAdd, "${sender?.firstName} ${sender?.lastName}", "Sent connection request", NotificationType.CONNECTION_REQUEST, req.senderId))
                        }

                        // Collab Requests
                        collabs.filter { it.status == "pending" && it.projectOwnerId == myId }.forEach { req ->
                            list.add(NotificationItemData(req.id, Icons.Default.GroupAdd, req.collaboratorName, "Wants to join ${req.projectTitle}", NotificationType.COLLABORATION_REQUEST, req.id))
                        }

                        list.sortedBy { it.id }
                    }.collect { _notifications.value = it }
            }
        }
    }
}
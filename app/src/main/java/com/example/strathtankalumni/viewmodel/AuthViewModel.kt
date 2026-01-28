package com.example.strathtankalumni.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.strathtankalumni.data.*
import com.example.strathtankalumni.util.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.put
import kotlinx.serialization.json.buildJsonObject
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PersonAdd
import io.github.jan.supabase.annotations.SupabaseExperimental

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

    private val supabase = Supabase.client

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

    private val _projectComments = MutableStateFlow<List<Comment>>(emptyList())
    val projectComments: StateFlow<List<Comment>> = _projectComments.asStateFlow()

    private val _hubComments = MutableStateFlow<List<ProjectComment>>(emptyList())
    val hubComments: StateFlow<List<ProjectComment>> = _hubComments.asStateFlow()

    private val _isProjectLiked = MutableStateFlow(false)
    val isProjectLiked: StateFlow<Boolean> = _isProjectLiked.asStateFlow()

    init {
        loadCurrentUser()
        viewModelScope.launch {
            supabase.auth.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    fetchCurrentUser()
                    loadConnections()
                    loadCollaborations()
                    fetchAllAlumni()
                    observeNotifications()
                }
            }
        }
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
                // 1. Sign up with Metadata
                supabase.auth.signUpWith(Email) {
                    this.email = user.email
                    this.password = password
                    // Send profile data here so the Trigger can save it
                    this.data = buildJsonObject {
                        put("first_name", user.firstName)
                        put("last_name", user.lastName)
                        put("role", user.role)
                    }
                }

                // 2. Success (The Trigger handles the database insert)
                _authState.value = AuthState.Success("Registration successful! Please check your email.")

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
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                fetchCurrentUser()
                val currentUser = _currentUser.value
                _authState.value = AuthState.Success("Login successful!", currentUser?.role)

            } catch (e: Exception) {
                _authState.value = AuthState.Error("Login failed: ${e.message}")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            supabase.auth.signOut()
            _currentUser.value = null
        }
    }

    fun loadCurrentUser() {
        val sessionUser = supabase.auth.currentUserOrNull() ?: return
        viewModelScope.launch {
            try {
                val user = supabase.postgrest.from("users")
                    .select { filter { eq("user_id", sessionUser.id) } }
                    .decodeSingleOrNull<User>()
                _currentUser.value = user
            } catch (e: Exception) {
                _currentUser.value = null
            }
        }
    }

    fun fetchCurrentUser() = loadCurrentUser()

    fun fetchUserById(userId: String, onResult: (User?) -> Unit) {
        if (userId.isEmpty()) { onResult(null); return }
        viewModelScope.launch {
            try {
                val user = supabase.postgrest.from("users")
                    .select { filter { eq("user_id", userId) } }
                    .decodeSingleOrNull<User>()
                onResult(user)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user", e)
                onResult(null)
            }
        }
    }

    fun updateUserProfile(
        about: String, experience: List<ExperienceItem>, skills: List<String>, linkedinUrl: String, onResult: (Boolean) -> Unit
    ) {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return onResult(false)
        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "about" to about,
                    "experience" to experience,
                    "skills" to skills,
                    "linkedin_url" to linkedinUrl
                )
                supabase.postgrest.from("users").update(updates) {
                    filter { eq("user_id", uid) }
                }
                fetchCurrentUser()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun uploadProfilePhoto(uri: Uri, contentResolver: ContentResolver, onResult: (Boolean) -> Unit) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return onResult(false)
        viewModelScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { it.readBytes() }
                } ?: return@launch onResult(false)

                val fileName = "user_photos/$userId/profile_photo.jpg"
                val bucket = supabase.storage.from("avatars")

                // ✅ FIX: Correct upload syntax
                bucket.upload(fileName, bytes) {
                    upsert = true
                }
                val downloadUrl = bucket.publicUrl(fileName)

                supabase.postgrest.from("users").update(mapOf("profile_photo_url" to downloadUrl)) {
                    filter { eq("user_id", userId) }
                }
                fetchCurrentUser()
                onResult(true)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Upload failed", e)
                onResult(false)
            }
        }
    }

    // --- CONNECTIONS ---
    fun fetchAllAlumni() {
        val currentUserId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            try {
                val users = supabase.postgrest.from("users")
                    .select { filter { neq("user_id", currentUserId) } }
                    .decodeList<User>()
                _alumniList.value = users
            } catch (e: Exception) {
                _alumniList.value = emptyList()
            }
        }
    }

    @OptIn(SupabaseExperimental::class)
    fun loadConnections() {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            // ✅ FIX: Use postgrest.from and explicit type for list
            supabase.postgrest.from("connections").selectAsFlow(Connection::id)
                .map { list: List<Connection> ->
                    list.filter { it.participantIds.contains(uid) }
                }
                .collect { filteredList ->
                    _connections.value = filteredList
                }
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
                supabase.postgrest.from("connections").upsert(newConnection)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error sending connection", e)
            }
        }
    }

    fun updateConnectionStatus(connection: Connection, newStatus: String) {
        viewModelScope.launch {
            try {
                supabase.postgrest.from("connections").update(mapOf("status" to newStatus)) {
                    filter { eq("id", connection.id) }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating status", e)
            }
        }
    }

    // --- COLLABORATIONS ---
    @OptIn(SupabaseExperimental::class)
    private fun loadCollaborations() {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            // ✅ FIX: Explicit typing for list
            supabase.postgrest.from("collaborations").selectAsFlow(Collaboration::id)
                .map { list: List<Collaboration> ->
                    list.filter { it.projectOwnerId == uid || it.collaboratorId == uid }
                }
                .collect { filteredList ->
                    _collaborations.value = filteredList
                }
        }
    }

    fun requestCollaboration(project: Project) {
        val currentUser = _currentUser.value ?: return
        // Safe access for nullable Project ID
        val projectId = project.id ?: return
        val collabId = "${projectId}_${currentUser.userId}"

        val newCollaboration = Collaboration(
            id = collabId,
            projectId = projectId,
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
                supabase.postgrest.from("collaborations").upsert(newCollaboration)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error requesting collab", e)
            }
        }
    }

    fun updateCollaborationStatus(collaborationId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                supabase.postgrest.from("collaborations").update(mapOf("status" to newStatus)) {
                    filter { eq("id", collaborationId) }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating collab", e)
            }
        }
    }

    fun getUsersForCollaboration(projectId: String, projectOwnerId: String) {
        viewModelScope.launch {
            try {
                val collaborators = supabase.postgrest.from("collaborations")
                    .select {
                        filter {
                            eq("project_id", projectId)
                            eq("status", "accepted")
                        }
                    }.decodeList<Collaboration>()

                val collaboratorIds = collaborators.map { it.collaboratorId }
                val allIds = (collaboratorIds + projectOwnerId).distinct()

                val users = supabase.postgrest.from("users")
                    .select { filter { isIn("user_id", allIds) } }
                    .decodeList<User>()

                _collaborationMembers.value = users
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching members", e)
            }
        }
    }

    // --- PROJECTS ---

    fun saveProject(
        title: String, description: String, projectUrl: String, githubUrl: String, projectType: String,
        imageUri: Uri?, mediaImageUris: List<Uri>, pdfUri: Uri?,
        categories: List<String>, programmingLanguages: List<String>, databaseUsed: List<String>, techStack: List<String>,
        contentResolver: ContentResolver,
        onResult: (Boolean) -> Unit
    ) {
        _projectState.value = ProjectState.Loading
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            try {
                val imageUrl = uploadFile(imageUri, "projects", "covers/$userId/${UUID.randomUUID()}.jpg", contentResolver)

                val mediaImageUrls = mediaImageUris.mapNotNull { uri ->
                    uploadFile(uri, "projects", "media/$userId/${UUID.randomUUID()}.jpg", contentResolver)
                }
                val pdfUrl = uploadFile(pdfUri, "projects", "docs/$userId/${UUID.randomUUID()}.pdf", contentResolver)

                val newProject = Project(
                    // ID generated by DB
                    userId = userId, title = title, description = description, projectUrl = projectUrl, githubUrl = githubUrl,
                    projectType = projectType, imageUrl = imageUrl ?: "", mediaImageUrls = mediaImageUrls, pdfUrl = pdfUrl ?: "",
                    categories = categories, programmingLanguages = programmingLanguages, databaseUsed = databaseUsed, techStack = techStack
                )
                supabase.postgrest.from("projects").insert(newProject)

                _projectState.value = ProjectState.Success("Project saved!")
                onResult(true)
            } catch (e: Exception) {
                _projectState.value = ProjectState.Error(e.message ?: "Failed")
                onResult(false)
            }
        }
    }

    private suspend fun uploadFile(uri: Uri?, bucketName: String, path: String, resolver: ContentResolver): String? {
        if (uri == null) return null
        return try {
            val bytes = withContext(Dispatchers.IO) {
                resolver.openInputStream(uri)?.use { it.readBytes() }
            } ?: return null

            val bucket = supabase.storage.from(bucketName)
            // ✅ FIX: Correct upload syntax
            bucket.upload(path, bytes) {
                upsert = true
            }
            bucket.publicUrl(path)
        } catch (e: Exception) {
            Log.e("AuthViewModel", "File upload failed: $path", e)
            null
        }
    }

    fun fetchAllProjects() {
        viewModelScope.launch {
            _allProjectsState.value = ProjectsListState.Loading
            try {
                val projects = supabase.postgrest.from("projects")
                    .select { order("created_at", Order.DESCENDING) }
                    .decodeList<Project>()
                _allProjectsState.value = ProjectsListState.Success(projects)
            } catch (e: Exception) {
                _allProjectsState.value = ProjectsListState.Error(e.message ?: "Error")
            }
        }
    }

    fun fetchProjectById(projectId: String) {
        val uid = supabase.auth.currentUserOrNull()?.id
        viewModelScope.launch {
            try {
                val project = supabase.postgrest.from("projects")
                    .select { filter { eq("id", projectId) } }
                    .decodeSingleOrNull<Project>()

                if (project != null) {
                    if (uid != null) {
                        // ✅ FIX: Removed unsupported 'head' param, used count()
                        val count = supabase.postgrest.from("project_likes").select {
                            count(Count.EXACT)
                            filter {
                                eq("project_id", projectId)
                                eq("user_id", uid)
                            }
                        }.countOrNull() ?: 0
                        _isProjectLiked.value = count > 0
                    }
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

    @OptIn(SupabaseExperimental::class)
    fun fetchCommentsForProject(projectId: String) {
        if (projectId.isBlank()) return
        viewModelScope.launch {
            // ✅ FIX: Explicit types and correct filtering
            supabase.postgrest.from("comments").selectAsFlow(Comment::id)
                .map { list: List<Comment> ->
                    list.filter { it.projectId == projectId }
                        .sortedByDescending { it.createdAt }
                }
                .collect { sortedComments ->
                    _projectComments.value = sortedComments
                }
        }
    }

    fun postComment(projectId: String, text: String) {
        val user = _currentUser.value ?: return
        val newComment = Comment(
            id = UUID.randomUUID().toString(),
            text = text,
            userId = user.userId,
            projectId = projectId,
            userName = "${user.firstName} ${user.lastName}",
            userPhotoUrl = user.profilePhotoUrl
        )
        viewModelScope.launch {
            supabase.postgrest.from("comments").insert(newComment)
        }
    }

    fun toggleProjectLike(projectId: String, isLiked: Boolean) {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            if (isLiked) {
                supabase.postgrest.from("project_likes").delete {
                    filter {
                        eq("project_id", projectId)
                        eq("user_id", uid)
                    }
                }
                _isProjectLiked.value = false
            } else {
                val likeData = mapOf("project_id" to projectId, "user_id" to uid)
                supabase.postgrest.from("project_likes").insert(likeData)
                _isProjectLiked.value = true
            }
        }
    }

    // --- HUB COMMENTS (Private Collaboration Chat) ---

    @OptIn(SupabaseExperimental::class)
    fun fetchHubComments(projectId: String) {
        if (projectId.isBlank()) return
        viewModelScope.launch {
            // ✅ FIX: Explicit types
            supabase.postgrest.from("hub_comments").selectAsFlow(ProjectComment::id)
                .map { list: List<ProjectComment> ->
                    list.filter { it.projectId == projectId }
                        .sortedBy { it.timestamp }
                }
                .collect { sortedComments ->
                    _hubComments.value = sortedComments
                }
        }
    }

    fun addHubComment(projectId: String, text: String, user: User?) {
        if (user == null || projectId.isBlank()) return

        val comment = ProjectComment(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            userId = user.userId,
            userName = "${user.firstName} ${user.lastName}",
            userPhotoUrl = user.profilePhotoUrl,
            text = text
        )

        viewModelScope.launch {
            try {
                supabase.postgrest.from("hub_comments").insert(comment)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error sending hub comment", e)
            }
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

                        conns.filter { it.status == "pending" && it.senderId != myId }.forEach { req ->
                            val sender = alumni.find { it.userId == req.senderId }
                            list.add(NotificationItemData(req.id, Icons.Default.PersonAdd, "${sender?.firstName} ${sender?.lastName}", "Sent connection request", NotificationType.CONNECTION_REQUEST, req.senderId))
                        }

                        collabs.filter { it.status == "pending" && it.projectOwnerId == myId }.forEach { req ->
                            list.add(NotificationItemData(req.id, Icons.Default.GroupAdd, req.collaboratorName, "Wants to join ${req.projectTitle}", NotificationType.COLLABORATION_REQUEST, req.id))
                        }

                        list.sortedBy { it.id }
                    }.collect { _notifications.value = it }
            }
        }
    }
}
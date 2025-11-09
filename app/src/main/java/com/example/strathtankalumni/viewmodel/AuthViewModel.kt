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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

// Authentication state feedback
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

// Project List State for fetching all projects
sealed class ProjectsListState {
    object Loading : ProjectsListState()
    data class Success(val projects: List<Project>) : ProjectsListState()
    data class Error(val message: String) : ProjectsListState()
}


class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // --- State Flows ---
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _projectState = MutableStateFlow<ProjectState>(ProjectState.Idle)
    val projectState: StateFlow<ProjectState> = _projectState

    private val _allProjectsState = MutableStateFlow<ProjectsListState>(ProjectsListState.Loading)
    val allProjectsState: StateFlow<ProjectsListState> = _allProjectsState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // --- State Management Helpers ---

    /**
     * Resets the project state back to Idle.
     */
    fun resetProjectState() {
        _projectState.value = ProjectState.Idle
    }

    /**
     * Resets the auth state back to Idle.
     */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    // --- Authentication & Profile Management Functions (Missing functions added below) ---

    /**
     * Registers a new user with Firebase Authentication and stores their details in Firestore.
     */
    fun registerUser(newUser: User, password: String, onResult: (Boolean, String) -> Unit) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                // 1. Create user in Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(newUser.email, password).await()
                val userId = authResult.user?.uid

                if (userId != null) {
                    // 2. Store user details in Firestore
                    val userWithId = newUser.copy(userId = userId)
                    firestore.collection("users").document(userId).set(userWithId.toMap()).await()

                    _authState.value = AuthState.Idle // Set back to Idle after success, as navigation is handled in the UI
                    onResult(true, "Registration successful! Please log in.")
                } else {
                    throw Exception("User ID not generated.")
                }
            } catch (e: Exception) {
                val message = e.localizedMessage ?: "Registration failed. Check your network."
                Log.e("AuthViewModel", "Registration failed: $message", e)
                _authState.value = AuthState.Error(message)
                onResult(false, message)
            }
        }
    }

    /**
     * Signs in a user with Firebase Authentication and fetches their role.
     */
    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                // 1. Sign in with Firebase Auth
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val userId = authResult.user?.uid

                if (userId != null) {
                    // 2. Fetch user role from Firestore
                    val userSnapshot = firestore.collection("users").document(userId).get().await()
                    val user = userSnapshot.toObject(User::class.java)
                    val role = user?.role

                    _authState.value = AuthState.Success("Logged in successfully!", userRole = role)
                    fetchCurrentUser() // Start fetching current user data
                } else {
                    throw Exception("User ID not found after sign-in.")
                }
            } catch (e: Exception) {
                val message = e.localizedMessage ?: "Sign-in failed. Please check your credentials."
                Log.e("AuthViewModel", "Sign-in failed: $message", e)
                _authState.value = AuthState.Error(message)
            }
        }
    }


    /**
     * Checks if a user is logged in and returns their role from Firestore (for App startup).
     * This is a suspending function suitable for LaunchedEffect.
     */
    suspend fun checkLoggedInUser(): String? = withContext(Dispatchers.IO) {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            return@withContext null
        }

        return@withContext try {
            val snapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = snapshot.toObject(User::class.java)
            user?.role // Returns "alumni", "admin", or null
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error fetching user role on startup: ${e.message}", e)
            null
        }
    }

    /**
     * Fetches the current user's data from Firestore.
     */
    fun fetchCurrentUser() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _currentUser.value = null
            return
        }

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users").document(userId).get().await()
                val user = snapshot.toObject(User::class.java)
                _currentUser.value = user
                Log.d("AuthViewModel", "User data fetched: ${user?.email}")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to fetch user data: ${e.message}", e)
                _currentUser.value = null
            }
        }
    }

    /**
     * Updates the user's profile information in Firestore.
     */
    fun updateUserProfile(
        about: String,
        experience: String,
        skills: List<String>,
        linkedinUrl: String,
        onResult: (Boolean) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("AuthViewModel", "User not logged in for profile update.")
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                val updates = hashMapOf(
                    "about" to about,
                    "experience" to experience,
                    "skills" to skills,
                    "linkedinUrl" to linkedinUrl
                )

                firestore.collection("users").document(userId)
                    .set(updates as Map<String, Any>, SetOptions.merge())
                    .await()

                _currentUser.value = _currentUser.value?.copy(
                    about = about,
                    experience = experience,
                    skills = skills,
                    linkedinUrl = linkedinUrl
                )

                Log.d("AuthViewModel", "User profile updated successfully for ID: $userId")
                onResult(true)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to update user profile: ${e.message}", e)
                onResult(false)
            }
        }
    }

    /**
     * Uploads a profile photo and updates the user's document.
     */
    fun uploadProfilePhoto(imageUri: Uri, contentResolver: ContentResolver, onResult: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                val imageRef = storage.reference.child("profile_photos/$userId/${UUID.randomUUID()}")
                imageRef.putFile(imageUri).await()
                val photoUrl = imageRef.downloadUrl.await().toString()

                firestore.collection("users").document(userId)
                    .update("profilePhotoUrl", photoUrl)
                    .await()

                _currentUser.value = _currentUser.value?.copy(profilePhotoUrl = photoUrl)

                Log.d("AuthViewModel", "Profile photo updated to: $photoUrl")
                onResult(true)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to upload profile photo: ${e.message}", e)
                onResult(false)
            }
        }
    }

    /**
     * Signs out the user.
     */
    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        Log.d("AuthViewModel", "User signed out successfully.")
    }

    // --- Project Management Functions ---

    /**
     * Uploads the image to Firebase Storage.
     */
    private suspend fun uploadImage(imageUri: Uri?): String? = withContext(Dispatchers.IO) {
        if (imageUri == null) return@withContext null

        return@withContext try {
            val imageRef = storage.reference.child("project_images/${UUID.randomUUID()}")
            imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Project image upload failed: ${e.message}", e)
            null
        }
    }

    /**
     * Saves a new project to Firestore.
     */
    fun saveProject(
        title: String,
        description: String,
        projectUrl: String,
        githubUrl: String,
        projectType: String,
        imageUri: Uri?,
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
                val imageUrl = uploadImage(imageUri)
                if (imageUri != null && imageUrl == null) {
                    _projectState.value = ProjectState.Error("Failed to upload project image.")
                    onResult(false)
                    return@launch
                }

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
                    techStack = techStack
                )

                firestore.collection("projects").add(newProject).await()
                _projectState.value = ProjectState.Success("Project saved successfully!")
                onResult(true)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to save project or upload image: ${e.message}", e)
                _projectState.value = ProjectState.Error(e.localizedMessage ?: "Failed to save project.")
                onResult(false)
            }

        }
    }

    /**
     * Fetches all projects from the "projects" collection in Firestore.
     */
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
}
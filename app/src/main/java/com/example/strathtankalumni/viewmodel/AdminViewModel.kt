package com.example.strathtankalumni.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.strathtankalumni.data.Collaboration
import com.example.strathtankalumni.data.Project
import com.example.strathtankalumni.data.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AdminDashboardStats(
    val totalUsers: Int = 0,
    val activeProjects: Int = 0,
    val pendingVerifications: Int = 0,
    val openReports: Int = 0
)

class AdminViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _dashboardStats = MutableStateFlow(AdminDashboardStats())
    val dashboardStats: StateFlow<AdminDashboardStats> = _dashboardStats.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _collaborationRequests = MutableStateFlow<List<Collaboration>>(emptyList())
    val collaborationRequests: StateFlow<List<Collaboration>> = _collaborationRequests.asStateFlow()

    init {
        loadDashboardStats()
        observeUsers()
        observeProjects()
        observeCollaborationRequests()
    }

    private fun loadDashboardStats() {
        viewModelScope.launch {
            try {
                val usersSnapshot = firestore.collection("users").get().await()
                val projectsSnapshot = firestore.collection("projects").get().await()
                val pendingVerifications = usersSnapshot.documents.count { doc ->
                    val role = doc.getString("role") ?: ""
                    val status = doc.getString("verificationStatus") ?: "verified"
                    role != "admin" && status == "pending"
                }

                // If you later add a real reports collection, update this query.
                val reportsSnapshot = firestore.collection("reports").get().await()

                _dashboardStats.value = AdminDashboardStats(
                    totalUsers = usersSnapshot.size(),
                    activeProjects = projectsSnapshot.size(), // refine with a status field later
                    pendingVerifications = pendingVerifications,
                    openReports = reportsSnapshot.size()
                )
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading dashboard stats", e)
            }
        }
    }

    private fun observeUsers() {
        firestore.collection("users")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("AdminViewModel", "Error listening to users", e)
                    _users.value = emptyList()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _users.value = snapshot.toObjects(User::class.java)
                    loadDashboardStats()
                }
            }
    }

    private fun observeProjects() {
        firestore.collection("projects")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("AdminViewModel", "Error listening to projects", e)
                    _projects.value = emptyList()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Project::class.java)?.copy(id = doc.id)
                    }
                    _projects.value = list
                    loadDashboardStats()
                }
            }
    }

    private fun observeCollaborationRequests() {
        firestore.collection("collaborations")
            .orderBy("requestedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("AdminViewModel", "Error listening to collaborations", e)
                    _collaborationRequests.value = emptyList()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _collaborationRequests.value = snapshot.toObjects(Collaboration::class.java)
                }
            }
    }

    fun updateUserVerification(userId: String, newStatus: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(userId)
                    .update("verificationStatus", newStatus)
                    .await()
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating user verification", e)
            }
        }
    }

    fun updateProjectStatus(projectId: String, newStatus: String) {
        if (projectId.isBlank()) return
        viewModelScope.launch {
            try {
                firestore.collection("projects")
                    .document(projectId)
                    .update("status", newStatus)
                    .await()
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating project status", e)
            }
        }
    }

    fun updateCollaborationStatus(collaborationId: String, newStatus: String) {
        if (collaborationId.isBlank()) return
        viewModelScope.launch {
            try {
                firestore.collection("collaborations")
                    .document(collaborationId)
                    .update("status", newStatus)
                    .await()
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating collaboration status", e)
            }
        }
    }
}



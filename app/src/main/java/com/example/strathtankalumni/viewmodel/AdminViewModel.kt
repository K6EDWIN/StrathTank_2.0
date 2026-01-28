package com.example.strathtankalumni.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.strathtankalumni.data.Collaboration
import com.example.strathtankalumni.data.Project
import com.example.strathtankalumni.data.User
import com.example.strathtankalumni.util.Supabase
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminDashboardStats(
    val totalUsers: Int = 0,
    val activeProjects: Int = 0,
    val pendingVerifications: Int = 0,
    val openReports: Int = 0
)

class AdminViewModel : ViewModel() {

    private val supabase = Supabase.client

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
                // ✅ FIX: Use select { count(Count.EXACT) } instead of select(head = true)
                val usersCount = supabase.postgrest.from("users").select {
                    count(Count.EXACT)
                }.countOrNull() ?: 0

                val projectsCount = supabase.postgrest.from("projects").select {
                    count(Count.EXACT)
                }.countOrNull() ?: 0

                val pendingCount = supabase.postgrest.from("users").select {
                    count(Count.EXACT)
                    filter {
                        neq("role", "admin")
                        // eq("verification_status", "pending") // Uncomment if column exists
                    }
                }.countOrNull() ?: 0

                _dashboardStats.value = AdminDashboardStats(
                    totalUsers = usersCount.toInt(),
                    activeProjects = projectsCount.toInt(),
                    pendingVerifications = pendingCount.toInt(),
                    openReports = 0
                )
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading dashboard stats", e)
            }
        }
    }

    @OptIn(SupabaseExperimental::class)
    private fun observeUsers() {
        viewModelScope.launch {
            // ✅ FIX: Use supabase.postgrest.from
            // ✅ FIX: Use User::userId (not User::class.userId)
            supabase.postgrest.from("users").selectAsFlow(User::userId)
                .collect { list: List<User> -> // ✅ FIX: Explicit type for lambda arg
                    _users.value = list
                    loadDashboardStats()
                }
        }
    }

    @OptIn(SupabaseExperimental::class)
    private fun observeProjects() {
        viewModelScope.launch {
            supabase.postgrest.from("projects").selectAsFlow(Project::id)
                .collect { list: List<Project> ->
                    // ✅ FIX: Handle nullable createdAt for sorting
                    _projects.value = list.sortedByDescending { it.createdAt ?: "" }
                    loadDashboardStats()
                }
        }
    }

    @OptIn(SupabaseExperimental::class)
    private fun observeCollaborationRequests() {
        viewModelScope.launch {
            supabase.postgrest.from("collaborations").selectAsFlow(Collaboration::id)
                .collect { list: List<Collaboration> ->
                    // ✅ FIX: Handle nullable requestedAt for sorting
                    _collaborationRequests.value = list.sortedByDescending { it.requestedAt ?: "" }
                }
        }
    }

    fun updateUserVerification(userId: String, newStatus: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            try {
                supabase.postgrest.from("users").update(
                    mapOf("verification_status" to newStatus)
                ) {
                    filter { eq("user_id", userId) }
                }
                loadDashboardStats()
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating user verification", e)
            }
        }
    }

    fun updateProjectStatus(projectId: String, newStatus: String) {
        if (projectId.isBlank()) return
        viewModelScope.launch {
            try {
                supabase.postgrest.from("projects").update(
                    mapOf("status" to newStatus)
                ) {
                    filter { eq("id", projectId) }
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating project status", e)
            }
        }
    }

    fun updateCollaborationStatus(collaborationId: String, newStatus: String) {
        if (collaborationId.isBlank()) return
        viewModelScope.launch {
            try {
                supabase.postgrest.from("collaborations").update(
                    mapOf("status" to newStatus)
                ) {
                    filter { eq("id", collaborationId) }
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating collaboration status", e)
            }
        }
    }
}
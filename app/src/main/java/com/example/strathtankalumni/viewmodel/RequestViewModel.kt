package com.example.strathtankalumni.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.strathtankalumni.ui.alumni.CollaborationRequestItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// A simple data class for Friend Requests to make it more structured
data class FriendRequestItem(
    val name: String
)

class RequestViewModel : ViewModel() {

    // --- Collaboration Requests State ---
    private val _collaborationRequests = MutableStateFlow<List<CollaborationRequestItem>>(emptyList())
    val collaborationRequests: StateFlow<List<CollaborationRequestItem>> = _collaborationRequests.asStateFlow()

    // --- Friend Requests State ---
    private val _friendRequests = MutableStateFlow<List<FriendRequestItem>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequestItem>> = _friendRequests.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // Load initial dummy data
        _collaborationRequests.value = listOf(
            CollaborationRequestItem("Project Alpha", "Seeking Kotlin developer for a new social media app.", "John Doe"),
            CollaborationRequestItem("Research Project", "Need a data analyst for a research project on market trends.", "Jane Smith")
        )
        _friendRequests.value = listOf(
            FriendRequestItem("John Doe"),
            FriendRequestItem("Jane Smith"),
            FriendRequestItem("Peter Jones")
        )
    }

    // --- Public Functions to handle UI events ---

    fun acceptCollaborationRequest(request: CollaborationRequestItem) {
        // In a real app, you would make a network call here.
        // For now, we'''ll just remove it from the list.
        viewModelScope.launch {
            _collaborationRequests.update { currentList ->
                currentList.filterNot { it == request }
            }
        }
    }

    fun declineCollaborationRequest(request: CollaborationRequestItem) {
        viewModelScope.launch {
            _collaborationRequests.update { currentList ->
                currentList.filterNot { it == request }
            }
        }
    }

    fun acceptFriendRequest(request: FriendRequestItem) {
        viewModelScope.launch {
            _friendRequests.update { currentList ->
                currentList.filterNot { it == request }
            }
        }
    }

    fun declineFriendRequest(request: FriendRequestItem) {
        viewModelScope.launch {
            _friendRequests.update { currentList ->
                currentList.filterNot { it == request }
            }
        }
    }
}

package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.strathtankalumni.viewmodel.FriendRequestItem
import com.example.strathtankalumni.viewmodel.RequestViewModel

// This data class is used by the ViewModel. It's good practice to move this
// to a separate 'model' package in a larger app.
data class CollaborationRequestItem(
    val title: String,
    val description: String,
    val creatorName: String
)


@Composable
fun RequestsScreen(
    navController: NavController,
    requestViewModel: RequestViewModel = viewModel()
) {
    // Observe the data from the ViewModel
    val collaborationRequests by requestViewModel.collaborationRequests.collectAsState()
    val friendRequests by requestViewModel.friendRequests.collectAsState()

    val tabs = listOf("Friend Requests (${friendRequests.size})", "Collaboration Requests (${collaborationRequests.size})")
    var selectedTabIndex by remember { mutableIntStateOf(0) } // Default to Friend Requests

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Requests") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )

                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        when (selectedTabIndex) {
            0 -> FriendRequestsContent(
                modifier = Modifier.padding(paddingValues),
                friendRequests = friendRequests,
                onAccept = { requestViewModel.acceptFriendRequest(it) },
                onDecline = { requestViewModel.declineFriendRequest(it) }
            )
            1 -> CollaborationRequestsContent(
                modifier = Modifier.padding(paddingValues),
                collaborationRequests = collaborationRequests,
                onAccept = { requestViewModel.acceptCollaborationRequest(it) },
                onDecline = { requestViewModel.declineCollaborationRequest(it) }
            )
        }
    }
}

@Composable
fun FriendRequestsContent(
    modifier: Modifier = Modifier,
    friendRequests: List<FriendRequestItem>,
    onAccept: (FriendRequestItem) -> Unit,
    onDecline: (FriendRequestItem) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(friendRequests) { request ->
            FriendRequestItemRow(
                request = request,
                onAccept = { onAccept(request) },
                onDecline = { onDecline(request) }
            )
            Divider()
        }
    }
}

@Composable
fun FriendRequestItemRow(
    request: FriendRequestItem,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = request.name)
        Row {
            Button(onClick = onAccept) {
                Text("Accept")
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = onDecline,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Decline")
            }
        }
    }
}

@Composable
fun CollaborationRequestsContent(
    modifier: Modifier = Modifier,
    collaborationRequests: List<CollaborationRequestItem>,
    onAccept: (CollaborationRequestItem) -> Unit,
    onDecline: (CollaborationRequestItem) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(collaborationRequests) { request ->
            CollaborationRequestCard(
                request = request,
                onAccept = { onAccept(request) },
                onDecline = { onDecline(request) }
            )
        }
    }
}

@Composable
fun CollaborationRequestCard(
    request: CollaborationRequestItem,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = request.title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "From: ${request.creatorName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = request.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onAccept) {
                    Text("Accept")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = onDecline) {
                    Text("Decline")
                }
            }
        }
    }
}

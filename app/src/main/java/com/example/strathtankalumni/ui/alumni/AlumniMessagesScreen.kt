package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Import items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Import ViewModel
import androidx.navigation.NavHostController
import com.example.strathtankalumni.data.MessagesViewModel // Import your ViewModel
import com.example.strathtankalumni.navigation.Screen
// ✅ 1. ADD IMPORTS
import com.example.strathtankalumni.viewmodel.AuthViewModel
import com.example.strathtankalumni.data.Connection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniMessagesScreen(
    navController: NavHostController,
    paddingValues: PaddingValues,
    viewModel: MessagesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel() // ✅ 2. INJECT AUTHVIEWMODEL
) {
    // ✅ 3. GET DYNAMIC USER
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserId = currentUser?.userId

    // ✅ 4. GET CONNECTIONS
    val connections by authViewModel.connections.collectAsState()

    // ✅ 5. FILTER FOR ACCEPTED CONNECTIONS
    val acceptedConnections = remember(connections) {
        connections.filter { it.status == "accepted" }
    }

    // --- Load conversations based on accepted connections ---
    // ✅ 6. THIS IS THE FIX
    // This block replaces your old LaunchedEffect
    LaunchedEffect(key1 = currentUserId, key2 = acceptedConnections) {
        if (currentUserId != null) {
            // Call the new function with two arguments
            viewModel.loadConversations(currentUserId, acceptedConnections)
        }
    }

    val conversations by viewModel.conversations.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp)
        )
        Spacer(Modifier.height(24.dp))

        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    // ✅ 7. UPDATED EMPTY TEXT
                    text = if (acceptedConnections.isEmpty())
                        "You haven't made any connections yet.\nFind alumni to connect with."
                    else
                        "No messages yet.\nStart a chat with one of your connections!",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                items(conversations) { convoWithUser ->
                    val otherUser = convoWithUser.user
                    val conversation = convoWithUser.conversation

                    ConversationRow(
                        name = "${otherUser.firstName} ${otherUser.lastName}",
                        lastMessage = conversation.lastMessage,
                        timestamp = "now", // TODO: Format this
                        onClick = {
                            // ✅ 8. FIXED NAVIGATION ARGUMENTS
                            navController.navigate(
                                Screen.DirectMessage.createRoute(
                                    userName = "${otherUser.firstName} ${otherUser.lastName}",
                                    otherUserId = otherUser.userId
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(
    name: String,
    lastMessage: String,
    timestamp: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "$name's profile picture",
            modifier = Modifier.size(56.dp),
            tint = Color.LightGray
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(
                text = lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = timestamp,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
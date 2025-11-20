package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.strathtankalumni.R
import com.example.strathtankalumni.viewmodel.AuthViewModel
import coil.size.Size
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AlumniMessagesScreen(
    navController: NavHostController,
    viewModel: MessagesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val connections by authViewModel.connections.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // 1. Collect search query and filtered list
    val searchQuery by viewModel.searchQuery.collectAsState()
    val conversations by viewModel.filteredConversations.collectAsState()

    // Load conversations whenever connections or user changes
    LaunchedEffect(connections, currentUser) {
        val userId = currentUser?.userId
        if (userId != null && connections.isNotEmpty()) {
            // Filter for accepted connections first
            val accepted = connections.filter { it.status == "accepted" }
            viewModel.loadConversations(userId, accepted)
        }
    }

<<<<<<< Updated upstream
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Messages",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 2. Search Bar connected to ViewModel
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Search messages...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )

        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "No conversations yet." else "No matching conversations.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversations) { conversationWithUser ->
                    ConversationItem(
                        conversationWithUser = conversationWithUser,
                        currentUserId = currentUser?.userId ?: "",
                        onClick = {
                            val otherUser = conversationWithUser.user
                            // Navigate to chat: pass ID and Name
                            navController.navigate("direct_message/${otherUser.userId}/${otherUser.firstName} ${otherUser.lastName}")
=======
    val conversations by viewModel.conversations.collectAsState()


    // --- EDIT: Added Box wrapper for loading spinner ---
    Box(modifier = Modifier.fillMaxSize()) {
        if (currentUser == null) {
            // --- Show a centered spinner while loading ---
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            // --- Once loaded, show content ---
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
                // No Spacer here - this removes the gap

                if (conversations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f) // --- EDIT: Changed from fillMaxSize ---
                            .fillMaxWidth(), // --- EDIT: Added ---
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
                    LazyColumn(
                        modifier = Modifier.weight(1f), // --- EDIT: Added .weight(1f) ---
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(conversations) { convoWithUser ->
                            val otherUser = convoWithUser.user
                            val conversation = convoWithUser.conversation

                            // ⬇️ 2. PASS THE UNREAD COUNT
                            ConversationRow(
                                name = "${otherUser.firstName} ${otherUser.lastName}",
                                lastMessage = conversation.lastMessage,
                                timestamp = "now", // TODO: Format this
                                photoUrl = otherUser.profilePhotoUrl,
                                // This comes from the ConversationWithUser class
                                unreadCount = convoWithUser.unreadCount,
                                onClick = {
                                    // The DirectMessageScreen will call markAsRead
                                    navController.navigate(
                                        Screen.DirectMessage.createRoute(
                                            userName = "${otherUser.firstName} ${otherUser.lastName}",
                                            otherUserId = otherUser.userId
                                        )
                                    )
                                }
                            )
>>>>>>> Stashed changes
                        }
                    }
                }
            }
        }
    }
}

<<<<<<< Updated upstream
=======
// (ConversationRow is unchanged)
@OptIn(ExperimentalMaterial3Api::class) // Added for Badge
>>>>>>> Stashed changes
@Composable
fun ConversationItem(
    conversationWithUser: ConversationWithUser,
    currentUserId: String,
    onClick: () -> Unit
) {
    val user = conversationWithUser.user
    val conversation = conversationWithUser.conversation
    val isUnread = conversationWithUser.unreadCount > 0

    // Determine text style based on read status
    val fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
    val textColor = if (isUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.profilePhotoUrl.takeIf { !it.isNullOrBlank() } ?: R.drawable.noprofile)
                    .crossfade(true)
                    .size(Size(128, 128))
                    .build(),
                contentDescription = "${user.firstName}'s photo",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.noprofile)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${user.firstName} ${user.lastName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Timestamp
                    val date = conversation.lastMessageTimestamp
                    if (date != null) {
                        val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (conversation.lastSenderId == currentUserId) "You: ${conversation.lastMessage}" else conversation.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = fontWeight,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (isUnread) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = conversationWithUser.unreadCount.toString(),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
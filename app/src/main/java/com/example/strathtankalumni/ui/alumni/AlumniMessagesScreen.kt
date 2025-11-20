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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.strathtankalumni.R
import com.example.strathtankalumni.viewmodel.AuthViewModel
//import com.example.strathtankalumni.viewmodel.ConversationWithUser
//import com.example.strathtankalumni.viewmodel.MessagesViewModel
// Ensure MessagesViewModel is imported
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniMessagesScreen(
    navController: NavHostController,
    viewModel: MessagesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val conversations by viewModel.conversations.collectAsState()

    // Load Data Immediately
    LaunchedEffect(currentUser) {
        val userId = currentUser?.userId
        if (userId != null) {
            viewModel.loadConversations(userId, null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Search messages...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            ),
            singleLine = true
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (currentUser == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (conversations.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No results found." else "No messages yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    if (searchQuery.isBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Start a chat from the Directory!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(conversations) { conversationWithUser ->
                        ConversationItem(
                            conversationWithUser = conversationWithUser,
                            currentUserId = currentUser?.userId ?: "",
                            onClick = {
                                val otherUser = conversationWithUser.user

                                // ✅ FIX: Sanitize name to prevent scrambled URL
                                var safeName = "${otherUser.firstName} ${otherUser.lastName}".trim()
                                if (safeName.isBlank()) safeName = "User"
                                // Replace slashes just in case
                                safeName = safeName.replace("/", "-")

                                navController.navigate("direct_message/${otherUser.userId}/$safeName")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversationWithUser: ConversationWithUser,
    currentUserId: String,
    onClick: () -> Unit
) {
    val user = conversationWithUser.user
    val conversation = conversationWithUser.conversation
    val isUnread = conversationWithUser.unreadCount > 0

    val fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
    val textColor = if (isUnread) Color.Black else Color.DarkGray
    val cardBackground = if (isUnread) Color.White else Color(0xFFF5F5F5)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnread) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.profilePhotoUrl.takeIf { !it.isNullOrBlank() } ?: R.drawable.noprofile)
                    .crossfade(true)
                    .size(Size(128, 128))
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.LightGray),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.noprofile)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${user.firstName} ${user.lastName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    conversation.lastMessageTimestamp?.let { date ->
                        Text(
                            text = formatMessageTime(date),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isUnread) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val prefix = if (conversation.lastSenderId == currentUserId) "You: " else ""
                    Text(
                        text = "$prefix${conversation.lastMessage}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = fontWeight,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isUnread) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text(text = conversationWithUser.unreadCount.toString())
                        }
                    }
                }
            }
        }
    }
}

private fun formatMessageTime(date: Date): String {
    val now = System.currentTimeMillis()
    val diff = now - date.time
    return when {
        diff < 24 * 60 * 60 * 1000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        diff < 7 * 24 * 60 * 60 * 1000 -> SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        else -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
    }
}
package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import coil.size.Size
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.strathtankalumni.R
import com.example.strathtankalumni.data.MessagesViewModel
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniMessagesScreen(
    navController: NavHostController,
    paddingValues: PaddingValues,
    viewModel: MessagesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserId = currentUser?.userId

    val connections by authViewModel.connections.collectAsState()

    val acceptedConnections = remember(connections) {
        connections.filter { it.status == "accepted" }
    }

    // load conversations based on accepted connections
    LaunchedEffect(key1 = currentUserId, key2 = acceptedConnections) {
        if (currentUserId != null) {
            viewModel.loadConversations(currentUserId, acceptedConnections)
        }
    }

    val conversations by viewModel.conversations.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            // FIXED: Remove huge top padding, keep only bottom inset padding
            .padding(bottom = paddingValues.calculateBottomPadding())
            .padding(horizontal = 16.dp)
    ) {
        // Search bar at top with normal spacing
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding()),
                contentAlignment = Alignment.Center
            ) {
                Text(
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
                        timestamp = "now",
                        photoUrl = otherUser.profilePhotoUrl,
                        unreadCount = convoWithUser.unreadCount,
                        onClick = {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationRow(
    name: String,
    lastMessage: String,
    timestamp: String,
    photoUrl: String?,
    unreadCount: Int,
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
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoUrl.takeIf { !it.isNullOrBlank() } ?: R.drawable.noprofile)
                .crossfade(true)
                .size(Size(128, 128))
                .allowHardware(false)
                .build(),
            contentDescription = "$name's profile picture",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F3F4)),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.noprofile)
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

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            if (unreadCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = "$unreadCount",
                        modifier = Modifier.padding(horizontal = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
// import androidx.compose.material.icons.filled.AccountCircle // No longer needed here
import coil.size.Size
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.strathtankalumni.R
import com.example.strathtankalumni.data.MessagesViewModel
import com.example.strathtankalumni.viewmodel.AuthViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectMessageScreen(
    navController: NavHostController,
    otherUserId: String,
    userName: String,
    viewModel: MessagesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    // This is correct
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserId = currentUser?.userId

    // --- THIS IS THE MODIFIED BLOCK ---
    LaunchedEffect(key1 = currentUserId, key2 = otherUserId) {
        if (currentUserId != null) {
            // This line was already here
            viewModel.loadDirectMessages(currentUserId, otherUserId)

            // 🚀 THIS IS THE NEW LINE YOU NEEDED
            // It resets the unread count to 0 when you open the chat
            viewModel.markAsRead(currentUserId, otherUserId)
        }
    }
    // --- END OF MODIFICATION ---

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearDirectMessages()
        }
    }

    val messages by viewModel.directMessages.collectAsState()
    val conversations by viewModel.conversations.collectAsState()

    // Find the specific user object for this chat
    val otherUser = remember(conversations, otherUserId) {
        conversations.find { it.user.userId == otherUserId }?.user
    }

    // 1. Get OTHER user's photo URL
    val otherUserPhotoUrl = otherUser?.profilePhotoUrl

    // 2. Get CURRENT user's photo URL
    val currentUserPhotoUrl = currentUser?.profilePhotoUrl

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // This is correct
        CenterAlignedTopAppBar(
            title = { Text(userName, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            items(messages.reversed()) { message ->
                // 3. Pass BOTH URLs to MessageBubble
                MessageBubble(
                    text = message.text,
                    isFromUser = message.senderId == currentUserId,
                    otherUserPhotoUrl = otherUserPhotoUrl,
                    currentUserPhotoUrl = currentUserPhotoUrl
                )
            }
        }

        // 4. Pass CURRENT user's URL to MessageInput
        MessageInput(
            currentUserPhotoUrl = currentUserPhotoUrl,
            onMessageSend = { text ->
                if (currentUserId != null) {
                    viewModel.sendMessage(
                        text = text,
                        senderId = currentUserId,
                        receiverId = otherUserId
                    )
                }
            }
        )
    }
}


@Composable
private fun MessageBubble(
    text: String,
    isFromUser: Boolean,
    otherUserPhotoUrl: String?,
    currentUserPhotoUrl: String?
) {
    val alignment = if (isFromUser) Alignment.CenterEnd else Alignment.CenterStart
    val color =
        if (isFromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val textColor =
        if (isFromUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

    val shape = if (isFromUser) {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    }

    // Helper composable to avoid repeating the AsyncImage code
    @Composable
    fun ProfileImage(photoUrl: String?) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoUrl.takeIf { !it.isNullOrBlank() } ?: R.drawable.noprofile)
                .crossfade(true)
                .size(Size(128, 128))
                .allowHardware(false)
                .build(),
            contentDescription = "Profile photo",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F3F4)),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.noprofile)
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            // 1. If it's the OTHER user, show pic on the LEFT
            if (!isFromUser) {
                ProfileImage(photoUrl = otherUserPhotoUrl)
                Spacer(Modifier.width(8.dp))
            }

            // 2. The Text bubble is always in the middle
            Text(
                text = text,
                color = textColor,
                modifier = Modifier
                    .background(color, shape)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )

            // 3. If it's the CURRENT user, show pic on the RIGHT
            if (isFromUser) {
                Spacer(Modifier.width(8.dp))
                ProfileImage(photoUrl = currentUserPhotoUrl)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageInput(
    currentUserPhotoUrl: String?,
    onMessageSend: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Replaced the hardcoded Icon with the AsyncImage
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(currentUserPhotoUrl.takeIf { !it.isNullOrBlank() } ?: R.drawable.noprofile)
                .crossfade(true)
                .size(Size(128, 128)) // <-- ADD THIS
                .allowHardware(false)
                .build(),
            contentDescription = "Your profile picture",
            modifier = Modifier
                .size(40.dp) // Kept original size
                .clip(CircleShape)
                .background(Color(0xFFF1F3F4)),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.noprofile)
        )

        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Message") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = {
            if (text.isNotBlank()) {
                onMessageSend(text)
                text = ""
            }
        }) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send message",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
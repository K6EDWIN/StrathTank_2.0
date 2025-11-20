package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.strathtankalumni.R
import com.example.strathtankalumni.data.User
import com.example.strathtankalumni.viewmodel.AuthViewModel
//import com.example.strathtankalumni.viewmodel.MessagesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectMessageScreen(
    navController: NavHostController,
    otherUserId: String,
    userName: String,
    viewModel: MessagesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserId = currentUser?.userId

    // ✅ State to hold the REAL profile fetched from DB
    var chatPartner by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(key1 = currentUserId, key2 = otherUserId) {
        if (currentUserId != null) {
            viewModel.loadDirectMessages(currentUserId, otherUserId)
            viewModel.markAsRead(currentUserId, otherUserId)
        }
        // ✅ FIX: Fetch the real user details immediately
        authViewModel.fetchUserById(otherUserId) { user ->
            chatPartner = user
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearDirectMessages() }
    }

    val messages by viewModel.directMessages.collectAsState()

    // ✅ FIX: Use fetched name if available, else fallback to nav param
    val displayName = chatPartner?.let { "${it.firstName} ${it.lastName}" } ?: userName
    val displayPhoto = chatPartner?.profilePhotoUrl
    val myPhoto = currentUser?.profilePhotoUrl

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(photoUrl = displayPhoto, size = 32.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F7FB))
        ) {
            if (currentUser == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // --- Chat List ---
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        reverseLayout = true,
                        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages.reversed()) { message ->
                            MessageBubble(
                                text = message.text,
                                isFromUser = message.senderId == currentUserId,
                                showAvatar = message.senderId != currentUserId,
                                userPhotoUrl = if (message.senderId == currentUserId) myPhoto else displayPhoto
                            )
                        }
                    }

                    // --- Input Area ---
                    Surface(
                        tonalElevation = 2.dp,
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        MessageInput(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .imePadding(),
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
            }
        }
    }
}

@Composable
private fun MessageBubble(
    text: String,
    isFromUser: Boolean,
    showAvatar: Boolean,
    userPhotoUrl: String?
) {
    val bubbleColor = if (isFromUser) MaterialTheme.colorScheme.primary else Color.White
    val textColor = if (isFromUser) MaterialTheme.colorScheme.onPrimary else Color.Black
    val shadowElevation = if (isFromUser) 0.dp else 1.dp

    val bubbleShape = if (isFromUser) {
        RoundedCornerShape(18.dp, 2.dp, 18.dp, 18.dp)
    } else {
        RoundedCornerShape(2.dp, 18.dp, 18.dp, 18.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isFromUser) {
            if (showAvatar) {
                UserAvatar(photoUrl = userPhotoUrl, size = 32.dp)
                Spacer(Modifier.width(8.dp))
            } else {
                Spacer(Modifier.width(40.dp))
            }
        }

        Surface(
            color = bubbleColor,
            shape = bubbleShape,
            shadowElevation = shadowElevation,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun MessageInput(
    modifier: Modifier = Modifier,
    onMessageSend: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Type a message...") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F7FB),
                unfocusedContainerColor = Color(0xFFF5F7FB),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )

        Spacer(Modifier.width(8.dp))

        val isSendEnabled = text.isNotBlank()
        IconButton(
            onClick = {
                if (isSendEnabled) {
                    onMessageSend(text)
                    text = ""
                }
            },
            enabled = isSendEnabled,
            modifier = Modifier.background(
                if (isSendEnabled) MaterialTheme.colorScheme.primary else Color.LightGray,
                CircleShape
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = Color.White
            )
        }
    }
}

@Composable
fun UserAvatar(photoUrl: String?, size: androidx.compose.ui.unit.Dp) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(photoUrl.takeIf { !it.isNullOrBlank() } ?: R.drawable.noprofile)
            .crossfade(true)
            .size(Size(128, 128))
            .build(),
        contentDescription = "Avatar",
        modifier = Modifier.size(size).clip(CircleShape).background(Color.LightGray),
        contentScale = ContentScale.Crop,
        error = painterResource(id = R.drawable.noprofile)
    )
}
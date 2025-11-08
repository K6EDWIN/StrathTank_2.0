package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Import items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Send // Import Send icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Import ViewModel
import androidx.navigation.NavHostController
import com.example.strathtankalumni.data.MessagesViewModel // Import your ViewModel
import com.example.strathtankalumni.viewmodel.AuthViewModel // ✅ 1. ADD AUTHVIEWMODEL IMPORT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectMessageScreen(
    navController: NavHostController,
    otherUserId: String,
    userName: String,
    viewModel: MessagesViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel() // ✅ 2. INJECT AUTHVIEWMODEL
) {
    // ✅ 3. GET THE DYNAMIC CURRENT USER ID
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserId = currentUser?.userId

    // ✅ 4. UPDATE EFFECT TO HANDLE A NULL USER ID
    // Load messages when currentUserId or otherUserId changes
    LaunchedEffect(key1 = currentUserId, key2 = otherUserId) {
        if (currentUserId != null) {
            viewModel.loadDirectMessages(currentUserId, otherUserId)
        }
    }

    // Clear messages when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearDirectMessages()
        }
    }

    val messages by viewModel.directMessages.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TopAppBar(
            title = { Text(userName, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
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
                MessageBubble(
                    text = message.text,
                    isFromUser = message.senderId == currentUserId // This check is safe
                )
            }
        }

        MessageInput(
            onMessageSend = { text ->
                // ✅ 5. ENSURE USER IS NOT NULL BEFORE SENDING
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
private fun MessageBubble(text: String, isFromUser: Boolean) {
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
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Text(
            text = text,
            color = textColor,
            modifier = Modifier
                .background(color, shape)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageInput(
    onMessageSend: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Your profile picture",
            modifier = Modifier.size(40.dp),
            tint = Color.LightGray
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
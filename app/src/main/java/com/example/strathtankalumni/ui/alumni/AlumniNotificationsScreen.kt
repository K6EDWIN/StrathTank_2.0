package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.strathtankalumni.R
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel
import com.example.strathtankalumni.viewmodel.NotificationItemData
import com.example.strathtankalumni.viewmodel.NotificationType

// ❌ REMOVED: NotificationType enum (It is now in NotificationModels.kt)
// ❌ REMOVED: NotificationItemData data class (It is now in NotificationModels.kt)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniNotificationsScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val notifications by authViewModel.notifications.collectAsState()
    val unreadCount = notifications.size

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notifications", fontWeight = FontWeight.Bold)

                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            EmptyNotificationState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationItem(
                        item = notification,
                        onClick = {
                            when (notification.type) {
                                NotificationType.CONNECTION_REQUEST -> {
                                    notification.referenceId?.let { senderId ->
                                        navController.navigate(Screen.OtherProfile.createRoute(senderId))
                                    }
                                }
                                NotificationType.COLLABORATION_REQUEST -> {
                                    notification.referenceId?.let { collaborationId ->
                                        navController.navigate(Screen.CollaborationDetail.createRoute(collaborationId))
                                    }
                                }
                                NotificationType.NEW_MESSAGE -> {
                                    notification.referenceId?.let { userId ->
                                        navController.navigate("direct_message/$userId/User")
                                    }
                                }
                                NotificationType.SYSTEM_UPDATE -> {
                                    // Handle generic updates
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    item: NotificationItemData,
    onClick: () -> Unit
) {
    val (iconVector, iconColor, containerColor) = when (item.type) {
        NotificationType.CONNECTION_REQUEST -> Triple(
            Icons.Default.PersonAdd,
            Color(0xFF0A66C2),
            Color(0xFFE8F4F9)
        )
        NotificationType.COLLABORATION_REQUEST -> Triple(
            Icons.Default.Handshake,
            Color(0xFFD84315),
            Color(0xFFFBE9E7)
        )
        NotificationType.NEW_MESSAGE -> Triple(
            Icons.Default.Mail,
            Color(0xFF2E7D32),
            Color(0xFFE8F5E9)
        )
        NotificationType.SYSTEM_UPDATE -> Triple(
            Icons.Default.Image,
            Color(0xFF6200EE),
            Color(0xFFEDE7F6)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Text content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.subtitle,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 20.sp
                    )

                    // Optional: Add a "View" prompt
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tap to view details",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Image Support
            if (item.imageUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Notification Attachment",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.sample_featured)
                )
            }
        }
    }
}

@Composable
fun EmptyNotificationState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.LightGray
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No notifications yet",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        Text(
            text = "We'll let you know when something arrives.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}
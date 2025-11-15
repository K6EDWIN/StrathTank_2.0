// megre branch ]/StrathTank_2.0-merge/app/src/main/java/com/example/strathtankalumni/ui/alumni/AlumniNotificationsScreen.kt
package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.LocalIndication // ✅ IMPORT ADDED
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource // ✅ IMPORT ADDED
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember // ✅ IMPORT ADDED
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniNotificationsScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val notifications by authViewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notifications") },
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (notifications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No notifications yet", color = Color.Gray)
                    }
                }
            } else {
                items(notifications) { notification ->
                    NotificationItem(
                        item = notification,
                        onClick = {
                            when (notification.type) {
                                NotificationType.CONNECTION_REQUEST -> {
                                    notification.referenceId?.let { senderId ->
                                        navController.navigate(
                                            Screen.OtherProfile.createRoute(senderId)
                                        )
                                    }
                                }
                                // --- NEW CASE ---
                                NotificationType.COLLABORATION_REQUEST -> {
                                    notification.referenceId?.let { collaborationId ->
                                        navController.navigate(
                                            Screen.CollaborationDetail.createRoute(collaborationId)
                                        )
                                    }
                                }
                                NotificationType.NEW_MESSAGE -> {
                                    // TODO: Navigate to DM screen
                                }
                                NotificationType.EVENT -> {
                                    // TODO: Navigate to Event details screen
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * A new Composable for rendering a single notification item, based on your image.
 */
@Composable
fun NotificationItem(
    item: NotificationItemData,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with circle background
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F3F4)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.type.name,
                tint = Color.DarkGray,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
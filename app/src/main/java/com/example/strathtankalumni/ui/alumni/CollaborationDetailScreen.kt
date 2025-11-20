package com.example.strathtankalumni.ui.alumni

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.strathtankalumni.R
import com.example.strathtankalumni.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaborationDetailScreen(
    navController: NavHostController,
    collaborationId: String,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val collaborations by authViewModel.collaborations.collectAsState()
    val members by authViewModel.collaborationMembers.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val comments by authViewModel.hubComments.collectAsState()

    // Get the specific collaboration object
    val collaboration = remember(collaborations, collaborationId) {
        collaborations.find { it.id == collaborationId }
    }

    // Load Data
    LaunchedEffect(collaboration) {
        if (collaboration != null) {
            authViewModel.getUsersForCollaboration(collaboration.projectId, collaboration.projectOwnerId)
            authViewModel.fetchComments(collaboration.projectId)
        } else {
            authViewModel.clearCollaborationMembers()
        }
    }

    var commentText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collaboration Hub", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (collaboration != null) {
                ChatInputBar(
                    text = commentText,
                    onTextChange = { commentText = it },
                    onSend = {
                        if (commentText.isNotBlank()) {
                            authViewModel.addComment(
                                projectId = collaboration.projectId,
                                text = commentText,
                                user = currentUser
                            )
                            commentText = ""
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (collaboration == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF9FAFB)), // Light grey background
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // --- 1. PROJECT HERO HEADER ---
                item {
                    Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(collaboration.projectImageUrl.ifEmpty { R.drawable.sample_featured })
                                .crossfade(true)
                                .build(),
                            contentDescription = "Project Cover",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                        startY = 100f
                                    )
                                )
                        )
                        // Status Badge
                        Surface(
                            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                            shape = RoundedCornerShape(50),
                            color = if(collaboration.status == "accepted") Color(0xFF4CAF50) else Color(0xFFFF9800)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if(collaboration.status == "accepted") Icons.Default.CheckCircle else Icons.Default.Pending,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = collaboration.status.replaceFirstChar { it.uppercase() },
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Title at bottom
                        Column(
                            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                        ) {
                            Text(
                                text = collaboration.projectTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // --- 2. DETAILS & ACTIONS ---
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            // Description
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = collaboration.projectDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray
                                )
                            }

                            Divider(Modifier.padding(vertical = 16.dp))

                            // Admin Actions
                            val isOwner = currentUser?.userId == collaboration.projectOwnerId
                            val isCollaborator = currentUser?.userId == collaboration.collaboratorId
                            val isPending = collaboration.status == "pending"

                            if (isOwner && isPending) {
                                Text("Request Action", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            authViewModel.updateCollaborationStatus(collaboration.id, "accepted")
                                            Toast.makeText(context, "Accepted!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                    ) { Text("Accept") }

                                    OutlinedButton(
                                        onClick = {
                                            authViewModel.updateCollaborationStatus(collaboration.id, "declined")
                                            navController.popBackStack()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) { Text("Decline") }
                                }
                            } else if (isCollaborator && collaboration.status == "accepted") {
                                OutlinedButton(
                                    onClick = {
                                        authViewModel.updateCollaborationStatus(collaboration.id, "left")
                                        navController.popBackStack()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) { Text("Leave Team") }
                            } else {
                                Text(
                                    text = "You are part of this team.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // --- 3. TEAM MEMBERS ---
                item {
                    PaddingLabel("Team Members")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy((-12).dp) // Overlap effect
                    ) {
                        items(members, key = { it.userId }) { member ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(member.profilePhotoUrl.ifEmpty { R.drawable.noprofile })
                                    .crossfade(true)
                                    .build(),
                                contentDescription = member.firstName,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // --- 4. DISCUSSION ---
                item {
                    PaddingLabel("Team Discussion")
                }

                if (comments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Start the conversation!", color = Color.Gray)
                        }
                    }
                } else {
                    items(comments) { comment ->
                        DiscussionItem(
                            name = comment.userName,
                            photoUrl = comment.userPhotoUrl,
                            time = formatCommentTime(comment.timestamp),
                            comment = comment.text,
                            isMe = comment.userId == currentUser?.userId
                        )
                    }
                }

                // Space for Input bar
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

// --- COMPONENTS ---

@Composable
private fun PaddingLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ChatInputBar(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(
        tonalElevation = 4.dp,
        color = Color.White,
        modifier = Modifier.imePadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Type a message...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                maxLines = 3
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape),
                enabled = text.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@Composable
private fun DiscussionItem(
    name: String,
    photoUrl: String?,
    time: String,
    comment: String,
    isMe: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUrl ?: R.drawable.noprofile)
                    .build(),
                contentDescription = name,
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isMe) {
                    Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                }
                Text(time, fontSize = 10.sp, color = Color.LightGray)
            }

            Surface(
                color = if (isMe) MaterialTheme.colorScheme.primaryContainer else Color.White,
                shape = if (isMe) RoundedCornerShape(16.dp, 0.dp, 16.dp, 16.dp) else RoundedCornerShape(0.dp, 16.dp, 16.dp, 16.dp),
                shadowElevation = 1.dp
            ) {
                Text(
                    text = comment,
                    modifier = Modifier.padding(12.dp),
                    color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else Color.Black
                )
            }
        }
    }
}

private fun formatCommentTime(date: Date?): String {
    if (date == null) return "Just now"
    val diff = System.currentTimeMillis() - date.time
    return when {
        diff < 60000 -> "Now"
        diff < 3600000 -> "${diff / 60000}m"
        diff < 86400000 -> "${diff / 3600000}h"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
    }
}
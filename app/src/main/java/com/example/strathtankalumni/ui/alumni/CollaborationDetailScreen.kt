// megre branch ]/StrathTank_2.0-merge/app/src/main/java/com/example/strathtankalumni/ui/alumni/CollaborationDetailScreen.kt
package com.example.strathtankalumni.ui.alumni

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.strathtankalumni.viewmodel.AuthViewModel

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

    val collaboration = remember(collaborations, collaborationId) {
        collaborations.find { it.id == collaborationId }
    }

    LaunchedEffect(collaboration) {
        if (collaboration != null) {
            authViewModel.getUsersForCollaboration(collaboration.projectId, collaboration.projectOwnerId)
        } else {
            authViewModel.clearCollaborationMembers() // Clear list if collab is null
        }
    }

    // State for discussion
    var commentText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Project Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (collaboration == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Loading collaboration...")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Project Overview ---
            item {
                Text(
                    text = collaboration.projectTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = collaboration.projectDescription,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }

            // --- Current Members ---
            item {
                Text(
                    text = "Current Members",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    items(members, key = { it.userId }) { member ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(member.profilePhotoUrl.ifEmpty { R.drawable.noprofile })
                                .crossfade(true)
                                .allowHardware(false) // ✅ --- THIS IS THE FIX ---
                                .build(),
                            contentDescription = member.firstName,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.noprofile)
                        )
                    }
                }
            }

            // --- Action Buttons ---
            item {
                val isOwner = currentUser?.userId == collaboration.projectOwnerId
                val isCollaborator = currentUser?.userId == collaboration.collaboratorId
                val isPending = collaboration.status == "pending"

                when {
                    // Project owner viewing a pending request
                    isOwner && isPending -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    authViewModel.updateCollaborationStatus(collaboration.id, "accepted")
                                    Toast.makeText(context, "Collaboration accepted!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Accept Request")
                            }
                            OutlinedButton(
                                onClick = {
                                    authViewModel.updateCollaborationStatus(collaboration.id, "declined")
                                    Toast.makeText(context, "Collaboration declined.", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Decline Request")
                            }
                        }
                    }

                    // Accepted collaborator viewing the project
                    isCollaborator && collaboration.status == "accepted" -> {
                        Button(
                            onClick = {
                                authViewModel.updateCollaborationStatus(collaboration.id, "left")
                                Toast.makeText(context, "You have left the team.", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Leave The team")
                        }
                    }
                }
            }

            // --- Discussion ---
            item {
                Text(
                    text = "Discussion",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Stubbed discussion items
            item { DiscussionItem("Ethan", "2h ago", "Excited to see this project come to life!") }
            item { DiscussionItem("Olivia", "3h ago", "Looking forward to collaborating with everyone.") }
            item { DiscussionItem("Noah", "4h ago", "Let's make this project a success!") }

            // Comment Input
            item {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Add a comment...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(32.dp)) // Padding at the bottom
            }
        }
    }
}

@Composable
private fun DiscussionItem(name: String, time: String, comment: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.width(8.dp))
                Text(time, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(comment, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}
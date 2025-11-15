// megre branch ]/StrathTank_2.0-merge/app/src/main/java/com/example/strathtankalumni/ui/alumni/ProjectViewScreen.kt
package com.example.strathtankalumni.ui.alumni

import android.content.Intent
import android.net.Uri
import android.util.Log // ✅ IMPORT
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send // ✅ IMPORT
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector // ✅ IMPORT
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage // ✅ IMPORT
import coil.request.ImageRequest
import com.example.strathtankalumni.R
import com.example.strathtankalumni.data.Comment // ✅ IMPORT
import com.example.strathtankalumni.data.Project
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.LocalIndication // ✅ IMPORT

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProjectViewScreen(
    project: Project,
    onBack: () -> Unit,
    // ✅ 1. ADD THESE PARAMETERS
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // --- State from ViewModel ---
    val comments by authViewModel.projectComments.collectAsState()
    val isLiked by authViewModel.isProjectLiked.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // --- Collaboration State ---
    val collaborations by authViewModel.collaborations.collectAsState()
    val myCollaboration = remember(collaborations, project, currentUser) {
        collaborations.find {
            it.projectId == project.id && it.collaboratorId == currentUser?.userId
        }
    }

    // --- Load comments ---
    LaunchedEffect(project.id) {
        Log.d("ProjectViewScreen", "Fetching comments for project: ${project.id}")
        authViewModel.fetchCommentsForProject(project.id)
    }

    // --- Clear comments on exit ---
    DisposableEffect(project.id) {
        onDispose {
            Log.d("ProjectViewScreen", "Clearing comments for project: ${project.id}")
            authViewModel.clearComments()
        }
    }

    // --- Helper function ---
    val openUrl: (String) -> Unit = { url ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        // ---- Project Image (Cover) ----
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(project.imageUrl.ifEmpty { R.drawable.sample_featured })
                .crossfade(true)
                .allowHardware(false) // ✅ --- CRASH FIX 1 ---
                .build(),
            contentDescription = "Project Cover Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.sample_featured)
        )
        Spacer(Modifier.height(16.dp))

        // ---- Project Details ----
        Text(project.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            project.description,
            fontSize = 14.sp,
            color = Color.DarkGray,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(12.dp))

        // ---- Likes and Comments ----
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = {
                authViewModel.toggleProjectLike(project.id, isLiked)
            }) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.Gray
                )
            }
            Text(project.likes.toString(), fontSize = 14.sp) // Use dynamic project.likes
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = "Comments",
                tint = Color.Gray
            )
            Text(project.commentCount.toString(), fontSize = 14.sp) // Use dynamic project.commentCount
        }
        Spacer(Modifier.height(20.dp))

        // --- GitHub & Live Demo Section ---
        Text("Links", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        if (project.projectUrl.isNotBlank()) {
            ProjectLinkButton("View Live Demo", Icons.Default.OpenInBrowser) { openUrl(project.projectUrl) }
            Spacer(Modifier.height(8.dp))
        }

        if (project.githubUrl.isNotBlank()) {
            ProjectLinkButton("View on GitHub", Icons.Default.Code) { openUrl(project.githubUrl) }
            Spacer(Modifier.height(8.dp))
        }

        // ✅ 2. DYNAMIC COLLABORATION BUTTON
        if (currentUser?.userId != project.userId) { // Only show if NOT the project owner
            val buttonText = when (myCollaboration?.status) {
                "pending" -> "Request Sent"
                "accepted" -> "View Collaboration"
                "declined", "left" -> "Request Collaboration"
                null -> "Request Collaboration"
                else -> "Request Collaboration"
            }
            val isEnabled = myCollaboration?.status != "pending"

            OutlinedButton(
                onClick = {
                    when (myCollaboration?.status) {
                        "accepted" -> {
                            navController.navigate(Screen.CollaborationDetail.createRoute(myCollaboration.id))
                        }
                        null, "declined", "left" -> {
                            authViewModel.requestCollaboration(project)
                            Toast.makeText(context, "Collaboration request sent!", Toast.LENGTH_SHORT).show()
                        }
                        else -> { /* Do nothing if pending */ }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                enabled = isEnabled
            ) {
                Text(buttonText)
            }
        }
        Spacer(Modifier.height(20.dp))
        // --- End Links Section ---

        // ---- Tags ----
        val techTags = (project.programmingLanguages + project.databaseUsed + project.techStack).distinct()
        if (techTags.isNotEmpty()) {
            Text("Tech Stack", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                techTags.forEach { tag -> ProjectTag(tag) }
            }
            Spacer(Modifier.height(24.dp))
        }

        // ---- Project Media Section ----
        if (project.mediaImageUrls.isNotEmpty()) {
            Text("Project Media", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(project.mediaImageUrls) { imageUrl ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .allowHardware(false) // ✅ --- CRASH FIX 2 ---
                            .build(),
                        contentDescription = "Project Media Image",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0F0F0)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.sample_featured)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // 2. PDF Document
        if (project.pdfUrl.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        onClick = { openUrl(project.pdfUrl) } // Open PDF in browser/viewer
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = "PDF Icon",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "View Documentation (PDF)",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.OpenInBrowser,
                        contentDescription = "Open Link",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ---- Comments ----
        Spacer(Modifier.height(20.dp))
        CommentSection(
            projectId = project.id,
            comments = comments,
            authViewModel = authViewModel,
            currentUserPhotoUrl = currentUser?.profilePhotoUrl
        )
        Spacer(Modifier.height(32.dp))
    }
}

// Re-styled Link Chip as a full-width OutlinedButton
@Composable
private fun ProjectLinkButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Icon(icon, null, Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

// Helper Composable for project tags
@Composable
private fun ProjectTag(tag: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Text(
            text = tag,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

// Helper Composable for comment items
@Composable
private fun CommentSection(
    projectId: String,
    comments: List<Comment>,
    authViewModel: AuthViewModel,
    currentUserPhotoUrl: String?
) {
    // Add logging for debugging
    LaunchedEffect(comments) {
        Log.d("CommentSection", "Rendering ${comments.size} comments for project: $projectId")
    }

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Comments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            // Show comment count badge
            if (comments.isNotEmpty()) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${comments.size}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (comments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "No comments yet.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        "Be the first to comment!",
                        textAlign = TextAlign.Center,
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Sort comments by date, handling null dates safely
                comments
                    .sortedBy { it.createdAt ?: Date(0) } // Show oldest first
                    .forEach { comment ->
                        CommentItem(comment)
                    }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Comment Input Area
        CommentInputArea(projectId, authViewModel, currentUserPhotoUrl)
    }
}

@Composable
private fun CommentInputArea(projectId: String, authViewModel: AuthViewModel, currentUserPhotoUrl: String?) {
    var commentText by remember { mutableStateOf("") }
    val isSendEnabled = commentText.isNotBlank()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // User profile photo
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(currentUserPhotoUrl.takeIf { !it.isNullOrBlank() } ?: R.drawable.noprofile)
                .crossfade(true)
                .allowHardware(false) // ✅ --- CRASH FIX 3 ---
                .build(),
            contentDescription = "User Photo",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.noprofile)
        )

        // Comment text field
        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            label = { Text("Add a comment...") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (isSendEnabled) {
                            Log.d("CommentInputArea", "Posting comment: $commentText for project: $projectId")
                            authViewModel.postComment(projectId, commentText)
                            commentText = ""
                        }
                    },
                    enabled = isSendEnabled
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (isSendEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Gray
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun CommentItem(comment: Comment) {
    val timeFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // User profile photo
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(comment.userPhotoUrl.takeIf { !it.isNullOrBlank() } ?: R.drawable.noprofile)
                .crossfade(true)
                .allowHardware(false) // ✅ --- CRASH FIX 4 ---
                .build(),
            contentDescription = "User Photo",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.noprofile)
        )

        // Comment content
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.userName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.width(4.dp))
                comment.createdAt?.let { date ->
                    Text(
                        text = "• ${timeFormatter.format(date)}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = comment.text,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }
    }
}
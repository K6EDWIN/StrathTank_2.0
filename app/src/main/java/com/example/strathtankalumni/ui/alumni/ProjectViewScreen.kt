package com.example.strathtankalumni.ui.alumni

import android.util.Log
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.strathtankalumni.viewmodel.AuthViewModel
import com.example.strathtankalumni.data.Project
import com.example.strathtankalumni.data.Comment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectViewScreen(
    project: Project,
    onBack: () -> Unit,
    authViewModel: AuthViewModel
) {
    val scrollState = rememberScrollState()
    val comments by authViewModel.projectComments.collectAsState()
    val isLiked by authViewModel.isProjectLiked.collectAsState(initial = project.isLiked)
    val currentUser by authViewModel.currentUser.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // Fetch comments when project changes
    LaunchedEffect(project.id) {
        Log.d("ProjectViewScreen", "Fetching comments for project: ${project.id}")
        authViewModel.fetchCommentsForProject(project.id)
    }

    // Log when comments update
    LaunchedEffect(comments) {
        Log.d("ProjectViewScreen", "Comments updated: ${comments.size} comments received")
        comments.forEach { comment ->
            Log.d("ProjectViewScreen", "Comment: ${comment.text}, User: ${comment.userName}, Date: ${comment.createdAt}")
        }
    }

    // Clear comments when leaving screen
    DisposableEffect(project.id) {
        onDispose {
            Log.d("ProjectViewScreen", "Clearing comments for project: ${project.id}")
            authViewModel.clearComments()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Project Image
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(project.imageUrl)
                        .crossfade(true)
                        .build()
                ),
                contentDescription = "Project Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(16.dp))

            // Project Title and Date
            Text(
                text = project.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = project.createdAt?.let { "Posted on ${dateFormatter.format(it)}" } ?: "Date N/A",
                color = Color.Gray,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(12.dp))

            // Like and Comment Counts
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = { authViewModel.toggleProjectLike(project.id, isLiked) }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.Gray
                    )
                }
                Text("${project.likes}", fontSize = 14.sp, color = Color.Gray)

                Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comments", tint = Color.Gray)
                Text(
                    text = "${project.commentCount}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Description Section
            Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(project.description, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))

            // Resources Section
            if (project.projectUrl.isNotBlank() || project.githubUrl.isNotBlank() || project.pdfUrl.isNotBlank()) {
                Text("Resources", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (project.projectUrl.isNotBlank())
                        ProjectLinkChip("Live Project", Icons.Default.OpenInBrowser) {}
                    if (project.githubUrl.isNotBlank())
                        ProjectLinkChip("GitHub Repo", Icons.Default.OpenInBrowser) {}
                    if (project.pdfUrl.isNotBlank())
                        ProjectLinkChip("Documentation (PDF)", Icons.Default.PictureAsPdf) {}
                }
                Spacer(Modifier.height(16.dp))
            }

            // Tech Stack Section
            val techTags = project.programmingLanguages + project.databaseUsed + project.techStack
            if (techTags.isNotEmpty()) {
                Text("Tech Stack", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(techTags) { tag -> ProjectTag(tag) }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Comments Section
            CommentSection(
                projectId = project.id,
                comments = comments,
                authViewModel = authViewModel,
                currentUserPhotoUrl = currentUser?.profilePhotoUrl
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProjectLinkChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = {
            Icon(icon, null, Modifier.size(18.dp))
        }
    )
}

@Composable
fun ProjectTag(tag: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Text(
            tag,
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun CommentSection(
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
                    .sortedByDescending { it.createdAt ?: Date(0) }
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
fun CommentInputArea(projectId: String, authViewModel: AuthViewModel, currentUserPhotoUrl: String?) {
    var commentText by remember { mutableStateOf("") }
    val isSendEnabled = commentText.isNotBlank()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // User profile photo
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(currentUserPhotoUrl)
                    .crossfade(true)
                    .build()
            ),
            contentDescription = "User Photo",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
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
fun CommentItem(comment: Comment) {
    val timeFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // User profile photo
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(comment.userPhotoUrl)
                    .crossfade(true)
                    .build()
            ),
            contentDescription = "User Photo",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
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
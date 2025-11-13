package com.example.strathtankalumni.ui.alumni

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            // --- Project Image ---
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
            Text(project.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                text = project.createdAt?.let { "Posted on ${dateFormatter.format(it)}" } ?: "Date N/A",
                color = Color.Gray, fontSize = 13.sp
            )

            Spacer(Modifier.height(12.dp))

            // --- Like + Comment Row ---
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(
                    onClick = {
                        authViewModel.toggleProjectLike(project.id, isLiked)
                    }
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
                    // Show live comment count or fallback to saved count
                    text = if (comments.isNotEmpty()) "${comments.size}" else "${project.commentCount}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // --- Description ---
            Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(project.description, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))

            // --- Resources ---
            if (project.projectUrl.isNotBlank() || project.githubUrl.isNotBlank() || project.pdfUrl.isNotBlank()) {
                Text("Resources", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (project.projectUrl.isNotBlank())
                        ProjectLinkChip("Live Project", Icons.Default.OpenInBrowser) { /* open project.projectUrl */ }
                    if (project.githubUrl.isNotBlank())
                        ProjectLinkChip("GitHub Repo", Icons.Default.OpenInBrowser) { /* open project.githubUrl */ }
                    if (project.pdfUrl.isNotBlank())
                        ProjectLinkChip("Documentation (PDF)", Icons.Default.PictureAsPdf) { /* open project.pdfUrl */ }
                }
                Spacer(Modifier.height(16.dp))
            }

            // --- Tech Stack ---
            val techTags = project.programmingLanguages + project.databaseUsed + project.techStack
            if (techTags.isNotEmpty()) {
                Text("Tech Stack", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(techTags) { tag -> ProjectTag(tag) }
                }
                Spacer(Modifier.height(24.dp))
            }

            // --- Comments ---
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
    AssistChip(onClick = onClick, label = { Text(text) }, leadingIcon = { Icon(icon, null, Modifier.size(18.dp)) })
}

@Composable
fun ProjectTag(tag: String) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Text(tag, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

// --- COMMENTS SECTION ---
@Composable
fun CommentSection(
    projectId: String,
    comments: List<Comment>,
    authViewModel: AuthViewModel,
    currentUserPhotoUrl: String?
) {
    Column(Modifier.fillMaxWidth()) {
        // Removed (0)
        Text("Comments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        CommentInputArea(projectId, authViewModel, currentUserPhotoUrl)
        Divider(modifier = Modifier.padding(vertical = 16.dp))

        if (comments.isEmpty()) {
            Text("Be the first to comment!", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.Gray)
        } else {
            // Comments list directly below heading
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                comments.forEach { CommentItem(it) }
            }
        }
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
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current).data(currentUserPhotoUrl).crossfade(true).build()
            ),
            contentDescription = "User Photo",
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
            contentScale = ContentScale.Crop
        )

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
                            authViewModel.submitComment(projectId, commentText)
                            commentText = ""
                        }
                    },
                    enabled = isSendEnabled
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}

@Composable
fun CommentItem(comment: Comment) {
    val timeFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current).data(comment.userPhotoUrl).crossfade(true).build()
            ),
            contentDescription = "User Photo",
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.LightGray),
            contentScale = ContentScale.Crop
        )

        Column {
            // Only show first name
            val firstName = comment.userName.split(" ").firstOrNull() ?: comment.userName
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(firstName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                comment.createdAt?.let {
                    Text("• ${timeFormatter.format(it)}", color = Color.Gray, fontSize = 12.sp)
                }
            }
            Text(comment.text, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
    Divider(modifier = Modifier.padding(top = 8.dp))
}

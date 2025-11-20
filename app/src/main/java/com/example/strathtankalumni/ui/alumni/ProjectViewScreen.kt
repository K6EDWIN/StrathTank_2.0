package com.example.strathtankalumni.ui.alumni

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import coil.size.Size
import com.example.strathtankalumni.R
import com.example.strathtankalumni.data.Comment
import com.example.strathtankalumni.data.Project
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProjectViewScreen(
    project: Project,
    onBack: () -> Unit, // Kept for compatibility, handled by parent
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val comments by authViewModel.projectComments.collectAsState()
    val isLiked by authViewModel.isProjectLiked.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Collaboration Logic
    val collaborations by authViewModel.collaborations.collectAsState()
    val myCollaboration = remember(collaborations, project, currentUser) {
        collaborations.find {
            it.projectId == project.id && it.collaboratorId == currentUser?.userId
        }
    }

    val isTechProject = remember(project.projectType) {
        project.projectType in listOf("Mobile App", "Web Platform", "AI/Data Science", "IoT/Hardware", "Game Development")
    }

    // Fetch comments on load
    LaunchedEffect(project.id) {
        authViewModel.fetchCommentsForProject(project.id)
    }

    // Cleanup comments on exit
    DisposableEffect(project.id) {
        onDispose { authViewModel.clearComments() }
    }

    // --- UNIVERSAL OPENER (Handles PDFs and Websites) ---
    val openContent: (String) -> Unit = { url ->
        try {
            val validUrl = if (!url.startsWith("http")) "https://$url" else url
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open content", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ FIX: Removed Scaffold/TopAppBar.
    // This is now a pure content screen that fits inside the parent's Scaffold.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Ensure background is white
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        // 1. HERO IMAGE
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(project.imageUrl.ifEmpty { R.drawable.sample_featured })
                .crossfade(true)
                .size(Size(1024, 1024))
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.sample_featured)
        )

        Spacer(Modifier.height(20.dp))

        // 2. TITLE & TYPE
        if (project.projectType.isNotBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = project.projectType,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        Text(
            text = project.title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 32.sp,
            color = Color.Black
        )

        // 3. STATS ROW
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { authViewModel.toggleProjectLike(project.id, isLiked) }) {
                Icon(
                    if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    "Like",
                    tint = if (isLiked) Color.Red else Color.Gray
                )
            }
            Text("${project.likes}", fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(24.dp))
            Icon(Icons.Default.ChatBubbleOutline, null, tint = Color.Gray)
            Spacer(Modifier.width(6.dp))
            Text("${project.commentCount}", fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))

        // 4. DESCRIPTION
        Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            text = project.description,
            fontSize = 16.sp,
            color = Color.DarkGray,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(24.dp))

        // 5. COLLABORATION BUTTON
        if (currentUser?.userId != project.userId) {
            val (text, color, enabled) = when (myCollaboration?.status) {
                "pending" -> Triple("Request Pending", Color.Gray, false)
                "accepted" -> Triple("Open Collaboration Hub", Color(0xFF2E7D32), true)
                "declined" -> Triple("Request Declined", Color.Red, false)
                else -> Triple("Request to Join Team", MaterialTheme.colorScheme.primary, true)
            }

            Button(
                onClick = {
                    if (myCollaboration?.status == "accepted") {
                        navController.navigate(Screen.CollaborationDetail.createRoute(myCollaboration!!.id))
                    } else if (myCollaboration == null) {
                        authViewModel.requestCollaboration(project)
                        Toast.makeText(context, "Request Sent!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = color,
                    disabledContainerColor = Color.LightGray
                ),
                enabled = enabled
            ) {
                Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }

        // 6. LINKS SECTION
        if (project.projectUrl.isNotBlank() || project.githubUrl.isNotBlank()) {
            Text("Resources", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (project.projectUrl.isNotBlank()) {
                    ProjectLinkButton(
                        "Live Demo",
                        Icons.Default.Language,
                        Modifier.weight(1f)
                    ) { openContent(project.projectUrl) }
                }
                if (project.githubUrl.isNotBlank()) {
                    ProjectLinkButton(
                        "GitHub",
                        Icons.Default.Code,
                        Modifier.weight(1f)
                    ) { openContent(project.githubUrl) }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // 7. PDF DOCUMENTATION SECTION
        if (project.pdfUrl.isNotBlank()) {
            Text("Documentation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            // [Image of PDF icon]

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openContent(project.pdfUrl) }, // Opens PDF
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFFFEBEE), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Project Document",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Tap to view PDF",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open",
                        tint = Color.Gray
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // 8. TECH STACK
        val tags = (project.programmingLanguages + project.techStack + project.categories).distinct()
        if (tags.isNotEmpty()) {
            Text("Tech Stack", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = tag,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.DarkGray
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // 9. MEDIA GALLERY
        if (project.mediaImageUrls.isNotEmpty()) {
            Text("Gallery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(project.mediaImageUrls) { url ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .height(140.dp)
                            .width(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // 10. COMMENTS
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.3f))
        Spacer(Modifier.height(20.dp))
        CommentSection(project.id, comments, authViewModel, currentUser?.profilePhotoUrl)
        Spacer(Modifier.height(60.dp)) // Extra space at bottom
    }
}

@Composable
private fun ProjectLinkButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun CommentSection(
    projectId: String,
    comments: List<Comment>,
    authViewModel: AuthViewModel,
    userPhoto: String?
) {
    Column {
        Text("Comments (${comments.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        if (comments.isEmpty()) {
            Text("No comments yet. Be the first!", color = Color.Gray, fontSize = 14.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                comments.forEach { CommentItem(it) }
            }
        }
        Spacer(Modifier.height(20.dp))

        // Input
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(userPhoto ?: R.drawable.noprofile).build(),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            var text by remember { mutableStateOf("") }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Add a comment...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                authViewModel.postComment(projectId, text)
                                text = ""
                            }
                        },
                        enabled = text.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    }
}

@Composable
private fun CommentItem(comment: Comment) {
    Row(verticalAlignment = Alignment.Top) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(comment.userPhotoUrl ?: R.drawable.noprofile).build(),
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                val date = comment.createdAt ?: Date()
                Text(
                    SimpleDateFormat("MMM d", Locale.getDefault()).format(date),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Text(comment.text, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}
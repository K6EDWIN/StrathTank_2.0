package com.example.strathtankalumni.ui.alumni

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.LocalIndication
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    onBack: () -> Unit,
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val comments by authViewModel.projectComments.collectAsState()
    val isLiked by authViewModel.isProjectLiked.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // --- Helper Logic to Determine Project Type ---
    // We check if it's a "Tech" project to decide what icons/labels to show
    val isTechProject = remember(project.projectType) {
        project.projectType in listOf("Mobile App", "Web Platform", "AI/Data Science", "IoT/Hardware", "Game Development")
    }

    // --- Collaboration State ---
    val collaborations by authViewModel.collaborations.collectAsState()
    val myCollaboration = remember(collaborations, project, currentUser) {
        collaborations.find {
            it.projectId == project.id && it.collaboratorId == currentUser?.userId
        }
    }

    // --- Load comments ---
    LaunchedEffect(project.id) {
        authViewModel.fetchCommentsForProject(project.id)
    }

    // --- Clear comments on exit ---
    DisposableEffect(project.id) {
        onDispose { authViewModel.clearComments() }
    }

    val openUrl: (String) -> Unit = { url ->
        try {
            val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        project.projectType.ifEmpty { "Project Details" },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .background(Color.White)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // 1. Project Cover Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(project.imageUrl.ifEmpty { R.drawable.sample_featured })
                    .crossfade(true)
                    .size(Size(1024, 1024))
                    .allowHardware(false)
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

            // 2. Title & Type
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = project.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // 3. Description
            Spacer(Modifier.height(8.dp))
            Text(
                text = project.description,
                fontSize = 15.sp,
                color = Color.DarkGray,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(12.dp))

            // 4. likes and comments
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = { authViewModel.toggleProjectLike(project.id, isLiked) }) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.Gray
                    )
                }
                Text(project.likes.toString(), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comments", tint = Color.Gray, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(project.commentCount.toString(), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(20.dp))


            // Links
            if (project.projectUrl.isNotBlank() || project.githubUrl.isNotBlank()) {
                Text("Project Links", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                // Primary Link (Live Demo / Website / Campaign)
                if (project.projectUrl.isNotBlank()) {
                    val label = if (isTechProject) "View Live Demo" else "Visit Website / Campaign"
                    val icon = if (isTechProject) Icons.Default.Smartphone else Icons.Default.Language
                    ProjectLinkButton(label, icon) { openUrl(project.projectUrl) }
                    Spacer(Modifier.height(8.dp))
                }

                // Secondary Link (GitHub / Docs / Source)
                if (project.githubUrl.isNotBlank()) {
                    val label = if (isTechProject) "View on GitHub" else "View Source / Documents"
                    val icon = if (isTechProject) Icons.Default.Code else Icons.Default.Description
                    ProjectLinkButton(label, icon) { openUrl(project.githubUrl) }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // 6. Collaboration Button
            if (currentUser?.userId != project.userId) {
                val buttonText = when (myCollaboration?.status) {
                    "pending" -> "Request Sent"
                    "accepted" -> "View Collaboration Hub"
                    "declined" -> "Request Declined"
                    else -> "Request to Collaborate"
                }
                val isEnabled = myCollaboration?.status != "pending" && myCollaboration?.status != "declined"


                Button(
                    onClick = {
                        if (myCollaboration?.status == "accepted") {
                            navController.navigate(Screen.CollaborationDetail.createRoute(myCollaboration!!.id))
                        } else if (myCollaboration == null) {
                            authViewModel.requestCollaboration(project)
                            Toast.makeText(context, "Collaboration request sent!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (myCollaboration?.status == "accepted") Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = isEnabled
                ) {
                    Text(buttonText, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))

            //tags
            val allTags = (project.programmingLanguages + project.databaseUsed + project.techStack + project.categories).distinct()

            if (allTags.isNotEmpty()) {
                Text("Tools & Technologies", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allTags.forEach { tag -> ProjectTag(tag) }
                }
                Spacer(Modifier.height(24.dp))
            }

            // 8. Gallery / Media
            if (project.mediaImageUrls.isNotEmpty()) {
                Text("Gallery", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(project.mediaImageUrls) { imageUrl ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .size(Size(1024, 1024))
                                .allowHardware(false)
                                .build(),
                            contentDescription = "Project Media",
                            modifier = Modifier
                                .size(160.dp) // Slightly larger
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF0F0F0)),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.sample_featured)
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // 9. PDF Documentation
            if (project.pdfUrl.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = LocalIndication.current,
                            onClick = { openUrl(project.pdfUrl) }
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = "PDF",
                            tint = Color.Red,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Project Documentation",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Tap to view PDF",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // 10. Comments Section
            HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(Modifier.height(20.dp))

            CommentSection(
                projectId = project.id,
                comments = comments,
                authViewModel = authViewModel,
                currentUserPhotoUrl = currentUser?.profilePhotoUrl
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}



@Composable
private fun ProjectLinkButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
    ) {
        Icon(icon, null, Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProjectTag(tag: String) {
    Surface(
        shape = RoundedCornerShape(20.dp), // More pill-like
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.height(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(
                text = tag,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun CommentSection(
    projectId: String,
    comments: List<Comment>,
    authViewModel: AuthViewModel,
    currentUserPhotoUrl: String?
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Comments (${comments.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))

        if (comments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Forum, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No comments yet.", color = Color.Gray)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                comments.sortedBy { it.createdAt ?: Date(0) }.forEach { comment ->
                    CommentItem(comment)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        CommentInputArea(projectId, authViewModel, currentUserPhotoUrl)
    }
}

@Composable
private fun CommentInputArea(projectId: String, authViewModel: AuthViewModel, currentUserPhotoUrl: String?) {
    var commentText by remember { mutableStateOf("") }
    val isSendEnabled = commentText.isNotBlank()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(currentUserPhotoUrl.takeIf { !it.isNullOrBlank() } ?: R.drawable.noprofile)
                .size(Size(128, 128))
                .allowHardware(false)
                .build(),
            contentDescription = "User",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.noprofile)
        )

        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            placeholder = { Text("Add a comment...") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.LightGray
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (isSendEnabled) {
                            authViewModel.postComment(projectId, commentText)
                            commentText = ""
                        }
                    },
                    enabled = isSendEnabled
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (isSendEnabled) MaterialTheme.colorScheme.primary else Color.Gray
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(comment.userPhotoUrl.takeIf { !it.isNullOrBlank() } ?: R.drawable.noprofile)
                .size(Size(128, 128))
                .allowHardware(false)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.noprofile)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = comment.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                comment.createdAt?.let {
                    Text(text = timeFormatter.format(it), color = Color.Gray, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(text = comment.text, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
        }
    }
}
package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.strathtankalumni.data.Project

// NEW: Import for Intent handling
import android.content.Intent
import android.net.Uri


@Composable
fun ProjectViewScreen(
    project: Project,
    // The onBack parameter is kept but not used here, as the outer screen handles navigation.
    onBack: () -> Unit // This is no longer strictly needed in this content-only composable, but harmless.
) {
    val context = LocalContext.current
    var commentText by remember { mutableStateOf("") }
    // NOTE: In a real app, isLiked and likeCount would be managed by the ViewModel/data source
    var isLiked by remember { mutableStateOf(project.isLiked) }
    var likeCount by remember { mutableStateOf(project.likes) }

    // Helper function to open a URL in a browser
    val openUrl: (String) -> Unit = { url ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle error (e.g., no browser app found)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        // ---- Project Image (Cover) ----
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(project.imageUrl)
                    .crossfade(true)
                    .build()
            ),
            contentDescription = "Project Cover Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
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
        // ---- Likes and Comments (Omitted for brevity) ----
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = {
                isLiked = !isLiked
                likeCount += if (isLiked) 1 else -1
            }) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.Gray
                )
            }
            Text(likeCount.toString(), fontSize = 14.sp)
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = "Comments",
                tint = Color.Gray
            )
            Text(project.commentCount.toString(), fontSize = 14.sp)
        }
        Spacer(Modifier.height(20.dp))
        // ---- GitHub & Live Demo Section (MODIFIED to use helper) ----
        Text("Links", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        if (project.projectUrl.isNotBlank()) {
            Button(
                onClick = { openUrl(project.projectUrl) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("View Live Demo")
            }
            Spacer(Modifier.height(8.dp))
        }

        if (project.githubUrl.isNotBlank()) {
            OutlinedButton(
                onClick = { openUrl(project.githubUrl) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("View on GitHub")
            }
            Spacer(Modifier.height(8.dp))
        }

        OutlinedButton(
            onClick = { /* TODO: request collaboration */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Request Collaboration")
        }
        Spacer(Modifier.height(20.dp))
        // ---- Tags (Omitted for brevity) ----
        Text("Tags", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val tags = project.categories + project.programmingLanguages + project.databaseUsed + project.techStack
            items(tags.distinct()) { tag ->
                ProjectTag(tag)
            }
        }
        Spacer(Modifier.height(20.dp))

        // ---- NEW: Project Media Section ----
        Text("Project Media", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        // 1. Image Gallery
        if (project.mediaImageUrls.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(project.mediaImageUrls) { imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = "Project Media Image",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0F0F0)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        } else {
            Text("No additional media images uploaded.", color = Color.Gray)
        }
        Spacer(Modifier.height(16.dp))

        // 2. PDF Document
        if (project.pdfUrl.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openUrl(project.pdfUrl) }, // Open PDF in browser/viewer
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
        // ---- Comments (Omitted for brevity) ----
        Spacer(Modifier.height(20.dp))
        Text("Comments", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        // Placeholder Comments
        CommentItem(
            name = "Kanye West",
            comment = "This is a great project! I love the clean design and the functionality. Keep up the good work!",
            time = "2d"
        )
        Spacer(Modifier.height(8.dp))
        CommentItem(
            name = "Sam Denz",
            comment = "I agree with Kanye — this project is fantastic. The code is clean and easy to follow.",
            time = "1d"
        )
        Spacer(Modifier.height(16.dp))
        // ---- Add Comment (Omitted for brevity) ----
        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            placeholder = { Text("Add a comment...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(32.dp))
    }
}

// Helper Composable for project tags (No Change)
@Composable
fun ProjectTag(tag: String) {
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

// Helper Composable for comment items (No Change)
@Composable
fun CommentItem(name: String, comment: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Placeholder for profile photo
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text("($time)", fontSize = 12.sp, color = Color.Gray)
            }
            Text(comment, fontSize = 14.sp)
        }
    }
}
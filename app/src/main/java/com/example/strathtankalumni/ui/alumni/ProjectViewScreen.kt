package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import com.example.strathtankalumni.data.Project

// NOTE: The @OptIn(ExperimentalMaterial3Api::class) annotation is not needed here
// since TopAppBar/Scaffold are removed.

@Composable
fun ProjectViewScreen(
    project: Project,
    // The onBack parameter is kept but not used here, as the outer screen handles navigation.
    // It's good practice to keep it if internal actions might need to pop the back stack.
    onBack: () -> Unit // This is no longer strictly needed in this content-only composable, but harmless.
) {
    var commentText by remember { mutableStateOf("") }
    // NOTE: In a real app, isLiked and likeCount would be managed by the ViewModel/data source
    var isLiked by remember { mutableStateOf(project.isLiked) }
    var likeCount by remember { mutableStateOf(project.likes) }

    // MODIFICATION: REMOVED Scaffold and TopAppBar
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            // Removed padding(padding) since Scaffold is gone.
            .padding(horizontal = 16.dp) // Apply padding directly to the content
    ) {
        Spacer(Modifier.height(12.dp))
        // ---- Project Image ----
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
        // ---- Likes and Comments ----
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = {
                isLiked = !isLiked
                // NOTE: This should ultimately call a ViewModel function to update the backend
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
        // ---- GitHub section ----
        Text("GitHub", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { /* TODO: open GitHub link using project.githubUrl */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            // Disable button if URL is empty
            enabled = project.githubUrl.isNotBlank()
        ) {
            Text("View on GitHub")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { /* TODO: request collaboration */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Request Collaboration")
        }
        Spacer(Modifier.height(20.dp))
        // ---- Tags ----
        Text("Tags", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Using techStack (based on your Project.kt fields) for a generic tag list
            val tags = project.categories + project.programmingLanguages + project.databaseUsed + project.techStack
            items(tags.distinct()) { tag ->
                ProjectTag(tag)
            }
        }
        Spacer(Modifier.height(20.dp))
        // ---- Comments ----
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
        // ---- Add Comment ----
        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            placeholder = { Text("Add a comment...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(24.dp))
        // ---- Project Media ----
        Text("Project Media", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Placeholder Media
            items(listOf("Image 1", "Image 2", "File .txt")) { item ->
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item, textAlign = TextAlign.Center)
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

// Helper Composable for project tags
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

// Helper Composable for comment items
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
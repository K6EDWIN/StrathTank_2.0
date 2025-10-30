package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.background
// import androidx.compose.foundation.clickable // 👈 Not used, can be removed
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectViewScreen(
    // ✅ FIXED: Corrected function signature
    title: String,
    description: String,
    onBack: () -> Unit
) {
    // ✅ FIXED: Moved state variables inside the composable
    var commentText by remember { mutableStateOf("") }
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(123) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                } // ✅ FIXED: Removed extra parenthesis
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            // ---- Project Image Placeholder ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Project Image", color = Color.DarkGray)
            }
            Spacer(Modifier.height(16.dp))
            // ---- Project Details ----
            Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                description,
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
                Text("45", fontSize = 14.sp)
            }
            Spacer(Modifier.height(20.dp))
            // ---- GitHub section ----
            Text("GitHub", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { /* TODO: open GitHub link */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProjectTag("#AI")
                ProjectTag("#Design")
                ProjectTag("#WebApp")
            }
            Spacer(Modifier.height(20.dp))
            // ---- Comments ----
            Text("Comments", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
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
            Spacer(Modifier.height(24.dp)) // ✅ FIXED: Corrected spacing
            // ---- Project Media ----
            Text("Project Media", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(listOf("Image", "File .txt")) { item ->
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
            } // ✅ FIXED: Moved Spacer outside of items block
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ------------------ TAG & COMMENT ITEMS ------------------ //
@Composable
fun ProjectTag(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFF2F2F2), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 12.sp, color = Color.DarkGray)
    }
}

@Composable
fun CommentItem(name: String, comment: String, time: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(name.first().uppercase(), fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(comment, fontSize = 13.sp, color = Color.DarkGray)
            Text(time, fontSize = 11.sp, color = Color.Gray)
        }
    }
}
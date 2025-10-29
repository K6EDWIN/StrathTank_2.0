package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// --- Data Class ---
data class Project(
    val title: String,
    val overview: String,
    val requiredSkills: List<String>,
    val members: List<String>
)

// --- Dummy Data ---
val dummyProject = Project(
    title = "E-commerce App",
    overview = "A full-featured e-commerce application for Android, built with Jetpack Compose and Firebase. The app will include user authentication, product catalogs, a shopping cart, and a checkout process.",
    requiredSkills = listOf("Kotlin", "Jetpack Compose", "Firebase", "MVVM"),
    members = listOf("Alice", "Bob")
)

@Composable
fun ProjectDetailsScreen(navController: NavController, project: Project = dummyProject) { // Added dummyProject as default
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project.title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Left Column (Details) - Uses verticalScroll
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Overview Section (Styled as Card, matching wireframe)
                Text(
                    "Overview of the project",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.LightGray.copy(alpha = 0.5f) // Grey background
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        project.overview,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // 2. Required Skills
                Text(
                    "Required skills",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                project.requiredSkills.forEach { skill ->
                    Text("• $skill", modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(Modifier.height(24.dp))

                // 3. Current Skills (Placeholder for rating dots)
                Text(
                    "Current Skills",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Placeholder for 5 rating dots/circles
                    repeat(5) { index ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    color = if (index < 3) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 4. Current Members
                Text(
                    "Current Members",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                project.members.forEach { member ->
                    Text("• $member", modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(Modifier.height(32.dp))

                // 5. Action Buttons (JOIN THE TEAM and leave)
                Button(
                    onClick = { /* Handle Join Action */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("JOIN THE TEAM")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { /* Handle Leave Action */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("leave")
                }
            }

            // Right Column (Comments/Discussion)
            CommentsSection(modifier = Modifier.weight(0.35f))
        }
    }
}

@Composable
fun CommentsSection(modifier: Modifier = Modifier) {
    val comments = listOf("Member1: Great project!", "Member2: I'm interested in joining.", "Member3: What's the timeline?")

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Color.Gray.copy(alpha = 0.3f)) // Darker background per wireframe
            .padding(16.dp)
    ) {
        Text(
            "Comments/ Discussion section", // Matched title to wireframe
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(comments) { comment ->
                // Simple Text items matching the list style of the wireframe
                Text(
                    comment.split(":")[0], // Just display the name/member
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // You might want a field to submit a comment here, as in the original code
        Spacer(Modifier.height(16.dp))
    }
}
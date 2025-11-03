package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// --- Data Classes ---
data class Project(
    val title: String,
    val overview: String,
    val requiredSkills: List<String>,
    val members: List<String>
)

data class Comment(
    val name: String,
    val timeAgo: String,
    val text: String
)

// --- Dummy Data ---
val dummyProject = Project(
    title = "StrathTank Alumni Project",
    overview = "A collaborative project for StrathTank alumni to develop innovative solutions and network with peers.",
    requiredSkills = listOf("Skill 1", "Skill 2", "Skill 3"),
    members = listOf("User1", "User2", "User3")
)

val dummyComments = listOf(
    Comment("Ethan", "2h ago", "Excited to see this project come to life!"),
    Comment("Olivia", "3h ago", "Looking forward to collaborating with everyone."),
    Comment("Noah", "4h ago", "Let's make this project a success!")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(navController: NavController, project: Project = dummyProject) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Details", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Project Title and Overview
            Text(
                project.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                project.overview,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // --- Required Skills ---
            Text(
                "Required Skills",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 24.dp)) {
                project.requiredSkills.forEach { skill ->
                    SkillTag(skill)
                }
            }

            // --- Current Members ---
            Text(
                "Current Members",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(modifier = Modifier.padding(bottom = 24.dp)) {
                // Member Icons (Placeholders)
                project.members.forEach { _ ->
                    MemberIcon()
                }
            }

            // --- Leave Team Button ---
            Button(
                onClick = { /* Handle Leave Action */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF) // Blue color from screenshot
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text("Leave The Team", color = Color.White)
            }

            // --- Discussion ---
            Text(
                "Discussion",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                dummyComments.forEach { comment ->
                    CommentItem(comment)
                }
            }

            // Add a comment input field
            OutlinedTextField(
                value = "",
                onValueChange = { /* Handle comment input */ },
                placeholder = { Text("Add a comment...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            )
            Spacer(Modifier.height(8.dp)) // Padding before bottom nav bar starts
        }
    }
}

@Composable
fun SkillTag(skill: String) {
    Surface(
        shape = CircleShape,
        color = Color(0xFFEBEBEB), // Light grey background
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            skill,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun MemberIcon() {
    // A simple black circle to represent a member's profile picture
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(Color.Black, CircleShape)
            .padding(end = 8.dp)
    )
    Spacer(Modifier.width(4.dp))
}

@Composable
fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Placeholder for Profile Icon (Small black circle)
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color.Black, CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    comment.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    comment.timeAgo,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Text(
                comment.text,
                fontSize = 14.sp
            )
        }
    }
}

// --- Bottom Navigation Bar ---
@Composable
fun BottomNavigationBar() {
    val items = listOf(
        Pair("Home", Icons.Filled.Home),
        Pair("Projects", Icons.Filled.WorkOutline),
        Pair("Messages", Icons.Filled.MailOutline),
        Pair("Profile", Icons.Filled.Person)
    )

    NavigationBar(
        containerColor = Color.White, // White background as in the screenshot
        modifier = Modifier.height(60.dp)
    ) {
        items.forEach { (label, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 10.sp) },
                selected = label == "Projects", // 'Projects' is selected in the screenshot
                onClick = { /* Handle navigation */ }
            )
        }
    }
}

// --- Preview Function ---
@Preview(showBackground = true)
@Composable
fun PreviewProjectDetailsScreen() {
    val navController = rememberNavController()
    // Use the default theme colors for better visibility in preview
    MaterialTheme {
        ProjectDetailsScreen(navController = navController, project = dummyProject)
    }
}
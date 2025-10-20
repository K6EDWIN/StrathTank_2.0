// File: com/example/strathtankalumni/ui/alumni/AlumniProfileScreen.kt
package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel

@Composable
fun AlumniProfileScreen(
    mainNavController: NavHostController,
    alumniNavController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile picture placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Edwin Kyle", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Verified Alumni", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    alumniNavController.navigate("edit_profile")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F3F4)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Edit Profile", color = Color.Black)
            }


            Spacer(modifier = Modifier.height(24.dp))

            // Profile sections
            ProfileSection(title = "About", content = "No bio yet")
            ProfileSection(title = "Experience", content = "No experience yet")
            ProfileSection(title = "Skills & Interests", content = "No skills yet")

            Spacer(modifier = Modifier.height(8.dp))

            // Contact section
            Text("Contact", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            ContactRow(icon = Icons.Default.Email, text = "ethan.carter@email.com")
            ContactRow(icon = Icons.Default.Link, text = "Add LinkedIn URL")

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabItem("Projects", selected = selectedTab == 0) { selectedTab = 0 }
                TabItem("Collaborations", selected = selectedTab == 1) { selectedTab = 1 }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Empty projects section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8F9FA))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No projects yet", fontWeight = FontWeight.Bold)
                    Text(
                        "Share your work and grow your alumni network",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { /* TODO: Start a project */ }) {
                        Text("Start a Project")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout button
            Button(
                onClick = {
                    authViewModel.signOut()
                    mainNavController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.AlumniHome.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Logout", color = Color.White)
            }
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(content, color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
private fun TabItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) Color(0xFF5C6BC0) else Color.Gray,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.clickable { onClick() }
    )
}

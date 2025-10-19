package com.example.strathtankalumni.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.strathtankalumni.navigation.Screen

// Define the colors based on the image
private val PrimaryBlue = Color(0xFF1976D2)
private val DarkText = Color(0xFF212121)

/**
 * Data class for feature list items on the welcome screen.
 */
data class FeatureItem(
    val icon: ImageVector,
    val description: String
)

/**
 * The Welcome/Onboarding screen for first-time users.
 * It explains the app's value and offers clear navigation paths.
 */
@Composable
fun WelcomeScreen(navController: NavHostController) {
    val features = listOf(
        FeatureItem(Icons.Filled.Work, "Post Projects & Campaigns"),
        FeatureItem(Icons.Filled.People, "Collaboration & Mentorship"),
        FeatureItem(Icons.Filled.Notifications, "Messaging & Notifications"),
        FeatureItem(Icons.Filled.Check, "Verified Alumni Network")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // --- Header ---
        Text(
            text = "StrathTank Alumni",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            ),
            color = DarkText,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Connect • Collaborate • Contribute",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "A platform for alumni to share projects, mentor others, and support innovation.",
            style = MaterialTheme.typography.bodyLarge,
            color = DarkText,
            textAlign = TextAlign.Center,
            // FIX: Using the all-sides, explicit padding to resolve potential import conflict/ambiguity
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .padding(bottom = 48.dp)
        )

        // --- Features List ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            features.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = DarkText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(64.dp))

        // --- Call-to-Action Buttons ---

        // 1. Get Started Button (Primary)
        Button(
            onClick = { navController.navigate(Screen.Register.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Get Started", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Login Button (Secondary/Outlined)
        OutlinedButton(
            onClick = { navController.navigate(Screen.Login.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(PrimaryBlue))
        ) {
            Text("Login", style = MaterialTheme.typography.titleMedium, color = PrimaryBlue)
        }
    }
}

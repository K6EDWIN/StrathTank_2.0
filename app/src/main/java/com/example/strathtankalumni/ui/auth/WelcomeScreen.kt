package com.example.strathtankalumni.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.strathtankalumni.R
import com.example.strathtankalumni.navigation.Screen
import kotlinx.coroutines.delay

data class FeatureItem(
    val icon: ImageVector,
    val description: String
)

@Composable
fun WelcomeScreen(navController: NavHostController) {
    var showLogo by remember { mutableStateOf(true) }
    var showSpinner by remember { mutableStateOf(false) }

    // Coroutine to manage the sequence of screens
    LaunchedEffect(Unit) {
        delay(2000) // Show logo for 2 seconds
        showLogo = false
        showSpinner = true
        delay(1500) // Show spinner for 1.5 seconds
        showSpinner = false
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // --- Step 1: Logo Display ---
        AnimatedVisibility(
            visible = showLogo,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Image(
                painter = painterResource(id = R.drawable.strathtank_logo),
                contentDescription = "StrathTank Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // --- Step 2: Loading Spinner ---
        AnimatedVisibility(
            visible = showSpinner,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(50.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
        }

        // --- Step 3: Main Content After Spinner ---
        AnimatedVisibility(
            visible = !showLogo && !showSpinner,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            MainWelcomeContent(navController)
        }
    }
}

@Composable
fun MainWelcomeContent(navController: NavHostController) {
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

        Text(
            text = "StrathTank Alumni",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground,
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
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 48.dp)
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = { navController.navigate(Screen.Register.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Get Started", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { navController.navigate(Screen.Login.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = SolidColor(MaterialTheme.colorScheme.primary)
            )
        ) {
            Text("Login", color = MaterialTheme.colorScheme.primary)
        }
    }
}
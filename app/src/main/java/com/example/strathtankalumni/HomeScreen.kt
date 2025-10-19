package com.example.strathtankalumni.ui // <<< CORRECTED PACKAGE

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.strathtankalumni.navigation.Screen // <<< FIX: Corrected import path

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val auth = Firebase.auth
    val currentUserEmail = auth.currentUser?.email ?: "Alumni"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Feed", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF00796B), titleContentColor = Color.White), // Dark Cyan/Teal for Strathmore feel
                actions = {
                    // Logout Button
                    IconButton(onClick = {
                        // Sign out the user from Firebase
                        auth.signOut()

                        navController.navigate(Screen.Login.route) {

                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White), // White Background
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Welcome, $currentUserEmail!",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge
            )


            Spacer(Modifier.height(16.dp))
            Text(
                "This is your StrathTank Alumni feed.",
                color = Color.DarkGray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                "Postings, collaboration requests, and mentorship updates will appear here.",
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

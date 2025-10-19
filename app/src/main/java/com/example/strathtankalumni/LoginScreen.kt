package com.example.strathtankalumni.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.strathtankalumni.navigation.Screen // Corrected import path
import com.example.strathtankalumni.viewmodel.AuthViewModel
import com.example.strathtankalumni.viewmodel.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    val authState by authViewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val strathColor = Color(0xFF00796B)


    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()

                if (state.navigateToHome) {
                    navController.navigate(Screen.Home.route) {

                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
                authViewModel.resetAuthState()
            }
            is AuthState.Error -> {

                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetAuthState()
            }
            else -> {
            }
        }
    }

    // --- UI Layout ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alumni Login") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = strathColor, titleContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "StrathTank Alumni",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = strathColor,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Filled.MailOutline, contentDescription = "Email", tint = strathColor) },
                modifier = Modifier.fillMaxWidth(),
                // keyboardOptions removed
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Password", tint = strathColor) },
                modifier = Modifier.fillMaxWidth(),
                // keyboardOptions removed
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = { authViewModel.signIn(email, password) },
                // Disable button and show spinner while loading
                enabled = authState != AuthState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = strathColor)
            ) {
                if (authState == AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Log In", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Link to Registration
            TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                Text("Don't have an account? Register Now", color = strathColor)
            }
        }
    }
}

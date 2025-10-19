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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// Use the theme colors from WelcomeScreen
private val PrimaryBlue = Color(0xFF1976D2)
private val DarkText = Color(0xFF212121)

/**
 * Defines the stages for the password reset process.
 * Simplified to remove client-side code verification.
 */
private sealed class ResetStage {
    object EMAIL_INPUT : ResetStage()
    object CODE_SENT : ResetStage() // Success message stage
}

@Composable
fun ForgotPasswordScreen(navController: NavHostController) {
    val auth = Firebase.auth
    val context = LocalContext.current

    // State for the two-step process
    var resetStage by remember { mutableStateOf<ResetStage>(ResetStage.EMAIL_INPUT) }

    // Input state
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Function to handle the password reset email sending
    val sendResetEmail: () -> Unit = {
        if (email.isNotBlank()) {
            isLoading = true
            auth.sendPasswordResetEmail(email.trim())
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        // Success toast for first send or resend
                        val message = if (resetStage == ResetStage.EMAIL_INPUT) {
                            "Reset link sent to $email. Check your inbox."
                        } else {
                            "Reset link re-sent."
                        }
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        resetStage = ResetStage.CODE_SENT
                    } else {
                        Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(context, "Please enter your email.", Toast.LENGTH_SHORT).show()
        }
    }


    // --- UI Layout ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Reverting to the original padding syntax
            .padding(horizontal = 32.dp)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Reset Password",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = PrimaryBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when (resetStage) {
            // =======================================================
            // STAGE 1: EMAIL INPUT (SEND CODE)
            // =======================================================
            ResetStage.EMAIL_INPUT -> {
                Text(
                    text = "Enter your email to receive a password reset link.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkText,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Filled.MailOutline, contentDescription = "Email", tint = PrimaryBlue) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = sendResetEmail,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Send Reset Link", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            // =======================================================
            // STAGE 2: CODE SENT (SUCCESS/RESEND)
            // =======================================================
            ResetStage.CODE_SENT -> {
                Text(
                    text = "A password reset link has been sent to $email. Please check your email inbox to proceed.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DarkText,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    // FIX: Using chained padding for better compatibility
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 48.dp)
                )

                // Resend Button
                OutlinedButton(
                    onClick = sendResetEmail,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(PrimaryBlue))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Didn't receive link? Resend", color = PrimaryBlue, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Navigation back to Login (always visible)
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Back to Login", color = DarkText)
        }
    }
}

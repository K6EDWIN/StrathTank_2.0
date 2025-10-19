package com.example.strathtankalumni.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.strathtankalumni.ui.ForgotPasswordScreen // Required for the fix
import com.example.strathtankalumni.ui.HomeScreen
import com.example.strathtankalumni.ui.LoginScreen
import com.example.strathtankalumni.ui.RegistrationScreen
import com.example.strathtankalumni.ui.WelcomeScreen

/**
 * Defines all navigation routes (screens) in the application.
 */
sealed class Screen(val route: String) {
    // NEW START DESTINATION
    object Welcome : Screen("welcome_screen")
    object Login : Screen("login_screen")
    object Register : Screen("registration_screen")
    object Home : Screen("home_screen")
    object ForgotPassword : Screen("forgot_password_screen") // Added new route
}

/**
 * Sets up the navigation host for the application.
 */
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        // Set the new Welcome Screen as the initial screen
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegistrationScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.ForgotPassword.route) {
            // FIX: Calling the actual Composable now that it's implemented
            ForgotPasswordScreen(navController = navController)
        }
    }
}

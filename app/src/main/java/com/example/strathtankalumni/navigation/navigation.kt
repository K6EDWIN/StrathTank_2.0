package com.example.strathtankalumni.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.strathtankalumni.ui.auth.ForgotPasswordScreen
import com.example.strathtankalumni.ui.auth.LoginScreen
import com.example.strathtankalumni.ui.auth.RegistrationScreen
import com.example.strathtankalumni.ui.auth.WelcomeScreen
import com.example.strathtankalumni.ui.alumni.AlumniHomeScreen
import com.example.strathtankalumni.ui.admin.AdminDashboardScreen

sealed class Screen(val route: String) {

    object Welcome : Screen("welcome_screen")
    object Login : Screen("login_screen")
    object Register : Screen("registration_screen")
    object ForgotPassword : Screen("forgot_password_screen")

    object AlumniHome : Screen("alumni_home_screen")
    object AdminHome : Screen("admin_home_screen")
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        // AUTH SCREENS
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegistrationScreen(navController = navController)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }

        // ROLE-BASED HOME SCREENS
        composable(Screen.AlumniHome.route) {
            AlumniHomeScreen(navController = navController)
        }
        composable(Screen.AdminHome.route) {
            AdminDashboardScreen(navController = navController)
        }
    }
}

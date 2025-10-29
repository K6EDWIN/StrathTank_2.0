package com.example.strathtankalumni.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.strathtankalumni.ui.admin.AdminDashboardScreen
import com.example.strathtankalumni.ui.alumni.*
import com.example.strathtankalumni.ui.auth.ForgotPasswordScreen
import com.example.strathtankalumni.ui.auth.LoginScreen
import com.example.strathtankalumni.ui.auth.RegistrationScreen
import com.example.strathtankalumni.ui.auth.WelcomeScreen

// ------------------ ROUTES ------------------ //
sealed class Screen(val route: String) {
    object Welcome : Screen("welcome_screen")
    object Login : Screen("login_screen")
    object Register : Screen("registration_screen")
    object ForgotPassword : Screen("forgot_password_screen")

    // Alumni Screens
    object AlumniHome : Screen("alumni_home_screen")
    object AlumniProjects : Screen("alumni_projects_screen")
    object AlumniMessages : Screen("alumni_messages_screen")
    object AlumniProfile : Screen("alumni_profile_screen")
    object AlumniNotifications : Screen("alumni_notifications_screen")

    // Admin Screen
    object AdminHome : Screen("admin_home_screen")
}

// ------------------ MAIN APP NAVIGATION ------------------ //
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

        // ALUMNI SCREENS
        composable(Screen.AlumniHome.route) {
            AlumniGraph(mainNavController = navController)
        }

        // ADMIN SCREEN
        composable(Screen.AdminHome.route) {
            AdminDashboardScreen(navController = navController)
        }

        composable(Screen.AlumniNotifications.route) {
            AlumniNotificationsScreen(navController = navController)
        }
    }
}

// ------------------ ALUMNI NAV GRAPH ------------------ //
@Composable
fun AlumniGraph(mainNavController: NavHostController) {
    val alumniNavController = rememberNavController()
    val currentBackStackEntry by alumniNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    AlumniNavLayout(
        mainNavController = mainNavController,
        navController = alumniNavController,
        currentRoute = currentRoute
    ) { navController, paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.AlumniHome.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // ✅ Normal screens
            composable(Screen.AlumniHome.route) {
                AlumniHomeScreen(navController = navController)
            }

            // ✅ Projects screen — pass padding explicitly
            composable(Screen.AlumniProjects.route) {
                AlumniProjectsScreen(
                    navController = navController,
                    padding = paddingValues
                )
            }

            // ✅ Other screens remain unaffected
            composable(Screen.AlumniMessages.route) {
                AlumniMessagesScreen(navController = navController)
            }

            composable(Screen.AlumniProfile.route) {
                AlumniProfileScreen(
                    mainNavController = mainNavController,
                    alumniNavController = navController
                )
            }
        }
    }
}


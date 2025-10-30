package com.example.strathtankalumni.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.strathtankalumni.ui.admin.AdminDashboardScreen
import com.example.strathtankalumni.ui.alumni.*
import com.example.strathtankalumni.ui.auth.ForgotPasswordScreen
import com.example.strathtankalumni.ui.auth.LoginScreen
import com.example.strathtankalumni.ui.auth.RegistrationScreen
import com.example.strathtankalumni.ui.auth.WelcomeScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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

    // Project View Screen (with arguments)
    object ProjectView : Screen("project_view/{title}/{description}") {
        fun createRoute(title: String, description: String): String =
            "project_view/${title}/${description}"
    }

    // 👈✅ CHANGE 1: Add the new Direct Message route
    object DirectMessage : Screen("direct_message/{userName}") {
        fun createRoute(userName: String): String = "direct_message/$userName"
    }
}

// ------------------ MAIN APP NAVIGATION ------------------ //
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        // Auth
        composable(Screen.Welcome.route) { WelcomeScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegistrationScreen(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }

        // Alumni graph
        composable(Screen.AlumniHome.route) {
            AlumniGraph(mainNavController = navController)
        }

        // Admin
        composable(Screen.AdminHome.route) {
            AdminDashboardScreen(navController = navController)
        }

        // Global notifications
        composable(Screen.AlumniNotifications.route) {
            AlumniNotificationsScreen(navController = navController)
        }

        // 👈✅ CHANGE 2: Add the DM screen route HERE (outside the AlumniGraph)
        // This ensures it has no bottom navigation bar.
        composable(
            route = Screen.DirectMessage.route,
            arguments = listOf(navArgument("userName") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: "Chat"
            DirectMessageScreen(
                navController = navController,
                userName = userName
            )
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
            composable(Screen.AlumniHome.route) {
                AlumniHomeScreen(navController)
            }

            composable(Screen.AlumniProjects.route) {
                AlumniProjectsScreen(
                    navController = navController,
                    padding = paddingValues
                )
            }

            // 👈✅ CHANGE 3: Update this composable call
            // Pass mainNavController (to navigate *out* to the DM screen)
            // Pass paddingValues (to respect the Scaffold's padding)
            composable(Screen.AlumniMessages.route) {
                AlumniMessagesScreen(
                    navController = mainNavController,
                    paddingValues = paddingValues
                )
            }

            composable(Screen.AlumniProfile.route) {
                AlumniProfileScreen(
                    mainNavController = mainNavController,
                    alumniNavController = navController
                )
            }
            composable(
                route = Screen.ProjectView.route,
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType },
                    navArgument("description") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title") ?: ""
                val description = backStackEntry.arguments?.getString("description") ?: ""

                val decodedTitle = remember(title) {
                    URLDecoder.decode(title, StandardCharsets.UTF_8.toString())
                }
                val decodedDescription = remember(description) {
                    URLDecoder.decode(description, StandardCharsets.UTF_8.toString())
                }

                ProjectViewScreen(
                    title = decodedTitle,
                    description = decodedDescription,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
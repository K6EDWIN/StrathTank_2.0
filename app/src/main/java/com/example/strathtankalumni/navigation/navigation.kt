package com.example.strathtankalumni.navigation

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
import java.net.URLEncoder
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

    // Project View Screen
    object ProjectView : Screen("project_view/{title}/{description}") {
        fun createRoute(title: String, description: String): String {
            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            val encodedDescription = URLEncoder.encode(description, StandardCharsets.UTF_8.toString())
            return "project_view/$encodedTitle/$encodedDescription"
        }
    }

    // Direct Message route
    object DirectMessage : Screen("direct_message/{userName}/{otherUserId}") {
        fun createRoute(userName: String, otherUserId: String): String {
            val encodedUserName = URLEncoder.encode(userName, StandardCharsets.UTF_8.toString())
            return "direct_message/$encodedUserName/$otherUserId"
        }
    }

    // Other Profile route
    object OtherProfile : Screen("other_profile/{userId}") {
        fun createRoute(userId: String): String {
            return "other_profile/$userId"
        }
    }

    object AlumniList : Screen("alumni_list_screen")
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

        // Alumni main graph
        composable(Screen.AlumniHome.route) {
            AlumniGraph(mainNavController = navController)
        }

        // Admin
        composable(Screen.AdminHome.route) {
            AdminDashboardScreen(navController = navController)
        }

        // Direct Message Screen
        composable(
            route = Screen.DirectMessage.route,
            arguments = listOf(
                navArgument("userName") { type = NavType.StringType },
                navArgument("otherUserId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: "Chat"
            val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""

            if (otherUserId.isNotBlank()) {
                DirectMessageScreen(
                    navController = navController,
                    userName = userName,
                    otherUserId = otherUserId
                )
            } else {
                navController.popBackStack()
            }
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

            composable(Screen.AlumniNotifications.route) {
                AlumniNotificationsScreen(navController = navController)
            }

            composable(Screen.AlumniList.route) {
                AlumniListScreen(navController = navController)
            }

            composable(
                route = Screen.OtherProfile.route,
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")

                if (!userId.isNullOrBlank()) {
                    // ✅ MODIFIED: Pass mainNavController
                    OtherUserProfileScreen(
                        userId = userId,
                        navController = navController, // for back button
                        mainNavController = mainNavController // for navigating to messages
                    )
                } else {
                    navController.popBackStack()
                }
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
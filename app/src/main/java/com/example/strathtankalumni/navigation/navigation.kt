package com.example.strathtankalumni.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.strathtankalumni.viewmodel.AuthViewModel
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
    object AlumniList : Screen("alumni_list_screen")

    object AlumniCollaborations : Screen("alumni_collaborations_screen")

    // Admin Screen
    object AdminHome : Screen("admin_home_screen")

    // --- NEW Project Routes from Ian's Branch ---
    object AlumniAddProjects : Screen("alumni_add_projects_screen")

    object AlumniProjectDetail : Screen("alumni_project_detail/{projectId}") {
        fun createRoute(projectId: String) = "alumni_project_detail/$projectId"
    }



    // --- My Direct Message route ---
    object DirectMessage : Screen("direct_message/{userName}/{otherUserId}") {
        fun createRoute(userName: String, otherUserId: String): String {
            val encodedUserName = URLEncoder.encode(userName, StandardCharsets.UTF_8.toString())
            return "direct_message/$encodedUserName/$otherUserId"
        }
    }

    // --- Other Profile route ---
    object OtherProfile : Screen("other_profile/{userId}") {
        fun createRoute(userId: String): String {
            return "other_profile/$userId"
        }
    }

    // --- Collaboration Detail Route ---
    object CollaborationDetail : Screen("collaboration_detail/{collaborationId}") {
        fun createRoute(collaborationId: String) = "collaboration_detail/$collaborationId"
    }
}

// ------------------ MAIN APP NAVIGATION ------------------ //
@Composable
fun AppNavHost(navController: NavHostController) {
    // AuthViewModel is instantiated ONCE here
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        // Auth
        composable(Screen.Welcome.route) { WelcomeScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController, authViewModel) } // Pass VM
        composable(Screen.Register.route) { RegistrationScreen(navController, authViewModel) } // Pass VM

        // ✅ **** THIS IS THE FIX ****
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController, authViewModel)
        }

        // Alumni main graph
        composable(Screen.AlumniHome.route) {
            AlumniGraph(mainNavController = navController, authViewModel = authViewModel)
        }

        // Admin
        composable(Screen.AdminHome.route) {
            AdminDashboardScreen(navController = navController)
        }

        // Direct Message Screen (at the main nav level)
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
                    // authViewModel is provided by default in the screen
                )
            } else {
                navController.popBackStack()
            }
        }
    }
}

// ------------------ ALUMNI NAV GRAPH ------------------ //
@Composable
fun AlumniGraph(mainNavController: NavHostController, authViewModel: AuthViewModel) {
    val alumniNavController = rememberNavController()
    val currentBackStackEntry by alumniNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // We use the authViewModel passed from AppNavHost

    AlumniNavLayout(
        mainNavController = mainNavController,
        navController = alumniNavController,
        currentRoute = currentRoute
    ) { mainNavController, alumniNavController, paddingValues ->
        NavHost(
            navController = alumniNavController,
            startDestination = Screen.AlumniHome.route,
            modifier = Modifier.padding(paddingValues) // Apply padding here
        ) {
            composable(Screen.AlumniHome.route) {
                AlumniHomeScreen(
                    navController = alumniNavController,
                    paddingValues = paddingValues,
                    authViewModel = authViewModel
                )
            }

            composable(Screen.AlumniProjects.route) {
                AlumniProjectsScreen(
                    navController = alumniNavController,
                    padding = paddingValues,
                    authViewModel = authViewModel // Pass VM
                )
            }

            composable(Screen.AlumniMessages.route) {
                AlumniMessagesScreen(
                    navController = mainNavController, // Use main for DM
                    paddingValues = paddingValues,
                    authViewModel = authViewModel // Pass VM
                )
            }

            composable(Screen.AlumniProfile.route) {
                AlumniProfileScreen(
                    mainNavController = mainNavController,
                    alumniNavController = alumniNavController, // Pass nested controller
                    paddingValues = paddingValues,
                    authViewModel = authViewModel // Pass VM
                )
            }

            composable(Screen.AlumniNotifications.route) {
                AlumniNotificationsScreen(
                    navController = alumniNavController, // Use nested controller
                    authViewModel = authViewModel // Pass VM
                )
            }

            composable(Screen.AlumniList.route) {
                AlumniListScreen(
                    navController = alumniNavController,
                    authViewModel = authViewModel // Pass VM
                )
            }

            composable(
                route = Screen.OtherProfile.route,
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")

                if (!userId.isNullOrBlank()) {
                    OtherUserProfileScreen(
                        userId = userId,
                        navController = alumniNavController,
                        mainNavController = mainNavController, // Pass main for DM
                        authViewModel = authViewModel // Pass VM
                    )
                } else {
                    alumniNavController.popBackStack()
                }
            }

            // --- NEW: Project Detail Screen ---
            composable(
                route = Screen.AlumniProjectDetail.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId")
                AlumniProjectDetailScreen(
                    navController = alumniNavController,
                    projectId = projectId,
                    authViewModel = authViewModel
                )
            }

            // --- NEW: Add Project Screen ---
            composable(Screen.AlumniAddProjects.route) {
                AlumniAddProjectsPage(
                    navController = alumniNavController,
                    authViewModel = authViewModel
                )
            }

            // ✅ --- ADD THIS BLOCK TO FIX THE CRASH ---
            composable(Screen.AlumniCollaborations.route) {
                AlumniCollaborationsScreen(
                    navController = alumniNavController,
                    authViewModel = authViewModel
                )
            }

            // --- NEW: Collaboration Detail Screen ---
            composable(
                route = Screen.CollaborationDetail.route,
                arguments = listOf(navArgument("collaborationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val collaborationId = backStackEntry.arguments?.getString("collaborationId")
                if (collaborationId != null) {
                    CollaborationDetailScreen(
                        navController = alumniNavController,
                        collaborationId = collaborationId,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
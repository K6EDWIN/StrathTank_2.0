package com.example.strathtankalumni.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.delay

// ------------------ ROUTES ------------------ //
sealed class Screen(val route: String) {
    object Welcome : Screen("welcome_screen")
    object Login : Screen("login_screen")
    object Register : Screen("registration_screen")
    object ForgotPassword : Screen("forgot_password_screen")

    // Alumni Screens
    object AlumniHome : Screen("alumni_home_screen")
    object AlumniProjects : Screen("alumni_projects_screen")
    object AlumniAddProjects : Screen("alumni_add_projects_screen")
    object AlumniMessages : Screen("alumni_messages_screen")
    object AlumniProfile : Screen("alumni_profile_screen")
    object AlumniNotifications : Screen("alumni_notifications_screen")

    // Project Detail Screen with argument - NEW
    object AlumniProjectDetail : Screen("alumni_project_detail/{projectId}") {
        fun createRoute(projectId: String) = "alumni_project_detail/$projectId"
    }

    // Admin Screen
    object AdminHome : Screen("admin_home_screen")
}

// ------------------ MAIN APP NAVIGATION ------------------ //
@Composable
fun AppNavHost(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    var startDestination by remember { mutableStateOf<String?>(null) }

    // Check if user is already logged in on app start
    LaunchedEffect(Unit) {
        delay(100) // Small delay to ensure Firebase is initialized
        val userRole = authViewModel.checkLoggedInUser()
        startDestination = if (userRole != null) {
            when (userRole) {
                "alumni" -> Screen.AlumniHome.route
                "admin" -> Screen.AdminHome.route
                else -> Screen.Welcome.route
            }
        } else {
            Screen.Welcome.route
        }
    }

    // Show loading indicator while checking auth, then show NavHost
    if (startDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = startDestination!!
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
            //composable(Screen.ForgotPassword.route) {
            // ForgotPasswordScreen(navController = navController)
            //}

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
}

// ------------------ ALUMNI NAV GRAPH ------------------ //
@Composable
fun AlumniGraph(mainNavController: NavHostController) {
    // Get the AuthViewModel instance again for use in sub-graphs
    val authViewModel: AuthViewModel = viewModel()
    val alumniNavController = rememberNavController()

    // The AlumniNavLayout now internally uses the NavController to find the current route.
    // The previous argument 'currentRoute = currentRoute' is no longer needed and was causing an error.
    AlumniNavLayout(
        mainNavController = mainNavController,
        navController = alumniNavController,
    ) { navController, paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.AlumniHome.route,
            modifier = Modifier.padding(paddingValues)
        ) {

            composable(Screen.AlumniHome.route) {
                // FIX: Pass the required authViewModel instance
                AlumniHomeScreen(navController = navController, authViewModel = authViewModel)
            }

            // Projects screen
            composable(Screen.AlumniProjects.route) {
                AlumniProjectsScreen(
                    navController = navController,
                    padding = paddingValues,
                    authViewModel = authViewModel
                )
            }

            // Project Detail Screen
            composable(
                route = Screen.AlumniProjectDetail.route,
                arguments = listOf(navArgument("projectId") { type = NavType.StringType })
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getString("projectId")
                AlumniProjectDetailScreen(
                    navController = navController,
                    projectId = projectId
                )
            }

            //Alumni Add Projects page
            composable(Screen.AlumniAddProjects.route) {
                AlumniAddProjectsPage(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

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
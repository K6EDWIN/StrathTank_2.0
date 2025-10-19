package com.example.strathtankalumni.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


import com.example.strathtankalumni.ui.HomeScreen
import com.example.strathtankalumni.ui.LoginScreen
import com.example.strathtankalumni.ui.RegistrationScreen
import com.example.strathtankalumni.ui.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object Home : Screen("home_screen")
    object ForgotPassword : Screen("forgot_password_screen")
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route // Application now starts on the Splash screen
    ) {
        //SPLASH SCREEN
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        //AUTHENTICATION FLOW

        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(Screen.Register.route) {
            RegistrationScreen(navController = navController)
        }

        //MAIN APP FLOW

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
    }
}

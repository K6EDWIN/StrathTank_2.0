package com.example.strathtankalumni.ui.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AdminViewModel

@Composable
fun AdminGraph(
    mainNavController: NavHostController,
    adminViewModel: AdminViewModel
) {
    val adminNavController = rememberNavController()
    val currentBackStackEntry by adminNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    AdminNavLayout(
        mainNavController = mainNavController,
        navController = adminNavController,
        currentRoute = currentRoute
    ) { _, innerNavController, paddingValues ->
        NavHost(
            navController = innerNavController,
            startDestination = Screen.AdminHome.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.AdminHome.route) {
                AdminDashboardScreen(
                    navController = innerNavController,
                    paddingValues = paddingValues,
                    adminViewModel = adminViewModel
                )
            }
            composable(Screen.AdminUsers.route) {
                AdminUsersScreen(
                    navController = innerNavController,
                    paddingValues = paddingValues,
                    adminViewModel = adminViewModel
                )
            }
            composable(Screen.AdminProjects.route) {
                AdminProjectsScreen(
                    navController = innerNavController,
                    paddingValues = paddingValues,
                    adminViewModel = adminViewModel
                )
            }
            composable(Screen.AdminRequests.route) {
                AdminRequestsScreen(
                    navController = innerNavController,
                    paddingValues = paddingValues,
                    adminViewModel = adminViewModel
                )
            }
            composable(Screen.AdminReports.route) {
                AdminReportsScreen(
                    navController = innerNavController,
                    paddingValues = paddingValues,
                    adminViewModel = adminViewModel
                )
            }
        }
    }
}



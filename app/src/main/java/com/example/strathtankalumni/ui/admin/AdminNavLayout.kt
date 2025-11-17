package com.example.strathtankalumni.ui.admin

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import com.example.strathtankalumni.navigation.Screen

data class AdminNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

private val AdminNavItems = listOf(
    AdminNavItem(Screen.AdminHome.route, Icons.Filled.Dashboard, "Dashboard"),
    AdminNavItem(Screen.AdminUsers.route, Icons.Filled.Group, "Users"),
    AdminNavItem(Screen.AdminProjects.route, Icons.Filled.WorkOutline, "Projects"),
    AdminNavItem(Screen.AdminRequests.route, Icons.Filled.Assignment, "Requests"),
    AdminNavItem(Screen.AdminReports.route, Icons.Filled.Flag, "Reports")
)

@Composable
fun AdminNavLayout(
    mainNavController: NavHostController,
    navController: NavHostController,
    currentRoute: String?,
    content: @Composable (NavHostController, NavHostController, PaddingValues) -> Unit
) {
    val title = when (currentRoute) {
        Screen.AdminHome.route -> "Dashboard"
        Screen.AdminUsers.route -> "Users"
        Screen.AdminProjects.route -> "Projects"
        Screen.AdminRequests.route -> "Requests"
        Screen.AdminReports.route -> "Reports"
        else -> ""
    }

    Scaffold(
        topBar = {
            if (AdminNavItems.any { it.route == currentRoute }) {
                CenterAlignedTopAppBar(
                    title = { Text(text = title, color = MaterialTheme.colorScheme.onSurface) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF05060A) // dark background similar to Figma
                    )
                )
            }
        },
        bottomBar = {
            if (AdminNavItems.any { it.route == currentRoute }) {
                NavigationBar(containerColor = Color(0xFF05060A)) {
                    AdminNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.AdminHome.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color(0xFF111827)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        content(mainNavController, navController, paddingValues)
    }
}



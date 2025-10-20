package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import com.example.strathtankalumni.navigation.Screen


data class NavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

private val AlumniNavItems = listOf(
    NavItem(Screen.AlumniHome.route, Icons.Filled.Home, "Home"),
    NavItem(Screen.AlumniProjects.route, Icons.Filled.WorkOutline, "Projects"),
    NavItem(Screen.AlumniMessages.route, Icons.Filled.MailOutline, "Messages"),
    NavItem(Screen.AlumniProfile.route, Icons.Filled.PersonOutline, "Profile")
)

private val PrimaryBlue = Color(0xFF1976D2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniNavLayout(
    mainNavController: NavHostController,
    navController: NavHostController,
    currentRoute: String?,
    content: @Composable (NavHostController, PaddingValues) -> Unit
) {
    // Determine the title based on the current nested route
    val title = when (currentRoute) {
        Screen.AlumniHome.route -> "Home"
        Screen.AlumniProjects.route -> "Projects"
        Screen.AlumniMessages.route -> "Messages"
        Screen.AlumniProfile.route -> "Alumni Profile"
        else -> ""
    }

    Scaffold(
        topBar = {

            CenterAlignedTopAppBar(
                title = { Text(text = title, color = PrimaryBlue) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                ),
                actions = {

                    if (currentRoute == Screen.AlumniHome.route) {
                        IconButton(onClick = {
                            // Use mainNavController to go to the new screen
                            mainNavController.navigate(Screen.AlumniNotifications.route)
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = PrimaryBlue
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                AlumniNavItems.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.AlumniHome.route) { saveState = true }
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
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { paddingValues ->

        content(navController, paddingValues)
    }
}
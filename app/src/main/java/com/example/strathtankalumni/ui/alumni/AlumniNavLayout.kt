package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniNavLayout(
    mainNavController: NavHostController,
    navController: NavHostController,
    currentRoute: String?,
    content: @Composable (NavHostController, PaddingValues) -> Unit
) {
    val title = when (currentRoute) {
        Screen.AlumniHome.route -> "Home"
        Screen.AlumniProjects.route -> "Projects"
        Screen.AlumniMessages.route -> "Messages"
        Screen.AlumniProfile.route -> "Alumni Profile"
        Screen.AlumniList.route -> ""
        else -> ""
    }

    Scaffold(
        topBar = {
            if (AlumniNavItems.any { it.route == currentRoute }) {
                CenterAlignedTopAppBar(
                    title = { Text(text = title, color = MaterialTheme.colorScheme.primary) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    ),
                    actions = {
                        if (currentRoute == Screen.AlumniHome.route) {

                            IconButton(onClick = {
                                navController.navigate(Screen.AlumniList.route)
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.People,
                                    contentDescription = "Find Alumni",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // ✅ MODIFIED: Use 'navController' here
                            IconButton(onClick = {
                                navController.navigate(Screen.AlumniNotifications.route)
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (AlumniNavItems.any { it.route == currentRoute }) {
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
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        content(navController, paddingValues)
    }
}
package com.example.strathtankalumni.ui.alumni

import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign // Import TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.strathtankalumni.R
import com.example.strathtankalumni.data.Project
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel
import com.example.strathtankalumni.viewmodel.ProjectsListState
import java.util.Calendar // Import for date comparison
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniProjectsScreen(
    navController: NavHostController,
    padding: PaddingValues,
    authViewModel: AuthViewModel
) {
    // State to manage the search query
    var searchQuery by remember { mutableStateOf("") }
    // State to manage the selected tab (0: All, 1: Latest, 2: My Projects)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("All", "Latest", "My Projects")

    // Collect the projects state and current user from the ViewModel
    val projectsState by authViewModel.allProjectsState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Fetch projects and current user data when the screen is first composed
    LaunchedEffect(Unit) {
        authViewModel.fetchAllProjects()
        authViewModel.fetchCurrentUser() // Ensure current user data is fetched for "My Projects" filter
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AlumniAddProjects.route)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Project"
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Projects") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Segmented Tabs for Filtering
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp) // Adjusted spacing
            ) {
                tabTitles.forEachIndexed { index, title ->
                    OutlinedButton(
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier.weight(1f),
                        shape = when (index) {
                            0 -> RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp, topEnd = 0.dp, bottomEnd = 0.dp)
                            tabTitles.lastIndex -> RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 50.dp, bottomEnd = 50.dp)
                            else -> RoundedCornerShape(0.dp)
                        },
                        colors = if (index == selectedTabIndex) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        },
                        // Only add border to unselected buttons for a continuous look
                        border = if (index != selectedTabIndex) ButtonDefaults.outlinedButtonBorder else null,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Text(title)
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic content based on ProjectsListState
            when (projectsState) {
                is ProjectsListState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ProjectsListState.Error -> {
                    Text(
                        text = "Error: ${(projectsState as ProjectsListState.Error).message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is ProjectsListState.Success -> {
                    val allProjects = (projectsState as ProjectsListState.Success).projects
                    val currentUserId = currentUser?.userId

                    // Step 1: Apply Tab Filter
                    val tabFilteredProjects = when (selectedTabIndex) {
                        1 -> allProjects.filter { isProjectToday(it) } // Latest (Today's Projects)
                        2 -> allProjects.filter { it.userId == currentUserId && currentUserId != null } // My Projects
                        else -> allProjects // All Projects (Index 0)
                    }

                    // Step 2: Apply Search Filter
                    val finalFilteredProjects = tabFilteredProjects.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                                it.description.contains(searchQuery, ignoreCase = true)
                    }

                    if (finalFilteredProjects.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val message = if (searchQuery.isBlank()) {
                                when (selectedTabIndex) {
                                    1 -> "No projects were created today."
                                    2 -> if (currentUserId == null) "Log in to see your projects." else "You haven't posted any projects yet."
                                    else -> "No projects found in the database."
                                }
                            } else {
                                "No projects match your search in this category."
                            }
                            Text(
                                text = message,
                                color = Color(0xFF666666),
                                fontSize = 16.sp,
                                // FIX: Use TextAlign.Center instead of Alignment.Center.toString()
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Display the list of projects
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                // Dynamic Heading
                                Text("${tabTitles[selectedTabIndex]}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            items(finalFilteredProjects, key = { it.id }) { project ->
                                ProjectCard(
                                    project = project,
                                    onClick = {
                                        // Navigate to detail screen on click
                                        navController.navigate(Screen.AlumniProjectDetail.createRoute(project.id))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to check if a project's creation date is today
private fun isProjectToday(project: Project): Boolean {
    val projectDate: Date? = project.createdAt
    if (projectDate == null) return false

    val projectCalendar = Calendar.getInstance().apply { time = projectDate }
    val todayCalendar = Calendar.getInstance()

    return projectCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
            projectCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)
}

// FilterChip composable (Kept for reference, though unused for the main tabs)
@Composable
fun FilterChip(label: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFEFF0F2),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize = 14.sp
        )
    }
}


// Updated ProjectCard composable to use Project data class and be clickable
@Composable
fun ProjectCard(project: Project, onClick: () -> Unit) {
    // Add clickable modifier to the outer Row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // Make the card clickable
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display project photo (using imageUrl)
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    // Use project's imageUrl for photo, falling back to a sample image
                    .data(project.imageUrl.ifEmpty { R.drawable.sample_museum })
                    .crossfade(true)
                    .build()
            ),
            contentDescription = "Project Photo: ${project.title}",
            modifier = Modifier
                .size(80.dp)
                .padding(end = 12.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Display project title
            Text(
                text = project.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
            // Display project description snippet
            Text(
                text = project.description,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 2
            )
        }
    }
    Divider(modifier = Modifier.padding(top = 8.dp)) // Add a divider for better separation
}

// Minimal Project Detail Screen Placeholder
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniProjectDetailScreen(
    navController: NavHostController,
    projectId: String?
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Project ID: $projectId", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Project detail screen placeholder", color = Color.Gray)
            }
        }
    }
}
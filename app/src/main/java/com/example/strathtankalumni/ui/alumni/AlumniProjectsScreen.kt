package com.example.strathtankalumni.ui.alumni

import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.strathtankalumni.R
import com.example.strathtankalumni.data.Project
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel
import com.example.strathtankalumni.viewmodel.ProjectDetailState
import com.example.strathtankalumni.viewmodel.ProjectsListState
import java.util.Calendar
import java.util.Date

// =====================================================================
// ALUMNI PROJECTS SCREEN (List + Tabs + Search + Navigation)
// =====================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniProjectsScreen(
    navController: NavHostController,
    padding: PaddingValues,
    authViewModel: AuthViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("All", "Latest", "My Projects")

    val projectsState by authViewModel.allProjectsState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Fetch projects when first opened
    LaunchedEffect(Unit) {
        authViewModel.fetchAllProjects()
        authViewModel.fetchCurrentUser()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AlumniAddProjects.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Project")
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

            // Tabs for filtering
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    OutlinedButton(
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier.weight(1f),
                        shape = when (index) {
                            0 -> RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp)
                            tabTitles.lastIndex -> RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
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
                        border = if (index != selectedTabIndex) ButtonDefaults.outlinedButtonBorder else null,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Text(title)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Handle project state
            when (projectsState) {
                is ProjectsListState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is ProjectsListState.Error -> Text(
                    text = "Error: ${(projectsState as ProjectsListState.Error).message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )

                is ProjectsListState.Success -> {
                    val allProjects = (projectsState as ProjectsListState.Success).projects
                    val currentUserId = currentUser?.userId

                    val tabFilteredProjects = when (selectedTabIndex) {
                        1 -> allProjects.filter { isProjectToday(it) } // Latest
                        2 -> allProjects.filter { it.userId == currentUserId && currentUserId != null } // My projects
                        else -> allProjects
                    }

                    val finalFilteredProjects = tabFilteredProjects.filter {
                        it.title.contains(searchQuery, ignoreCase = true) ||
                                it.description.contains(searchQuery, ignoreCase = true)
                    }

                    if (finalFilteredProjects.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            val message = if (searchQuery.isBlank()) {
                                when (selectedTabIndex) {
                                    1 -> "No projects were created today."
                                    2 -> if (currentUserId == null)
                                        "Log in to see your projects."
                                    else "You haven't posted any projects yet."
                                    else -> "No projects found."
                                }
                            } else "No projects match your search."

                            Text(
                                text = message,
                                color = Color(0xFF666666),
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text(
                                    "${tabTitles[selectedTabIndex]}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            items(finalFilteredProjects, key = { it.id }) { project ->
                                ProjectCard(
                                    project = project,
                                    onClick = {
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

// =====================================================================
// HELPER COMPOSABLES
// =====================================================================
private fun isProjectToday(project: Project): Boolean {
    val projectDate: Date? = project.createdAt
    if (projectDate == null) return false

    val projectCalendar = Calendar.getInstance().apply { time = projectDate }
    val todayCalendar = Calendar.getInstance()

    return projectCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
            projectCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun ProjectCard(project: Project, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = project.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
            Text(
                text = project.description,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 2
            )
        }
    }
    Divider(modifier = Modifier.padding(top = 8.dp))
}

// =====================================================================
// PROJECT DETAIL SCREEN (Uses AuthViewModel)
// =====================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniProjectDetailScreen(
    navController: NavHostController,
    projectId: String?,
    authViewModel: AuthViewModel
) {
    val projectDetailState by authViewModel.projectDetailState.collectAsState()

    LaunchedEffect(projectId) {
        if (!projectId.isNullOrBlank()) {
            authViewModel.fetchProjectById(projectId)
            authViewModel.fetchProjectComments(projectId)
            authViewModel.checkIfProjectIsLiked(projectId)
        }
    }

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
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = projectDetailState) {
                is ProjectDetailState.Loading, ProjectDetailState.Idle -> CircularProgressIndicator()
                is ProjectDetailState.Error -> Text(
                    text = "Error loading project: ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )

                is ProjectDetailState.Success -> ProjectViewScreen(
                    project = state.project,
                    onBack = { navController.popBackStack() },
                    authViewModel = authViewModel
                )
            }
        }
    }
}

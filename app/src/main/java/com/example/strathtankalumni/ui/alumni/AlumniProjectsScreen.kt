package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.strathtankalumni.R
import com.example.strathtankalumni.data.Project
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel
import com.example.strathtankalumni.viewmodel.ProjectDetailState
import com.example.strathtankalumni.viewmodel.ProjectsListState
import java.util.Calendar
import java.util.Date

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

    LaunchedEffect(Unit) {
        authViewModel.fetchAllProjects()
        authViewModel.fetchCurrentUser()
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB), // Light grey background
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.AlumniAddProjects.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Project") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // --- TOP CONTROLS SECTION ---
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                // 1. Modern Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search projects...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF0F2F5),
                        unfocusedContainerColor = Color(0xFFF0F2F5),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Modern Segmented Tabs
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = index == selectedTabIndex,
                            onClick = { selectedTabIndex = index },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = tabTitles.size),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text(label)
                        }
                    }
                }
            }

            // --- CONTENT SECTION ---
            when (projectsState) {
                is ProjectsListState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ProjectsListState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Error loading projects",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is ProjectsListState.Success -> {
                    val allProjects = (projectsState as ProjectsListState.Success).projects
                    val currentUserId = currentUser?.userId

                    // Filtering Logic
                    val filteredProjects = allProjects.filter { project ->
                        // Tab Filter
                        val matchesTab = when (selectedTabIndex) {
                            1 -> isProjectToday(project)
                            2 -> project.userId == currentUserId
                            else -> true
                        }
                        // Search Filter
                        val matchesSearch = project.title.contains(searchQuery, ignoreCase = true) ||
                                project.description.contains(searchQuery, ignoreCase = true)

                        matchesTab && matchesSearch
                    }

                    if (filteredProjects.isEmpty()) {
                        EmptyProjectState(selectedTabIndex, searchQuery, currentUserId != null)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredProjects, key = { it.id }) { project ->
                                AlumniProjectItemCard(
                                    project = project,
                                    onClick = {
                                        navController.navigate(Screen.AlumniProjectDetail.createRoute(project.id))
                                    }
                                )
                            }
                            // Add extra space at bottom for FAB
                            item { Spacer(modifier = Modifier.height(60.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// --- IMPROVED CARD COMPONENT ---
@Composable
fun AlumniProjectItemCard(project: Project, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Large Cover Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(project.imageUrl.ifEmpty { R.drawable.sample_museum })
                    .crossfade(true)
                    .size(Size(800, 600)) // Optimized size request
                    .build(),
                placeholder = painterResource(id = R.drawable.sample_museum),
                error = painterResource(id = R.drawable.sample_museum),
                contentDescription = project.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Project Type Tag
                if (project.projectType.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = project.projectType,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "View Details",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// --- EMPTY STATE COMPONENT ---
@Composable
fun EmptyProjectState(tabIndex: Int, query: String, isLoggedIn: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FolderOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))

        val message = if (query.isNotBlank()) {
            "No projects match \"$query\""
        } else {
            when (tabIndex) {
                1 -> "No projects created today"
                2 -> if (isLoggedIn) "You haven't posted any projects" else "Log in to see your projects"
                else -> "No projects available"
            }
        }

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

// --- UTILS ---
private fun isProjectToday(project: Project): Boolean {
    val projectDate: Date? = project.createdAt
    if (projectDate == null) return false
    val projectCalendar = Calendar.getInstance().apply { time = projectDate }
    val todayCalendar = Calendar.getInstance()
    return projectCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
            projectCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)
}

// --- DETAIL SCREEN WRAPPER ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniProjectDetailScreen(
    navController: NavHostController,
    projectId: String?,
    authViewModel: AuthViewModel
) {
    val projectDetailState by authViewModel.projectDetailState.collectAsState()

    LaunchedEffect(projectId) {
        if (projectId != null) {
            authViewModel.fetchProjectById(projectId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
                is ProjectDetailState.Loading, ProjectDetailState.Idle -> {
                    CircularProgressIndicator()
                }
                is ProjectDetailState.Error -> {
                    Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
                is ProjectDetailState.Success -> {
                    ProjectViewScreen(
                        project = state.project,
                        onBack = { navController.popBackStack() },
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
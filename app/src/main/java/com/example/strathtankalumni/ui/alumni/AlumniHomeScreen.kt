package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.strathtankalumni.R
import com.example.strathtankalumni.data.Project
import com.example.strathtankalumni.data.User
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel
import com.example.strathtankalumni.viewmodel.ProjectsListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniHomeScreen(
    navController: NavHostController,
    paddingValues: PaddingValues,
    authViewModel: AuthViewModel = viewModel()
) {
    val projectsState by authViewModel.allProjectsState.collectAsState()
    val alumniList by authViewModel.alumniList.collectAsState()

    // Fetch all data needed for the dashboard
    LaunchedEffect(Unit) {
        authViewModel.fetchAllProjects()
        authViewModel.fetchAllAlumni()
    }

    val featuredProjects = remember(projectsState) {
        if (projectsState is ProjectsListState.Success) {
            (projectsState as ProjectsListState.Success).projects.filter { it.isFeatured }
        } else {
            emptyList()
        }
    }

    val latestProjects = remember(projectsState) {
        if (projectsState is ProjectsListState.Success) {
            (projectsState as ProjectsListState.Success).projects.sortedByDescending { it.createdAt }
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Home", fontWeight = FontWeight.Bold) },
                actions = {
                    // ✅ HERE IS YOUR NEW ICON
                    IconButton(onClick = {
                        navController.navigate(Screen.AlumniCollaborations.route)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "My Collaborations"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Use padding from Scaffold
                .padding(bottom = paddingValues.calculateBottomPadding()), // Use padding from NavHost
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {

            // --- Suggested Users Section ---
            item {
                SuggestedUsersSection(
                    users = alumniList,
                    onUserClick = { userId ->
                        navController.navigate(Screen.OtherProfile.createRoute(userId))
                    }
                )
                Spacer(Modifier.height(24.dp))
            }

            // --- Featured Projects Section ---
            item {
                FeaturedProjectsSection(
                    projects = featuredProjects,
                    onProjectClick = { projectId ->
                        navController.navigate(Screen.AlumniProjectDetail.createRoute(projectId))
                    }
                )
                Spacer(Modifier.height(24.dp))
            }

            // --- Latest Projects Section ---
            item {
                Text(
                    text = "Latest Projects",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(16.dp))
            }

            if (latestProjects.isEmpty()) {
                item {
                    Text(
                        text = "No projects found.",
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                items(latestProjects, key = { it.id }) { project ->
                    // Re-using the ProjectCard from AlumniProjectsScreen.kt
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

// --- Composable for "Suggested for you" Section ---

@Composable
private fun SuggestedUsersSection(
    users: List<User>,
    onUserClick: (String) -> Unit
) {
    val shuffledUsers = remember(users) { users.shuffled().take(10) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Suggested for you",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(shuffledUsers, key = { it.userId }) { user ->
                SuggestedUserCard(
                    user = user,
                    onClick = { onUserClick(user.userId) }
                )
            }
        }
    }
}

@Composable
private fun SuggestedUserCard(
    user: User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.profilePhotoUrl.ifEmpty { R.drawable.noprofile })
                    .crossfade(true)
                    .size(Size(256, 256)) // ✅ CRASH FIX
                    .allowHardware(false) // ✅ CRASH FIX
                    .build(),
                contentDescription = "Profile Photo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.noprofile)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "${user.firstName} ${user.lastName}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = user.degree.ifBlank { "Alumni" },
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("View Profile", fontSize = 12.sp)
            }
        }
    }
}

// --- Composable for "Featured Projects" Section ---

@Composable
private fun FeaturedProjectsSection(
    projects: List<Project>,
    onProjectClick: (String) -> Unit
) {
    if (projects.isEmpty()) return // Don't show the section if there are no featured projects

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Featured Projects",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(projects, key = { it.id }) { project ->
                FeaturedProjectCard(
                    project = project,
                    onClick = { onProjectClick(project.id) }
                )
            }
        }
    }
}

@Composable
private fun FeaturedProjectCard(
    project: Project,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(project.imageUrl.ifEmpty { R.drawable.sample_featured })
                    .crossfade(true)
                    .size(Size(512, 512)) // ✅ CRASH FIX
                    .allowHardware(false) // ✅ CRASH FIX
                    .build(),
                contentDescription = project.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.sample_featured)
            )
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
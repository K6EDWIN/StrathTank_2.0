package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    paddingValues: PaddingValues, // Contains height of bottom navbar
    authViewModel: AuthViewModel = viewModel()
) {
    val projectsState by authViewModel.allProjectsState.collectAsState()
    val alumniList by authViewModel.alumniList.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

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
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9FAFB) // Light grey background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            // Padding handling for Bottom Navbar
            contentPadding = PaddingValues(
                bottom = paddingValues.calculateBottomPadding() + 24.dp,
                top = 0.dp
            )
        ) {

            // --- 1. Welcome Header ---
            item {
                WelcomeHeader(
                    userName = currentUser?.firstName ?: "Alumni",
                    photoUrl = currentUser?.profilePhotoUrl
                )
            }

            // --- 2. Featured Projects ---
            if (featuredProjects.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Featured Projects",
                        onSeeAll = {
                            // Optional: Navigate to a "Featured" list if you have one
                        }
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(featuredProjects, key = { it.id }) { project ->
                            FeaturedProjectCard(
                                project = project,
                                onClick = { navController.navigate(Screen.AlumniProjectDetail.createRoute(project.id)) }
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            // --- 3. Suggested Connections ---
            if (alumniList.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "People you may know",
                        onSeeAll = {
                            // Navigate to AlumniListScreen
                            navController.navigate(Screen.AlumniList.route)
                        }
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(alumniList.shuffled().take(8), key = { it.userId }) { user ->
                            SuggestedUserCard(
                                user = user,
                                onClick = { navController.navigate(Screen.OtherProfile.createRoute(user.userId)) }
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            // --- 4. Latest Projects ---
            item {
                SectionHeader(title = "Latest Projects", showSeeAll = false)
            }

            if (latestProjects.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No projects found.", color = Color.Gray)
                    }
                }
            } else {
                items(latestProjects, key = { it.id }) { project ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        // ✅ FIX: Renamed to LatestProjectItem to avoid conflict
                        LatestProjectItem(
                            project = project,
                            onClick = { navController.navigate(Screen.AlumniProjectDetail.createRoute(project.id)) }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENTS
// ==========================================

@Composable
private fun WelcomeHeader(userName: String, photoUrl: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Welcome back,",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUrl.takeIf { !it.isNullOrBlank() } ?: R.drawable.noprofile)
                    .crossfade(true)
                    .size(Size(128, 128))
                    .build(),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.LightGray, CircleShape),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.noprofile)
            )
            // Notification dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
                    .align(Alignment.TopEnd)
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    showSeeAll: Boolean = true,
    onSeeAll: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (showSeeAll) {
            Text(
                text = "See All",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onSeeAll() }
            )
        }
    }
}

@Composable
private fun SuggestedUserCard(user: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.profilePhotoUrl.ifEmpty { R.drawable.noprofile })
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F3F4)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${user.firstName} ${user.lastName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = user.degree.ifBlank { "Alumni" },
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.height(32.dp).fillMaxWidth(),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("View", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun FeaturedProjectCard(project: Project, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.height(160.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(project.imageUrl.ifEmpty { R.drawable.sample_featured })
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 100f
                        )
                    )
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Featured",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = project.projectType,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ✅ FIX: Renamed to LatestProjectItem to prevent conflict with other files
@Composable
fun LatestProjectItem(
    project: Project,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(project.imageUrl.ifEmpty { R.drawable.sample_featured })
                    .crossfade(true)
                    .build(),
                contentDescription = project.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Tags/Type
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = project.projectType,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
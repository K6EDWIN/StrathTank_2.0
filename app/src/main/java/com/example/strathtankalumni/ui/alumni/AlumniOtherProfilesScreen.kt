package com.example.strathtankalumni.ui.alumni

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FolderOpen
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.strathtankalumni.R
import com.example.strathtankalumni.data.ExperienceItem
import com.example.strathtankalumni.data.Project
import com.example.strathtankalumni.data.User
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel
import com.example.strathtankalumni.viewmodel.ProjectsListState
import androidx.compose.foundation.layout.ExperimentalLayoutApi

// Enum for connection logic
enum class ConnectionStatus {
    NONE,
    PENDING_SENT,
    PENDING_RECEIVED,
    ACCEPTED
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OtherUserProfileScreen(
    userId: String,
    navController: NavHostController,
    mainNavController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var userProfile by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserId = currentUser?.userId
    val connections by authViewModel.connections.collectAsState()

    val projectsState by authViewModel.allProjectsState.collectAsState()

    LaunchedEffect(userId) {
        isLoading = true
        authViewModel.fetchUserById(userId) { user ->
            userProfile = user
            isLoading = false
        }
        authViewModel.loadConnections()
        authViewModel.fetchAllAlumni()
        authViewModel.fetchAllProjects()
    }

    val userProjects = remember(projectsState, userId) {
        if (projectsState is ProjectsListState.Success) {
            (projectsState as ProjectsListState.Success).projects.filter { it.userId == userId }
        } else {
            emptyList()
        }
    }

    val connection = remember(connections, userId) {
        connections.find { it.participantIds.contains(userId) }
    }

    val connectionStatus = remember(connection, currentUserId) {
        when {
            connection == null -> ConnectionStatus.NONE
            connection.status == "accepted" -> ConnectionStatus.ACCEPTED
            connection.status == "pending" && connection.senderId == currentUserId -> ConnectionStatus.PENDING_SENT
            connection.status == "pending" && connection.senderId == userId -> ConnectionStatus.PENDING_RECEIVED
            else -> ConnectionStatus.NONE
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Alumni Profile", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (userProfile == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("User not found.", color = Color.Gray)
            }
        } else {
            val userData = userProfile!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // --- HEADER SECTION ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(top = 24.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(userData.profilePhotoUrl.takeIf { !it.isNullOrBlank() } ?: R.drawable.noprofile)
                            .crossfade(true)
                            .size(Size(256, 256))
                            .allowHardware(false)
                            .build(),
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F3F4)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.noprofile)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${userData.firstName} ${userData.lastName}".trim(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val classMajorText = buildString {
                        if (userData.graduationYear.isNotBlank()) append("Class of ${userData.graduationYear}")
                        if (userData.degree.isNotBlank()) {
                            if (this.isNotEmpty()) append(" • ")
                            append(userData.degree)
                        }
                    }.ifEmpty { "Member" }

                    Text(
                        text = classMajorText,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    if(userData.country.isNotBlank()) {
                        Text(
                            text = userData.country,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- ACTION BUTTONS ---
                    Column(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (connectionStatus) {
                            ConnectionStatus.NONE -> {
                                Button(
                                    onClick = { authViewModel.sendConnectionRequest(userData) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Connect") }
                            }
                            ConnectionStatus.PENDING_SENT -> {
                                OutlinedButton(
                                    onClick = { },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = false,
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Request Sent") }
                            }
                            ConnectionStatus.PENDING_RECEIVED -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { authViewModel.updateConnectionStatus(connection!!, "accepted") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Accept") }

                                    OutlinedButton(
                                        onClick = { authViewModel.updateConnectionStatus(connection!!, "declined") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) { Text("Decline") }
                                }
                            }
                            ConnectionStatus.ACCEPTED -> {
                                Button(
                                    onClick = {
                                        mainNavController.navigate(
                                            Screen.DirectMessage.createRoute(
                                                userName = "${userData.firstName} ${userData.lastName}",
                                                otherUserId = userData.userId
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Message") }
                            }
                        }

                        val linkedIn = userData.linkedinUrl
                        if (!linkedIn.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkedIn))
                                        context.startActivity(intent)
                                    } catch (e: Exception) { /* Handle error */ }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A66C2))
                            ) { Text("LinkedIn Profile") }
                        }
                    }
                }

                // --- DETAILS SECTION ---
                Column(modifier = Modifier.padding(16.dp)) {

                    SectionCard {
                        ProfileSection("About", userData.about.takeIf { !it.isNullOrBlank() } ?: "No bio available.")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    SectionCard {
                        Text("Experience", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        if (userData.experience.isEmpty()) {
                            Text("No experience added.", color = Color.Gray, fontSize = 14.sp)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                userData.experience.forEach { item -> ExperienceItemView(item) }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    SectionCard {
                        Text("Skills", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (userData.skills.isNullOrEmpty()) {
                            Text("No skills listed.", color = Color.Gray, fontSize = 14.sp)
                        } else {
                            ViewOnlyFlowRow(items = userData.skills)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                        Text("Contact", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        ContactRow(icon = Icons.Default.Email, text = userData.email)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Projects",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )

                    if (userProjects.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.LightGray
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "${userData.firstName} has no projects yet.",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        userProjects.forEach { project ->
                            // ✅ FIX: Using Unique Name "UserProfileProjectCard"
                            UserProfileProjectCard(
                                project = project,
                                onClick = {
                                    navController.navigate(Screen.AlumniProjectDetail.createRoute(project.id))
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// --- HELPER COMPONENTS ---

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun ProfileSection(title: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(content, color = Color.DarkGray, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color.DarkGray, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ViewOnlyFlowRow(items: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { skill ->
            SuggestionChip(
                onClick = { },
                label = { Text(skill) },
                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFF0F2F5))
            )
        }
    }
}

@Composable
private fun ExperienceItemView(item: ExperienceItem) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Default.Business,
            null,
            modifier = Modifier.size(32.dp).background(Color(0xFFF0F2F5), CircleShape).padding(6.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(item.role, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(item.companyName, fontSize = 13.sp, color = Color.DarkGray)
            Text("${item.startDate} - ${if (item.isCurrent) "Present" else item.endDate}", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

// ✅ FIX: Renamed Component to avoid "Conflicting Overloads"
@Composable
private fun UserProfileProjectCard(project: Project, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(project.imageUrl.ifBlank { R.drawable.sample_featured })
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.sample_featured)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(project.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(project.description, fontSize = 14.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(12.dp))
                Text("View Project", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
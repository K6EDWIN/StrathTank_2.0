// megre branch ]/StrathTank_2.0-merge/app/src/main/java/com/example/strathtankalumni/ui/alumni/AlumniOtherProfilesScreen.kt
package com.example.strathtankalumni.ui.alumni

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Corrected import
import androidx.compose.material.icons.filled.Business // Corrected import
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Message // Corrected import
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.strathtankalumni.R
import com.example.strathtankalumni.data.User
import com.example.strathtankalumni.data.Connection
// import com.example.strathtankalumni.data.ConnectionStatus // This enum doesn't exist, remove it
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel
// ✅ 1. ADD THIS IMPORT
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.example.strathtankalumni.data.ExperienceItem

// This Project class is a local-only placeholder, which is fine.
data class Project(
    val title: String = "",
    val description: String = "",
    val imageUrl: String = ""
)

// Re-creating this enum locally as it's not in the data layer
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
    var userProjects by remember { mutableStateOf<List<Project>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val currentUser by authViewModel.currentUser.collectAsState()
    val currentUserId = currentUser?.userId

    val connections by authViewModel.connections.collectAsState()

    LaunchedEffect(userId) {
        isLoading = true
        authViewModel.fetchUserById(userId) { user ->
            userProfile = user
            isLoading = false
        }

        authViewModel.loadConnections()
        authViewModel.fetchAllAlumni()

        // This is placeholder data, as per your file
        userProjects = listOf(
            Project(
                "AI-Powered Recommendation System",
                "Developed an AI-driven recommendation engine for e-commerce platforms, enhancing user engagement and sales.",
                "https_placeholder_image_url_1"
            ),
            Project(
                "Mobile App for Fitness Tracking",
                "Designed and built a mobile application for tracking fitness activities, providing personalized workout plans and progress analysis.",
                "https_placeholder_image_url_2"
            )
        )
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
        topBar = {
            TopAppBar(
                title = { Text("Alumni Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (userProfile == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("User not found.")
            }
        } else {
            val userData = userProfile!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(userData.profilePhotoUrl.takeIf { !it.isNullOrBlank() }
                                ?: R.drawable.noprofile)
                            .crossfade(true)
                            .allowHardware(false) // Fix for large image crash
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
                        if (userData.graduationYear.isNotBlank()) {
                            append("Class of ${userData.graduationYear}")
                        }
                        if (userData.degree.isNotBlank()) {
                            if (this.isNotEmpty()) append(" | ")
                            append(userData.degree)
                        }
                    }.ifEmpty { "No education details provided" }

                    Text(
                        text = classMajorText,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Text(
                        text = userData.country.takeIf { !it.isNullOrBlank() }
                            ?: "No location provided",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (connectionStatus) {
                            ConnectionStatus.NONE -> {
                                Button(
                                    onClick = {
                                        authViewModel.sendConnectionRequest(userData)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Connect")
                                }
                            }
                            ConnectionStatus.PENDING_SENT -> {
                                Button(
                                    onClick = { },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = false,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        disabledContainerColor = Color.LightGray
                                    )
                                ) {
                                    Text("Request Sent")
                                }
                            }
                            ConnectionStatus.PENDING_RECEIVED -> {
                                Button(
                                    onClick = { authViewModel.updateConnectionStatus(connection!!, "accepted") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Accept Request")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { authViewModel.updateConnectionStatus(connection!!, "declined") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Decline")
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
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Message")
                                }
                            }
                        }

                        val linkedIn = userData.linkedinUrl
                        val hasLinkedIn = !linkedIn.isNullOrBlank()

                        if (hasLinkedIn) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(linkedIn)
                                        )
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Handle invalid URL
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0A66C2)
                                )
                            ) {
                                Text("View LinkedIn")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    ProfileSection(
                        "About",
                        userData.about.takeIf { !it.isNullOrBlank() }
                            ?: "No about information provided."
                    )

                    // Corrected Experience Section
                    Text("Experience", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(8.dp))
                    if (userData.experience.isEmpty()) {
                        Text("No experience added", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            userData.experience.forEach { item ->
                                ExperienceItemView(item = item) // This composable is needed
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))


                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Skills",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (userData.skills.isNullOrEmpty()) {
                            Text(
                                "No skills listed.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        } else {
                            // The file you provided has ReadOnlyFlowRow
                            // I am assuming it should be ViewOnlyFlowRow to match the error
                            ViewOnlyFlowRow(items = userData.skills)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Contact",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ContactRow(
                            icon = Icons.Default.Email,
                            text = userData.email
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Projects",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (userProjects.isEmpty()) {
                            Text(
                                "No projects added yet.",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            userProjects.forEach { project ->
                                ProjectCard(project = project, onClick = {
                                    // TODO: Navigate to project details screen
                                })
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    content: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            content,
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}


@Composable
private fun ContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isLink: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(
                enabled = onClick != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = { onClick?.invoke() }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isLink) Color(0xFF0A66C2) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text,
            color = if (isLink) Color(0xFF0A66C2) else Color.Black
        )
    }
}


// ✅ 2. ADD 'private' TO FIX THE CONFLICT
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ViewOnlyFlowRow(items: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { skill ->
            SuggestionChip(
                onClick = { },
                label = { Text(skill) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = Color(0xFFF1F3F4)
                ),
            )
        }
    }
}

// (This was missing from the user's file, but is required by the code)
@Composable
private fun ExperienceItemView(item: ExperienceItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Default.Business,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .background(Color.LightGray, CircleShape)
                .padding(8.dp),
            tint = Color.DarkGray
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(item.role, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(item.companyName, fontSize = 14.sp)
            Text(
                text = "${item.startDate} - ${if (item.isCurrent) "Present" else item.endDate}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}


@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(project.imageUrl.ifBlank { R.drawable.noprofile })
                    .crossfade(true)
                    .allowHardware(false) // Fix for large image crash
                    .build(),
                contentDescription = project.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.noprofile)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = project.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = project.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onClick) {
                    Text("View Project")
                }
            }
        }
    }
}
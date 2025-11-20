package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
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
import com.example.strathtankalumni.R
import com.example.strathtankalumni.data.Collaboration
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel

@Composable
fun AlumniCollaborationsScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val collaborations by authViewModel.collaborations.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val userId = currentUser?.userId

    // 1. Filter: Projects I OWN (My Hubs)
    // We use distinctBy { it.projectId } because there might be multiple collaboration
    // documents for one project (one for each member), but we only want to show the project card once.
    val ownedProjects = remember(collaborations, userId) {
        collaborations
            .filter { it.projectOwnerId == userId }
            .distinctBy { it.projectId }
    }

    // 2. Filter: Projects I JOINED (Where I am a member)
    val joinedProjects = remember(collaborations, userId) {
        collaborations.filter {
            it.collaboratorId == userId && it.status == "accepted"
        }
    }

    val isEmpty = ownedProjects.isEmpty() && joinedProjects.isEmpty()

    if (isEmpty) {
        EmptyCollaborationsView(navController)
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {

            // --- SECTION 1: MY HUBS ---
            if (ownedProjects.isNotEmpty()) {
                item {
                    SectionHeader(title = "My Project Hubs")
                }
                items(ownedProjects, key = { "own_${it.projectId}" }) { collaboration ->
                    CollaborationCard(
                        collaboration = collaboration,
                        isOwner = true,
                        onClick = {
                            navController.navigate(Screen.CollaborationDetail.createRoute(collaboration.id))
                        }
                    )
                }
            }

            // --- SECTION 2: JOINED TEAMS ---
            if (joinedProjects.isNotEmpty()) {
                item {
                    // Add top spacer if we also had owned projects above
                    if (ownedProjects.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                    }
                    SectionHeader(title = "Joined Teams")
                }
                items(joinedProjects, key = { "join_${it.id}" }) { collaboration ->
                    CollaborationCard(
                        collaboration = collaboration,
                        isOwner = false,
                        onClick = {
                            navController.navigate(Screen.CollaborationDetail.createRoute(collaboration.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
    )
}

@Composable
private fun CollaborationCard(
    collaboration: Collaboration,
    isOwner: Boolean,
    onClick: () -> Unit
) {
    // Dynamic Colors based on Role
    val badgeColor = if (isOwner) Color(0xFFE8F5E9) else Color(0xFFE3F2FD) // Light Green vs Light Blue
    val badgeTextColor = if (isOwner) Color(0xFF2E7D32) else Color(0xFF1565C0) // Dark Green vs Dark Blue
    val badgeText = if (isOwner) "Owner" else "Collaborator"
    val badgeIcon = if (isOwner) Icons.Default.Star else Icons.Default.Group

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Project Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(collaboration.projectImageUrl.ifEmpty { R.drawable.sample_featured })
                    .crossfade(true)
                    .build(),
                contentDescription = collaboration.projectTitle,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.sample_featured)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title and Badge Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = collaboration.projectTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Description
                Text(
                    text = collaboration.projectDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(Modifier.height(10.dp))

                // Role Badge
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = badgeTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCollaborationsView(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.noprofile),
            contentDescription = "No collaborations",
            modifier = Modifier
                .size(100.dp)
                .alpha(0.5f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No Collab Hubs Yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Start a project or join a team to see your hubs here.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate(Screen.AlumniProjects.route) },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Explore Projects")
        }
    }
}

// Helper for alpha
fun Modifier.alpha(alpha: Float) = this.then(Modifier.drawWithContent {
    drawContent()
    drawRect(Color.White.copy(alpha = 1f - alpha), blendMode = BlendMode.DstIn)
})
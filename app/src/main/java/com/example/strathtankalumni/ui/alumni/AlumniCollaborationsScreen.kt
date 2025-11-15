// megre branch ]/StrathTank_2.0-merge/app/src/main/java/com/example/strathtankalumni/ui/alumni/AlumniCollaborationsScreen.kt
package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

    val myAcceptedCollabs = collaborations.filter {
        it.collaboratorId == currentUser?.userId && it.status == "accepted"
    }

    if (myAcceptedCollabs.isEmpty()) {
        EmptyCollaborationsView(navController)
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp)
        ) {
            items(myAcceptedCollabs, key = { it.id }) { collaboration ->
                CollaborationCard(
                    collaboration = collaboration,
                    onClick = {
                        navController.navigate(Screen.CollaborationDetail.createRoute(collaboration.id))
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyCollaborationsView(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.noprofile), // Placeholder
            contentDescription = "Empty box",
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(1f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Oops! No collaboration requests made.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Explore and request on one project.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                navController.navigate(Screen.AlumniProjects.route)
            },
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF0F2F5),
                contentColor = Color.Black
            ),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
        ) {
            Text("Explore Projects", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CollaborationCard(
    collaboration: Collaboration,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(collaboration.projectImageUrl.ifEmpty { R.drawable.sample_featured })
                    .crossfade(true)
                    .allowHardware(false) // ✅ --- THIS IS THE FIX ---
                    .build(),
                contentDescription = collaboration.projectTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.sample_featured)
            )
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = collaboration.projectTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = collaboration.projectDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
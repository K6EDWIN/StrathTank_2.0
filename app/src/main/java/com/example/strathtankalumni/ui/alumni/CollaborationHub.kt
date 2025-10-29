package com.example.strathtankalumni.ui.alumni

import com.example.strathtankalumni.navigation.Screen
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

// Dummy Data Class for a Project/Collaboration
data class CollaborationItem(
    val title: String,
    val description: String,
    val tags: List<String>,
    val creatorName: String
)

val dummyCollaborations = listOf(
    CollaborationItem("Project title", "Short description about the project", listOf("#Fundraiser", "#TechProject"), "Name"),
    CollaborationItem("E-commerce App", "A full-featured app with cart and checkout", listOf("#Android", "#Compose"), "Alice"),
    CollaborationItem("Health Tracker", "Tracks fitness and wellness goals", listOf("#Health", "#MobileApp"), "Bob"),
    CollaborationItem("Crowdfunding Portal", "Platform for startup fundraising", listOf("#Finance", "#WebApp"), "Clara"),
)

@Composable
fun CollaborationHubScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Main Title
                Text(
                    text = "Collaboration Hub",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                )

                // 2. Search Bar and Create New Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = { /* Update search query */ },
                        label = { Text("Search Collaborations...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { /* Handle Create New */ }) {
                        Text("Create New")
                    }
                }

                // 3. Filter/Sort Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { /* Newest */ }) { Text("Newest") }
                    TextButton(onClick = { /* Most Joined */ }) { Text("Most Joined") }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { /* Sort */ }) { Text("Sort") }
                }
            }
        }
    ) { paddingValues ->
        // 4. Collaboration Cards Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dummyCollaborations) { item ->
                CollaborationCard(item = item, navController = navController)
            }
        }
    }
}

@Composable
fun CollaborationCard(item: CollaborationItem, navController: NavHostController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
            Text(text = item.tags.joinToString(" "), style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(8.dp))

            // Creator Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Creator",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(4.dp))
                Column {
                    Text(text = "Created by", style = MaterialTheme.typography.bodySmall)
                    Text(text = item.creatorName, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Action Buttons
            OutlinedButton(
                onClick = {
                    // Navigate to project details screen
                    navController.navigate(Screen.ProjectDetails.route)

                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Details")
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { /* Join Collaboration action */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join Collaboration")
            }
        }
    }
}

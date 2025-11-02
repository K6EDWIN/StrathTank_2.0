package com.example.strathtankalumni.ui.alumni

import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.strathtankalumni.R
import com.example.strathtankalumni.navigation.Screen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniProjectsScreen(navController: NavHostController, padding: PaddingValues) {
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
            // 🔍 Search bar
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search projects") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🏷 Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(label = "IT")
                FilterChip(label = "Popular")
                FilterChip(label = "Latest")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 📋 Project List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Featured", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    ProjectCard(
                        title = "AI–Powered Tutoring App",
                        description = "An innovative app using AI to personalize learning experiences for students.",
                        imageRes = R.drawable.sample_featured
                    )
                }

                item {
                    Text("Trending Projects", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                items(
                    listOf(
                        Triple("Eco–Friendly Packaging Design", "Sustainable packaging solutions for a greener future, focusing on reducing waste.", R.drawable.sample_eco),
                        Triple("Mobile App for Local Farmers", "Connecting farmers directly with consumers, providing market access and fair pricing.", R.drawable.sample_farm),
                        Triple("Interactive Museum Exhibit", "An engaging exhibit using AR to bring history to life for visitors of all ages.", R.drawable.sample_museum)
                    )
                ) { (title, desc, img) ->
                    ProjectCard(title = title, description = desc, imageRes = img)
                }
            }
        }
    }
}

// 🏷 Simple Filter Chip
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

// 📋 Project Card
@Composable
fun ProjectCard(title: String, description: String, imageRes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(imageRes)
                    .size(coil.size.Size(200, 200))
                    .build()
            ),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(end = 12.dp),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(description, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

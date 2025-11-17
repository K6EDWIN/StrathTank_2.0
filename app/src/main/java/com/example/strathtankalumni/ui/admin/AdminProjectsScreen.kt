package com.example.strathtankalumni.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.strathtankalumni.R

data class AdminProject(
    val title: String,
    val category: String,
    val imageRes: Int
)

@Composable
fun AdminProjectsScreen(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    val projects = listOf(
        AdminProject("Eco-Friendly Packaging Solutions", "Non-IT", R.drawable.sample_eco),
        AdminProject("AI-Powered Tutoring Platform", "IT", R.drawable.sample_featured),
        AdminProject("Sustainable Agriculture Tech", "IT", R.drawable.sample_farm)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(projects) { project ->
                AdminProjectCard(project = project)
            }
        }
    }
}

@Composable
private fun AdminProjectCard(project: AdminProject) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = project.imageRes),
                    contentDescription = null,
                    modifier = Modifier.height(60.dp)
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "Category: ${project.category}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { /* TODO: preview */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF111827),
                        contentColor = Color(0xFFE5E7EB)
                    ),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text("Preview")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { /* TODO: reject */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF111827),
                            contentColor = Color(0xFFE5E7EB)
                        ),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text("Reject")
                    }
                    Button(
                        onClick = { /* TODO: approve */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text("Approve")
                    }
                }
            }
        }
    }
}



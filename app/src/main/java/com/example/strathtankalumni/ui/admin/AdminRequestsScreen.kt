package com.example.strathtankalumni.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.navigation.NavHostController

data class AdminRequest(
    val title: String,
    val status: String,
    val author: String
)

@Composable
fun AdminRequestsScreen(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    val requests = listOf(
        AdminRequest("Eco-Friendly Packaging Solutions", "Pending", "Ava Harper"),
        AdminRequest("Sustainable Energy Solutions", "Approved", "Ethan Carter"),
        AdminRequest("Urban Farming Initiative", "Pending", "Olivia Bennett"),
        AdminRequest("AI-Powered Education", "Approved", "Noah Thompson")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Text(
            text = "All Requests",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.SemiBold
            )
        )

        LazyColumn(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(requests) { request ->
                AdminRequestRow(request = request)
            }
        }
    }
}

@Composable
private fun AdminRequestRow(request: AdminRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF030712))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Project: ${request.title}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "Status: ${request.status}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
                )
                Text(
                    text = "By: ${request.author}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                )
            }
            IconButton(onClick = { /* TODO: open detail */ }) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open",
                    tint = Color(0xFF6B7280)
                )
            }
        }
    }
}



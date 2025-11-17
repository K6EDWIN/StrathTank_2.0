package com.example.strathtankalumni.ui.admin

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AdminViewModel

@Composable
fun AdminDashboardScreen(
    navController: NavHostController,
    paddingValues: PaddingValues,
    adminViewModel: AdminViewModel
) {
    val stats by adminViewModel.dashboardStats.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(title = "Total Users", value = stats.totalUsers.toString())
                StatCard(title = "Active Projects", value = stats.activeProjects.toString())
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(title = "Pending Verifications", value = stats.pendingVerifications.toString())
                StatCard(title = "Reports", value = stats.openReports.toString())
            }
        }

        item {
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF020617))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "User Growth",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF9CA3AF))
                    )
                    Text(
                        text = "+15%",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color(0xFF22C55E),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Last 30 Days",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Simple bar placeholders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        listOf(40, 60, 50, 70, 45).forEach { height ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(height.dp)
                                        .background(
                                            color = Color(0xFF1D4ED8),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        navController.navigate(Screen.AdminUsers.route)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Verify Users")
                }
                Button(
                    onClick = {
                        navController.navigate(Screen.AdminProjects.route)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF111827),
                        contentColor = Color(0xFFE5E7EB)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Approve Projects")
                }
                Button(
                    onClick = {
                        navController.navigate(Screen.AdminReports.route)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF111827),
                        contentColor = Color(0xFFE5E7EB)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View Reports")
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111827)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFFF9FAFB),
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}


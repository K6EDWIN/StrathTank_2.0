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

data class AdminReport(
    val reporter: String,
    val subject: String,
    val reason: String,
    val imageRes: Int? = null
)

@Composable
fun AdminReportsScreen(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    val reports = listOf(
        AdminReport("Ethan Harper", "User: Olivia Bennett", "Inappropriate Content", null),
        AdminReport("Noah Carter", "Project: Eco-Friendly Packaging", "Copyright Infringement", R.drawable.sample_eco),
        AdminReport("Sophia Clark", "User: Liam Davis", "Harassment", null)
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
            items(reports) { report ->
                AdminReportCard(report = report)
            }
        }
    }
}

@Composable
private fun AdminReportCard(report: AdminReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Reported by: ${report.reporter}",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
            )
            Text(
                text = report.subject,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = "Reason: ${report.reason}",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
            )

            report.imageRes?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { /* TODO: dismiss */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF111827),
                        contentColor = Color(0xFFE5E7EB)
                    ),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text("Dismiss")
                }
                Button(
                    onClick = { /* TODO: take action */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text("Take Action")
                }
            }
        }
    }
}



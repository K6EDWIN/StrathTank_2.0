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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.strathtankalumni.R
import com.example.strathtankalumni.data.User
import com.example.strathtankalumni.viewmodel.AdminViewModel

@Composable
fun AdminUsersScreen(
    navController: NavHostController,
    paddingValues: PaddingValues,
    adminViewModel: AdminViewModel
) {
    val query = remember { mutableStateOf("") }
    val users by adminViewModel.users.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query.value,
            onValueChange = { query.value = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search users") }
        )
        Spacer(modifier = Modifier.height(12.dp))

        val filtered = users.filter {
            val q = query.value.trim().lowercase()
            if (q.isBlank()) true
            else {
                val name = "${it.firstName} ${it.lastName}".lowercase()
                name.contains(q) || it.email.lowercase().contains(q)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filtered) { user ->
                AdminUserRow(user = user, onVerify = {
                    adminViewModel.updateUserVerification(user.userId, "verified")
                })
            }
        }
    }
}

@Composable
private fun AdminUserRow(
    user: User,
    onVerify: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF030712))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.noprofile),
                    contentDescription = null,
                    modifier = Modifier
                        .height(40.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = "${user.firstName} ${user.lastName}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onVerify,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Verify")
                }
            }
        }
    }
}



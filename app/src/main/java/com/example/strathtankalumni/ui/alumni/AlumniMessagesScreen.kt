package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.strathtankalumni.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniMessagesScreen(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding())
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp)
        )

        Spacer(Modifier.height(24.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item {
                ConversationRow(
                    name = "Ian",
                    lastMessage = "See you tomorrow!",
                    timestamp = "10:30 AM",
                    onClick = {
                        navController.navigate(Screen.DirectMessage.createRoute("Ian"))
                    }
                )
            }
            item {
                ConversationRow(
                    name = "Kanye West",
                    lastMessage = "Sounds good!",
                    timestamp = "Yesterday",
                    onClick = {
                        navController.navigate(Screen.DirectMessage.createRoute("Kanye West"))
                    }
                )
            }
            item {
                ConversationRow(
                    name = "Denzel",
                    lastMessage = "Yes I will check in 5 min",
                    timestamp = "Monday",
                    onClick = {
                        navController.navigate(Screen.DirectMessage.createRoute("Denzel"))
                    }
                )
            }
        }
    }
}

@Composable
private fun ConversationRow(
    name: String,
    lastMessage: String,
    timestamp: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "$name's profile picture",
            modifier = Modifier.size(56.dp),
            tint = Color.LightGray
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(
                text = lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = timestamp,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
// File: com/example/strathtankalumni/ui/alumni/EditProfileScreen.kt
package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlumniEditProfileScreen(
    alumniNavController: NavHostController
) {
    var bio by remember { mutableStateOf(TextFieldValue("")) }
    var linkedin by remember { mutableStateOf(TextFieldValue("")) }
    var skillInput by remember { mutableStateOf(TextFieldValue("")) }
    var skills by remember { mutableStateOf(listOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { alumniNavController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Denzel Omondi", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Product Manager at TechCorp", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(20.dp))

            // Bio
            Text("Bio", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = bio,
                onValueChange = { bio = it },
                placeholder = { Text("Write something about yourself...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.textFieldColors(containerColor = Color(0xFFF1F3F4))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Skills
            Text("Skills", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Input + Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = skillInput,
                    onValueChange = { skillInput = it },
                    placeholder = { Text("Add a skill") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.textFieldColors(containerColor = Color(0xFFF1F3F4))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (skillInput.text.isNotBlank()) {
                            skills = skills + skillInput.text.trim()
                            skillInput = TextFieldValue("")
                        }
                    }
                ) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Skill chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                skills.forEach { skill ->
                    AssistChip(
                        onClick = {},
                        label = { Text(skill) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFF1F3F4))
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Contact info
            Text("Contact Info", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("LinkedIn URL", color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = linkedin,
                onValueChange = { linkedin = it },
                placeholder = { Text("Enter LinkedIn profile URL") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.textFieldColors(containerColor = Color(0xFFF1F3F4))
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { alumniNavController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { /* TODO: Save profile */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

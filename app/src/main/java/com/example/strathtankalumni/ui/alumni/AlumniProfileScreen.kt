package com.example.strathtankalumni.ui.alumni

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.LocalIndication // 👈✅ ADD THIS IMPORT
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource // 👈✅ ADD THIS IMPORT
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.strathtankalumni.R
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniProfileScreen(
    mainNavController: NavHostController,
    alumniNavController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var about by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var newSkill by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf(listOf<String>()) }
    var linkedinUrl by remember { mutableStateOf("") }
    var editingLinkedIn by remember { mutableStateOf(false) }

    // ✅ Image picker launcher
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            authViewModel.uploadProfilePhoto(it, context.contentResolver) { success ->
                Toast.makeText(
                    context,
                    if (success) "Profile photo updated!" else "Failed to upload photo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // 🔄 Fetch user data
    LaunchedEffect(Unit) {
        authViewModel.fetchCurrentUser()
    }

    // Populate data
    LaunchedEffect(currentUser) {
        currentUser?.let {
            about = it.about.ifBlank { "No about yet" }
            experience = it.experience.ifBlank { "No experience added" }
            skills = it.skills
            linkedinUrl = it.linkedinUrl
        }
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    authViewModel.signOut()
                    mainNavController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.AlumniHome.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Logout", color = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ✅ Profile photo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F3F4))
                    // ✅ --- FIX 1 ---
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        onClick = { launcher.launch("image/*") }
                    ),
                // ✅ --- END OF FIX ---
                contentAlignment = Alignment.Center
            ) {
                if (!currentUser?.profilePhotoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentUser?.profilePhotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.noprofile),
                        contentDescription = "Default profile",
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${currentUser?.firstName ?: ""} ${currentUser?.lastName ?: ""}".trim()
                    .ifBlank { "Loading..." },
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = currentUser?.email ?: "Loading...",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (isEditing) {
                        authViewModel.updateUserProfile(
                            about = about,
                            experience = experience,
                            skills = skills,
                            linkedinUrl = linkedinUrl
                        ) { success ->
                            Toast.makeText(
                                context,
                                if (success) "Profile updated successfully" else "Failed to update profile",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    isEditing = !isEditing
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F3F4)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isEditing) "Save Changes" else "Edit Profile", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isEditing) {
                ProfileSection("About", about)
                ProfileSection("Experience", experience)
                ProfileSection(
                    "Skills & Interests",
                    if (skills.isEmpty()) "No skills yet" else skills.joinToString(", ")
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Contact", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                ContactRow(Icons.Default.Email, currentUser?.email ?: "Not available")

                if (!editingLinkedIn) {
                    if (linkedinUrl.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                tint = Color(0xFF0A66C2)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = linkedinUrl,
                                color = Color(0xFF0A66C2),
                                modifier = Modifier
                                    .weight(1f)
                                    // ✅ --- FIX 2 ---
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = LocalIndication.current,
                                        onClick = {
                                            val intent =
                                                Intent(Intent.ACTION_VIEW, Uri.parse(linkedinUrl))
                                            context.startActivity(intent)
                                        }
                                    )
                                // ✅ --- END OF FIX ---
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { editingLinkedIn = true }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit LinkedIn",
                                    tint = Color.Gray
                                )
                            }
                        }
                    } else {
                        ContactRow(Icons.Default.Link, "Add LinkedIn URL") {
                            editingLinkedIn = true
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = linkedinUrl,
                        onValueChange = { linkedinUrl = it },
                        label = { Text("LinkedIn URL") },
                        placeholder = { Text("https://linkedin.com/in/yourname") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            authViewModel.updateUserProfile(
                                about = about,
                                experience = experience,
                                skills = skills,
                                linkedinUrl = linkedinUrl
                            ) { success ->
                                Toast.makeText(
                                    context,
                                    if (success) "LinkedIn updated successfully" else "Failed to update LinkedIn",
                                    Toast.LENGTH_SHORT
                                ).show()
                                if (success) editingLinkedIn = false
                            }
                        }
                    ) {
                        Text("Save LinkedIn")
                    }
                }
            } else {
                // Editable mode
                EditableSection("About", about) { about = it }
                EditableSection("Experience", experience) { experience = it }
                Text("Skills & Interests", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newSkill,
                        onValueChange = { newSkill = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a skill") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (newSkill.isNotBlank()) {
                            skills = skills + newSkill.trim()
                            newSkill = ""
                        }
                    }) { Text("Add") }
                }

                if (skills.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRowLayout(items = skills, onRemove = { skills = skills - it })
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun ContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            // ✅ --- FIX 3 ---
            .clickable(
                enabled = onClick != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = {
                    onClick?.invoke()
                }
            ),
        // ✅ --- END OF FIX ---
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
private fun EditableSection(
    title: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun ProfileSection(
    title: String,
    content: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            content,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class) // 👈✅ ADDED THIS for FlowRow
@Composable
fun FlowRowLayout(
    items: List<String>,
    onRemove: (String) -> Unit
) {
    // ✅ Replaced custom implementation with standard FlowRow
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { skill ->
            AssistChip(
                onClick = { onRemove(skill) },
                label = { Text(skill) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFFE0E0E0)
                ),
            )
        }
    }
}
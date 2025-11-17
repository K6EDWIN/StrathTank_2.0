// megre branch ]/StrathTank_2.0-merge/app/src/main/java/com/example/strathtankalumni/ui/alumni/AlumniProfileScreen.kt
package com.example.strathtankalumni.ui.alumni

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.strathtankalumni.R
import com.example.strathtankalumni.data.ExperienceItem
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel
import androidx.compose.foundation.layout.ExperimentalLayoutApi // ✅ IMPORT
import coil.size.Size // ✅ IMPORT

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlumniProfileScreen(
    mainNavController: NavHostController,
    alumniNavController: NavHostController,
    paddingValues: PaddingValues, // This comes from the Scaffold
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var about by remember { mutableStateOf("") }

    var experienceList by remember { mutableStateOf(listOf<ExperienceItem>()) }

    var newSkill by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf(listOf<String>()) }
    var linkedinUrl by remember { mutableStateOf("") }
    var editingLinkedIn by remember { mutableStateOf(false) }

    var showExperienceDialog by remember { mutableStateOf(false) }

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

    LaunchedEffect(Unit) {
        authViewModel.fetchCurrentUser()
    }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            about = it.about.ifBlank { "No about yet" }
            experienceList = it.experience
            skills = it.skills
            linkedinUrl = it.linkedinUrl
        }
    }

    if (showExperienceDialog) {
        ExperienceEntryDialog(
            onDismiss = { showExperienceDialog = false },
            onSave = { newItem ->
                experienceList = experienceList + newItem
                showExperienceDialog = false
            }
        )
    }

    // --- EDIT: Added Box wrapper for loading spinner ---
    Box(modifier = Modifier.fillMaxSize()) {
        if (currentUser == null) {
            // --- Show a centered spinner while loading ---
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            // --- Once loaded, show profile content ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // --- MODIFICATION 1: Apply ONLY bottom padding from Scaffold ---
                    // This removes the unwanted top padding under the app bar.
                    .padding(bottom = paddingValues.calculateBottomPadding())
                // --- MODIFICATION 2: Removed .padding(horizontal = 16.dp) from here ---
            ) {

                // --- EDIT: Added new scrollable Column with .weight(1f) ---
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        // --- MODIFICATION 3: Added consolidated horizontal padding here ---
                        // This will apply to ALL scrollable content.
                        .padding(horizontal = 16.dp)
                ) {

                    // --- 1. Centered Header Section ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        // --- MODIFICATION 4: Removed .padding(horizontal = 16.dp) ---
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F3F4))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = LocalIndication.current,
                                    onClick = { launcher.launch("image/*") }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!currentUser?.profilePhotoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(currentUser?.profilePhotoUrl)
                                        .crossfade(true)
                                        .size(Size(256, 256)) // ✅ --- CRASH FIX ---
                                        .allowHardware(false)
                                        .build(),
                                    contentDescription = "Profile photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.noprofile)
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


                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (isEditing) {
                                    authViewModel.updateUserProfile(
                                        about = about,
                                        experience = experienceList,
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
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isEditing) "Save Changes" else "Edit Profile")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- 2. Left-Aligned Content Section ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                        // --- MODIFICATION 4: Removed .padding(horizontal = 16.dp) ---
                    ) {
                        if (!isEditing) {
                            // --- View Mode ---
                            ProfileSection("About", about)

                            Text("Experience", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            if (experienceList.isEmpty()) {
                                Text("No experience added", color = Color.Gray, fontSize = 14.sp)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    experienceList.forEach { item ->
                                        ExperienceItemView(item = item)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Skills", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            if (skills.isEmpty()) {
                                Text("No skills yet", color = Color.Gray, fontSize = 14.sp)
                            } else {
                                ViewOnlyFlowRow(items = skills)
                            }
                            Spacer(modifier = Modifier.height(12.dp))

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
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = LocalIndication.current,
                                                    onClick = {
                                                        val intent =
                                                            Intent(Intent.ACTION_VIEW, Uri.parse(linkedinUrl))
                                                        context.startActivity(intent)
                                                    }
                                                )
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
                                            experience = experienceList,
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
                            // --- Editable Mode ---
                            EditableSection("About", about) { about = it }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Experience", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                IconButton(onClick = { showExperienceDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Experience")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (experienceList.isEmpty()) {
                                Text("No experience added", color = Color.Gray, fontSize = 14.sp)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    experienceList.forEach { item ->
                                        ExperienceItemEditView(
                                            item = item,
                                            onRemove = {
                                                experienceList = experienceList - item
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

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

                        Spacer(modifier = Modifier.height(24.dp))

                        var tabIndex by remember { mutableStateOf(0) }
                        val tabs = listOf("Projects", "Collaborations")

                        PrimaryTabRow(selectedTabIndex = tabIndex) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = tabIndex == index,
                                    onClick = { tabIndex = index },
                                    text = { Text(text = title) }
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                                .heightIn(min = 200.dp), // Added height
                            contentAlignment = Alignment.TopCenter // Changed
                        ) {
                            when (tabIndex) {
                                0 -> Text("No projects yet", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                                1 -> AlumniCollaborationsScreen(
                                    navController = alumniNavController,
                                    authViewModel = authViewModel
                                )
                            }
                        }

                        // --- EDIT: Moved Logout Button out of this Column ---
                    }
                } // --- EDIT: Closing brace for new .weight(1f) Column

                // --- EDIT: Moved Logout Button here, to be pinned ---
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
                        // --- MODIFICATION 5: Added horizontal padding to align with content ---
                        .padding(horizontal = 16.dp) ,// Keep the top padding
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout", color = Color.White)
                }
            } // --- EDIT: Closing brace for Root Column
        } // --- EDIT: Closing brace for "else"
    } // --- EDIT: Closing brace for "Box"
}

// (ContactRow is unchanged)
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
            .clickable(
                enabled = onClick != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = {
                    onClick?.invoke()
                }
            ),
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

// (EditableSection is unchanged)
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
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp), // Give "About" more space
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

// (ProfileSection is unchanged)
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
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            content,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

// (FlowRowLayout is unchanged)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRowLayout(
    items: List<String>,
    onRemove: (String) -> Unit
) {
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

// (ViewOnlyFlowRow is unchanged)
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ViewOnlyFlowRow(items: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { skill ->
            Box(
                modifier = Modifier
                    .background(Color(0xFFF1F3F4), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(skill, fontSize = 14.sp, color = Color.Black)
            }
        }
    }
}

// (ExperienceItemView is unchanged)
@Composable
private fun ExperienceItemView(item: ExperienceItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Default.Business,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .background(Color.LightGray, CircleShape)
                .padding(8.dp),
            tint = Color.DarkGray
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(item.role, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(item.companyName, fontSize = 14.sp)
            Text(
                text = "${item.startDate} - ${if (item.isCurrent) "Present" else item.endDate}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

// (ExperienceItemEditView is unchanged)
@Composable
private fun ExperienceItemEditView(
    item: ExperienceItem,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F3F4), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.role, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(item.companyName, fontSize = 14.sp)
        }
        IconButton(onClick = { /* TODO: Implement edit */ }) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray)
        }
    }
}

// (ExperienceEntryDialog is unchanged)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperienceEntryDialog(
    onDismiss: () -> Unit,
    onSave: (ExperienceItem) -> Unit
) {
    var role by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var isCurrent by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp) // Changed from vertical
                    .padding(vertical = 24.dp), // Added vertical padding
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add Experience", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role / Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date (e.g., Jan 2020)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isCurrent,
                        onCheckedChange = { isCurrent = it }
                    )
                    Text("I am currently working in this role")
                }

                if (!isCurrent) {
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date (e.g., Dec 2022)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        val newItem = ExperienceItem(
                            role = role,
                            companyName = companyName,
                            startDate = startDate,
                            endDate = if (isCurrent) "Present" else endDate,
                            isCurrent = isCurrent
                        )
                        onSave(newItem)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = role.isNotBlank() && companyName.isNotBlank() && startDate.isNotBlank()
                ) {
                    Text("Save")
                }
            }
        }
    }
}
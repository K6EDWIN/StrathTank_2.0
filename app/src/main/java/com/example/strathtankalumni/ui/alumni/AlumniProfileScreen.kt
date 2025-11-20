package com.example.strathtankalumni.ui.alumni

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import coil.size.Size

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlumniProfileScreen(
    mainNavController: NavHostController,
    alumniNavController: NavHostController,
    paddingValues: PaddingValues,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()

    // --- State Management ---
    var isEditing by remember { mutableStateOf(false) }
    var about by remember { mutableStateOf("") }
    var experienceList by remember { mutableStateOf(listOf<ExperienceItem>()) }
    var newSkill by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf(listOf<String>()) }
    var linkedinUrl by remember { mutableStateOf("") }
    var editingLinkedIn by remember { mutableStateOf(false) }
    var showExperienceDialog by remember { mutableStateOf(false) }

    // --- Image Picker ---
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

    // --- Data Loading ---
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

    // --- Dialogs ---
    if (showExperienceDialog) {
        ExperienceEntryDialog(
            onDismiss = { showExperienceDialog = false },
            onSave = { newItem ->
                experienceList = experienceList + newItem
                showExperienceDialog = false
            }
        )
    }

    // ✅ LAYOUT FIX: Used LazyColumn for the entire screen content.
    // This ensures the Logout button scrolls with the content and sits directly below it,
    // removing the large empty gap.
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        if (currentUser == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // Add padding at bottom so last item isn't hidden by navbar
                contentPadding = PaddingValues(
                    bottom = paddingValues.calculateBottomPadding() + 24.dp
                )
            ) {
                // --- 1. HEADER SECTION ---
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(top = 24.dp, bottom = 24.dp)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F3F4))
                                .clickable { launcher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!currentUser?.profilePhotoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(currentUser?.profilePhotoUrl)
                                        .crossfade(true)
                                        .size(Size(256, 256))
                                        .build(),
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(R.drawable.noprofile),
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    alpha = 0.5f
                                )
                            }

                            // Camera Icon Overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .size(28.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .padding(6.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "${currentUser?.firstName} ${currentUser?.lastName}".trim().ifBlank { "User Name" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.Black
                        )
                        Text(currentUser?.email ?: "", color = Color.Gray, fontSize = 14.sp)

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (isEditing) {
                                    authViewModel.updateUserProfile(about, experienceList, skills, linkedinUrl) {
                                        Toast.makeText(context, if (it) "Profile updated!" else "Failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                isEditing = !isEditing
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEditing) MaterialTheme.colorScheme.primary else Color(0xFFE8F0FE),
                                contentColor = if (isEditing) Color.White else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(if (isEditing) "Save Changes" else "Edit Profile")
                        }
                    }
                }

                // --- 2. MAIN CONTENT (DETAILS) ---
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                if (!isEditing) {
                                    // READ MODE
                                    ProfileSection("About", about)
                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))

                                    Text("Experience", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(Modifier.height(8.dp))
                                    if (experienceList.isEmpty()) Text("No experience added", color = Color.Gray, fontSize = 14.sp)
                                    else Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { experienceList.forEach { ExperienceItemView(it) } }

                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))

                                    Text("Skills", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(Modifier.height(8.dp))
                                    if (skills.isEmpty()) Text("No skills yet", color = Color.Gray, fontSize = 14.sp) else ViewOnlyFlowRow(skills)

                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))

                                    Text("Contact", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(Modifier.height(8.dp))
                                    ContactRow(Icons.Default.Email, currentUser?.email ?: "N/A")

                                    if (!editingLinkedIn) {
                                        if (linkedinUrl.isNotBlank()) {
                                            ContactRow(Icons.Default.Link, "LinkedIn Profile") {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkedinUrl))
                                                context.startActivity(intent)
                                            }
                                        } else {
                                            TextButton(onClick = { editingLinkedIn = true }) { Text("Add LinkedIn Profile") }
                                        }
                                    } else {
                                        OutlinedTextField(
                                            value = linkedinUrl,
                                            onValueChange = { linkedinUrl = it },
                                            label = { Text("LinkedIn URL") },
                                            modifier = Modifier.fillMaxWidth(),
                                            trailingIcon = { IconButton(onClick = { authViewModel.updateUserProfile(about, experienceList, skills, linkedinUrl) { if(it) editingLinkedIn = false } }) { Icon(Icons.Default.Close, "Save") } }
                                        )
                                    }
                                } else {
                                    // EDIT MODE
                                    EditableSection("About", about) { about = it }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Experience", fontWeight = FontWeight.Bold)
                                        IconButton(onClick = { showExperienceDialog = true }) { Icon(Icons.Default.Add, "Add") }
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        experienceList.forEach { item -> ExperienceItemEditView(item) { experienceList = experienceList - item } }
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Text("Skills", fontWeight = FontWeight.Bold)
                                    Row {
                                        OutlinedTextField(value = newSkill, onValueChange = { newSkill = it }, modifier = Modifier.weight(1f), placeholder = { Text("Add skill") })
                                        Button(onClick = { if (newSkill.isNotBlank()) { skills = skills + newSkill.trim(); newSkill = "" } }) { Text("Add") }
                                    }
                                    FlowRowLayout(skills) { skills = skills - it }
                                }
                            }
                        }
                    }
                }

                // --- 3. COLLABORATIONS SECTION ---
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        CollaborationsContainer(alumniNavController, authViewModel)
                    }
                    Spacer(Modifier.height(32.dp))
                }

                // --- 4. LOGOUT BUTTON ---
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Button(
                            onClick = {
                                authViewModel.signOut()
                                mainNavController.navigate(Screen.Welcome.route) {
                                    popUpTo(Screen.AlumniHome.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Log Out")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun CollaborationsContainer(
    alumniNavController: NavHostController,
    authViewModel: AuthViewModel
) {
    Column {
        Text(
            text = "My Collaborations",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp) // Fixed height for inner list
                    .padding(vertical = 8.dp)
            ) {
                AlumniCollaborationsScreen(
                    navController = alumniNavController,
                    authViewModel = authViewModel
                )
            }
        }
    }
}

// --- HELPER COMPONENTS ---

@Composable
private fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(enabled = onClick != null) { onClick?.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 15.sp, color = Color.DarkGray)
    }
}

@Composable
private fun EditableSection(title: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFF8F9FA), focusedContainerColor = Color.White)
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun ProfileSection(title: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        Spacer(Modifier.height(4.dp))
        Text(content, color = Color.DarkGray, fontSize = 15.sp, lineHeight = 22.sp)
        Spacer(Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRowLayout(items: List<String>, onRemove: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { skill ->
            InputChip(
                selected = true, onClick = { onRemove(skill) }, label = { Text(skill) },
                trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) },
                colors = InputChipDefaults.inputChipColors(containerColor = Color(0xFFE0E0E0))
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ViewOnlyFlowRow(items: List<String>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { skill ->
            SuggestionChip(onClick = {}, label = { Text(skill) }, colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFF1F3F4), labelColor = Color.Black), border = null)
        }
    }
}

@Composable
private fun ExperienceItemView(item: ExperienceItem) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.Business, null, Modifier.size(40.dp).background(Color(0xFFF0F0F0), CircleShape).padding(8.dp), tint = Color.Gray)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(item.role, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(item.companyName, fontSize = 14.sp, color = Color.DarkGray)
            Text("${item.startDate} - ${if (item.isCurrent) "Present" else item.endDate}", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ExperienceItemEditView(item: ExperienceItem, onRemove: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(Color(0xFFF8F9FA), RoundedCornerShape(8.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.role, fontWeight = FontWeight.Bold)
            Text(item.companyName, fontSize = 14.sp)
        }
        IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, "Remove", tint = Color.Gray) }
    }
}

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
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Add Experience",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isCurrent,
                        onCheckedChange = { isCurrent = it }
                    )
                    Text("Currently working here")
                }

                if (!isCurrent) {
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Button(
                    onClick = {
                        // ✅ FIX: Using Named Arguments to match your Data Class exactly
                        val newItem = ExperienceItem(
                            role = role,
                            companyName = companyName,
                            startDate = startDate,
                            endDate = if (isCurrent) "Present" else endDate,
                            isCurrent = isCurrent
                            // Removed 'description' because your data class doesn't have it
                        )
                        onSave(newItem)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = role.isNotBlank() && companyName.isNotBlank()
                ) {
                    Text("Save")
                }
            }
        }
    }
}
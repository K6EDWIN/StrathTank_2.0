package com.example.strathtankalumni.ui.alumni

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.strathtankalumni.data.Project
import com.example.strathtankalumni.viewmodel.AuthViewModel
import com.example.strathtankalumni.viewmodel.ProjectState // Import ProjectState

// List of available categories/tags
private val allTags = listOf(
    "Machine Learning", "Data Analysis", "Cloud Computing", "UI/UX",
    "API Development", "Frontend", "Backend", "IoT", "DevOps", "Cybersecurity"
)

//List of project types
private val allProjectTypes = listOf(
    "Mobile App", "Web Application", "AI/ML Model", "Game Development", "Hardware/IoT", "Data Science"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // Added ExperimentalLayoutApi for FlowRow
@Composable
fun AlumniAddProjectsPage(navController: NavHostController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // State for all form fields
    var projectTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var liveUrl by remember { mutableStateOf("") }
    var githubUrl by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    var selectedProjectType by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // State for project submission feedback
    val projectState by authViewModel.projectState.collectAsState()

    // Handle navigation and message after success/error
    LaunchedEffect(projectState) {
        when (val state = projectState) {
            is ProjectState.Success -> {
                // 1. Display Toast Message
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()

                // 2. Navigate away
                navController.popBackStack()

                // 3. Reset state (only after navigation)
                authViewModel.resetProjectState()
            }
            is ProjectState.Error -> {
                // Display Error Message
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetProjectState()
            }
            // Ensure state is not handled when Idle or Loading
            ProjectState.Loading, ProjectState.Idle -> {
                // Do nothing
            }
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Submit button logic
    val handleSubmit = {
        if (projectTitle.isBlank() || description.isBlank() || selectedProjectType.isBlank()) {
            Toast.makeText(context, "Please fill in title, description, and project type.", Toast.LENGTH_LONG).show()
        } else {
            val newProject = Project(
                title = projectTitle,
                description = description,
                projectUrl = liveUrl,
                githubUrl = githubUrl,
                projectType = selectedProjectType,
                categories = selectedCategories.toList()
            )

            authViewModel.saveProject(
                projectData = newProject,
                imageUri = imageUri,
                contentResolver = context.contentResolver,
                onResult = { /* Success/failure is handled via projectState LaunchedEffect */ }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Project") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = handleSubmit,
                        enabled = projectState != ProjectState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (projectState is ProjectState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Submit", color = Color.White)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Project Image Uploader
            Text(
                text = "Project Photo (Optional)",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                    .clickable { imagePickerLauncher.launch("image/*") }
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Project Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = "Upload Image",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap to Select Image", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Project Title
            OutlinedTextField(
                value = projectTitle,
                onValueChange = { projectTitle = it },
                label = { Text("Project Title") },
                leadingIcon = { Icon(Icons.Default.Title, null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Project Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                leadingIcon = { Icon(Icons.Default.Description, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 6
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Project Live URL
            OutlinedTextField(
                value = liveUrl,
                onValueChange = { liveUrl = it },
                label = { Text("Live Project URL (Optional)") },
                leadingIcon = { Icon(Icons.Default.Link, null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // GitHub Repository URL
            OutlinedTextField(
                value = githubUrl,
                onValueChange = { githubUrl = it },
                label = { Text("GitHub Repository URL (Optional)") },
                leadingIcon = { Icon(Icons.Default.Code, null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))


            // Project Type Selection
            Text(
                text = "Project Type (Required)",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allProjectTypes.forEach { type ->
                    val isSelected = selectedProjectType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedProjectType = if (isSelected) "" else type // Toggle selection
                        },
                        label = { Text(type) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = Color(0xFFEEEEEE),
                            labelColor = Color(0xFF666666)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            //Category Tags Selection (Multi-select)
            Text(
                text = "Category Tags (Optional)",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allTags.forEach { tag ->
                    val isSelected = selectedCategories.contains(tag)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategories = if (isSelected) {
                                selectedCategories - tag
                            } else {
                                selectedCategories + tag
                            }
                        },
                        label = { Text(tag) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = Color(0xFFEEEEEE),
                            labelColor = Color(0xFF666666)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
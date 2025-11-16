package com.example.strathtankalumni.ui.alumni

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack 
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
import androidx.navigation.NavHostController
import androidx.compose.foundation.shape.CircleShape
import coil.compose.AsyncImage
import com.example.strathtankalumni.viewmodel.AuthViewModel
import com.example.strathtankalumni.viewmodel.ProjectState
import androidx.compose.foundation.lazy.LazyRow 
import androidx.compose.foundation.lazy.items 
import androidx.compose.ui.platform.LocalFocusManager 
import androidx.compose.ui.res.painterResource 
import androidx.compose.foundation.text.KeyboardActions 
import androidx.compose.foundation.text.KeyboardOptions 
import androidx.compose.ui.text.input.ImeAction 
import coil.request.ImageRequest 
import com.example.strathtankalumni.R 
import coil.size.Size 

// List of available categories/tags
private val allTags = listOf(
    "AI", "Machine Learning", "Mobile", "Web", "DevOps", "Cybersecurity",
    "Design", "Cloud", "Blockchain", "GameDev", "IoT", "AR/VR"
)

// List of available Project Types (used for filtering tech lists)
private val PROJECT_TYPES = listOf("Mobile App", "Web Development", "Data Science", "Embedded Systems", "Other")

// Technology Lists categorized by Project Type
private val MobileAppLanguages = listOf("Kotlin", "Swift", "Dart (Flutter)", "JavaScript (React Native)")
private val WebDevelopmentLanguages = listOf("JavaScript", "TypeScript", "Python", "Java", "PHP", "Go")
private val DataScienceLanguages = listOf("Python", "R", "Julia")
private val EmbeddedSystemsLanguages = listOf("C", "C++", "Assembly")

private val AllDatabases = listOf("Firestore", "Realtime DB", "PostgreSQL", "MySQL", "MongoDB", "SQLite", "Redis")
// Frameworks are filtered by Project Type to be more relevant
private val AllTechStacks = listOf(
    "Compose", "Flutter", "React Native", "SwiftUI", // Mobile
    "React", "Vue", "Angular", "Django", "Spring Boot", "Tailwind CSS", "Next.js", // Web
    "TensorFlow", "PyTorch", "Scikit-learn", // Data Science
    "Arduino", "Raspberry Pi", "RTOS", // Embedded
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlumniAddProjectsPage(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val projectState by authViewModel.projectState.collectAsState()
    val focusManager = LocalFocusManager.current

    // State for project fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var projectUrl by remember { mutableStateOf("") }
    var githubUrl by remember { mutableStateOf("") }
    var projectType by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) } // Main cover image
    var mediaImageUris by remember { mutableStateOf(emptyList<Uri>()) } // NEW: Multiple media images
    var pdfUri by remember { mutableStateOf<Uri?>(null) } // NEW: Single PDF file

    var selectedCategories by remember { mutableStateOf(emptyList<String>()) }

    // NEW STATE for Tech Selection
    var selectedLanguages by remember { mutableStateOf(emptyList<String>()) }
    var selectedDatabases by remember { mutableStateOf(emptyList<String>()) }
    var selectedTechStacks by remember { mutableStateOf(emptyList<String>()) }
    // END NEW STATE

    // Dynamic Lists based on Project Type
    val availableLanguages = remember(projectType) {
        when (projectType) {
            "Mobile App" -> MobileAppLanguages
            "Web Development" -> WebDevelopmentLanguages
            "Data Science" -> DataScienceLanguages
            "Embedded Systems" -> EmbeddedSystemsLanguages
            else -> MobileAppLanguages + WebDevelopmentLanguages + DataScienceLanguages + EmbeddedSystemsLanguages
        }.distinct().sorted()
    }

    val availableTechStacks = remember(projectType) {
        when (projectType) {
            "Mobile App" -> AllTechStacks.filter { it in listOf("Compose", "Flutter", "React Native", "SwiftUI") }
            "Web Development" -> AllTechStacks.filter { it in listOf("React", "Vue", "Angular", "Django", "Spring Boot", "Tailwind CSS", "Next.js") }
            "Data Science" -> AllTechStacks.filter { it in listOf("TensorFlow", "PyTorch", "Scikit-learn") }
            "Embedded Systems" -> AllTechStacks.filter { it in listOf("Arduino", "Raspberry Pi", "RTOS") }
            else -> AllTechStacks
        }.distinct().sorted()
    }

    val availableDatabases = remember { AllDatabases.sorted() }

    // Reset languages/tech stacks if project type changes
    LaunchedEffect(projectType) {
        selectedLanguages = selectedLanguages.filter { it in availableLanguages }
        selectedTechStacks = selectedTechStacks.filter { it in availableTechStacks }
    }


    // --- LAUNCHERS FOR MEDIA (MODIFIED/NEW) ---

    // 1. Main Cover Image (Single)
    val coverImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // 2. Project Media Images (Multiple)
    val mediaImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        // Append new URIs to the existing list
        mediaImageUris = mediaImageUris + uris
    }

    // 3. Project PDF (Single)
    val pdfFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // We strictly enforce PDF here, although GetContent is generic
        if (uri != null && context.contentResolver.getType(uri)?.startsWith("application/pdf") == true) {
            pdfUri = uri
        } else if (uri != null) {
            Toast.makeText(context, "Please select a valid PDF file.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- END LAUNCHERS ---


    LaunchedEffect(projectState) {
        when (val state = projectState) {
            is ProjectState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                // Clear fields and navigate back (or to projects screen)
                title = ""
                description = ""
                projectUrl = ""
                githubUrl = ""
                projectType = ""
                imageUri = null
                mediaImageUris = emptyList() // Clear NEW field
                pdfUri = null // Clear NEW field
                selectedCategories = emptyList()
                selectedLanguages = emptyList()
                selectedDatabases = emptyList()
                selectedTechStacks = emptyList()
                authViewModel.resetProjectState()
                navController.popBackStack() // Go back to the previous screen (Projects list)
            }
            is ProjectState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetProjectState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Project") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White) // ✅
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank() && projectType.isNotBlank()) {
                        authViewModel.saveProject(
                            title = title,
                            description = description,
                            projectUrl = projectUrl,
                            githubUrl = githubUrl,
                            projectType = projectType,
                            imageUri = imageUri,
                            mediaImageUris = mediaImageUris, // PASS NEW DATA
                            pdfUri = pdfUri, // PASS NEW DATA
                            categories = selectedCategories,
                            programmingLanguages = selectedLanguages,
                            databaseUsed = selectedDatabases,
                            techStack = selectedTechStacks,
                            onResult = {} // Handled by LaunchedEffect
                        )
                    } else {
                        Toast.makeText(context, "Please fill in project title, description, and type.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = projectState != ProjectState.Loading
            ) {
                if (projectState == ProjectState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("SAVE PROJECT", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Project Cover Image Uploader ---
            Text("Project Cover Image (Required)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        onClick = { coverImagePickerLauncher.launch("image/*") }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    // ✅ --- CRASH FIX 1 ---
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUri)
                            .crossfade(true)
                            .size(Size(1024, 1024)) // <-- FIX
                            .allowHardware(false) // <-- FIX
                            .build(),
                        contentDescription = "Project Cover Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.sample_featured)
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "Add Cover Photo", tint = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap to select cover image", color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- NEW: Project Media Images (Gallery) ---
            Text("Project Gallery Images (Optional, Multiple)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Replaced FlowRow with LazyRow
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Display selected images
                items(mediaImageUris) { uri ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        // ✅ --- CRASH FIX 2 ---
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(uri)
                                .crossfade(true)
                                .size(Size(256, 256)) // <-- FIX
                                .allowHardware(false) // <-- FIX
                                .build(),
                            contentDescription = "Project Media",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.sample_featured)
                        )
                        // Remove button
                        IconButton(
                            onClick = {
                                mediaImageUris = mediaImageUris - uri
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove Image", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Add button
                item {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current,
                                onClick = { mediaImagePickerLauncher.launch("image/*") }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Image", tint = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))


            // --- NEW: Project Documentation (PDF) ---
            Text("Project Documentation (Optional, PDF)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        onClick = { pdfFilePickerLauncher.launch("application/pdf") }
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (pdfUri != null) {
                    val fileName = pdfUri?.lastPathSegment?.substringAfterLast("/") ?: "Selected PDF"
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Icon", tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text(fileName, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { pdfUri = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove PDF", tint = Color.Gray)
                    }
                } else {
                    Icon(Icons.Default.AttachFile, contentDescription = "Attach PDF", tint = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    Text("Tap to select a single PDF file", color = Color.Gray, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))


            // Text Fields
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Project Title *") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                minLines = 4
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = projectUrl,
                onValueChange = { projectUrl = it },
                label = { Text("Live Demo URL (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = githubUrl,
                onValueChange = { githubUrl = it },
                label = { Text("GitHub URL (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))


            // Project Type Selection (Crucial for filtering below)
            Text("Project Type *", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp) // ✅
            ) {
                PROJECT_TYPES.forEach { type ->
                    val isSelected = projectType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { projectType = if (isSelected) "" else type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFEEEEEE),
                            labelColor = Color(0xFF666666)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- NEW: Programming Languages Used Selection ---
            Text("Programming Languages Used", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Select all that apply. (Options filtered by Project Type)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (projectType.isNotBlank()) {
                TechSelectionRow(
                    availableTags = availableLanguages,
                    selectedTags = selectedLanguages,
                    onTagToggle = { tag ->
                        selectedLanguages = if (selectedLanguages.contains(tag)) {
                            selectedLanguages - tag
                        } else {
                            selectedLanguages + tag
                        }
                    }
                )
            } else {
                Text("Please select a Project Type above to see language options.", color = Color.Red.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.height(16.dp))


            // --- NEW: Database Used Selection (Universal) ---
            Text("Database/Storage Used", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Select all that apply.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            TechSelectionRow(
                availableTags = availableDatabases,
                selectedTags = selectedDatabases,
                onTagToggle = { tag ->
                    selectedDatabases = if (selectedDatabases.contains(tag)) {
                        selectedDatabases - tag
                    } else {
                        selectedDatabases + tag
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))


            // --- NEW: Tech Stack/Frameworks Used Selection ---
            Text("Frameworks/Tech Stack", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Select all that apply. (Options filtered by Project Type)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (projectType.isNotBlank()) {
                TechSelectionRow(
                    availableTags = availableTechStacks,
                    selectedTags = selectedTechStacks,
                    onTagToggle = { tag ->
                        selectedTechStacks = if (selectedTechStacks.contains(tag)) {
                            selectedTechStacks - tag
                        } else {
                            selectedTechStacks + tag
                        }
                    }
                )
            } else {
                Text("Please select a Project Type above to see framework options.", color = Color.Red.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.height(16.dp))


            // --- EXISTING: General Project Categories/Tags ---
            Text("General Project Categories/Tags", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Select all general tags that best describe your project.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))

            TechSelectionRow(
                availableTags = allTags,
                selectedTags = selectedCategories,
                onTagToggle = { tag ->
                    selectedCategories = if (selectedCategories.contains(tag)) {
                        selectedCategories - tag
                    } else {
                        selectedCategories + tag
                    }
                }
            )

            Spacer(modifier = Modifier.height(100.dp)) // Space for the bottom button
        }
    }
}

/**
 * Reusable Composable for displaying and managing FilterChips.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TechSelectionRow(
    availableTags: List<String>,
    selectedTags: List<String>,
    onTagToggle: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp) // ✅
    ) {
        availableTags.forEach { tag ->
            val isSelected = selectedTags.contains(tag)
            FilterChip(
                selected = isSelected,
                onClick = { onTagToggle(tag) },
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
                    labelColor = Color(0xFF666666) // ✅
                ),
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}
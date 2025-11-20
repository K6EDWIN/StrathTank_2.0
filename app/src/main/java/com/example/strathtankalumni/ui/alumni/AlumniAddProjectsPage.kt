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
import coil.request.ImageRequest
import com.example.strathtankalumni.R
import coil.size.Size

// ==========================================
// 1. THE "EVERYTHING" LISTS
// Finally expanded this so it's not just for CS majors.
// ==========================================

// The massive list of tags. Covering all bases from coding to eco projs.
private val allTags = listOf(
    // The nerdy stuff
    "AI", "Machine Learning", "Mobile", "Web", "DevOps", "Cybersecurity", "Blockchain", "IoT", "AR/VR", "Robotics", "SaaS",
    // The good human stuff (Green Tech, NGOs)
    "Sustainability", "Renewable Energy", "Climate Action", "Circular Economy", "Waste Management", "Social Impact", "Community Dev", "Education", "Healthcare", "Poverty Alleviation",
    // The money stuff
    "Fintech", "E-commerce", "B2B", "B2C", "Marketing", "Entrepreneurship", "Venture Capital", "Supply Chain",
    // The artsy stuff
    "Design", "Media", "Journalism", "Architecture", "Fashion", "Research", "Policy"
)

// Project Types: Used to figure out what fields to show/hide later.
private val PROJECT_TYPES = listOf(
    // Hard Tech
    "Mobile App", "Web Platform", "AI/Data Science", "IoT/Hardware", "Game Development",
    // Business & Social (No code required)
    "Business Venture/Startup", "Social Impact/NGO", "Eco-Friendly/Sustainability", "Fundraising/Charity",
    // Misc
    "Creative Arts/Media", "Research/Academic", "Other"
)

// --- Tech Specific Lists (Only show these if it's a coding project) ---
private val MobileAppLanguages = listOf("Kotlin", "Swift", "Dart (Flutter)", "JavaScript (React Native)", "Java")
private val WebDevelopmentLanguages = listOf("JavaScript", "TypeScript", "Python", "Java", "PHP", "Go", "Ruby", "C#")
private val DataScienceLanguages = listOf("Python", "R", "Julia", "SQL", "Scala")
private val EmbeddedSystemsLanguages = listOf("C", "C++", "Assembly", "Rust", "Verilog")

private val AllDatabases = listOf("Firestore", "Realtime DB", "PostgreSQL", "MySQL", "MongoDB", "SQLite", "Redis", "Supabase", "DynamoDB", "Oracle")

// Combined list: Tech frameworks AND Business tools (Excel, Canva, etc.)
// Because business students need tools too.
private val AllToolsAndFrameworks = listOf(
    // Mobile
    "Compose", "Flutter", "React Native", "SwiftUI", "XML",
    // Web
    "React", "Vue", "Angular", "Django", "Spring Boot", "Tailwind CSS", "Next.js", "Laravel", ".NET", "Node.js",
    // Data/AI
    "TensorFlow", "PyTorch", "Scikit-learn", "Pandas", "Keras", "Hadoop",
    // Embedded
    "Arduino", "Raspberry Pi", "RTOS", "ESP32",
    // Business/Design/Management (For the non-techies!)
    "Trello", "Jira", "Slack", "Notion", "Excel/Sheets", "Power BI", "Tableau",
    "Canva", "Figma", "Adobe XD", "Adobe Creative Cloud",
    "Shopify", "WordPress", "Wix", "Kickstarter", "GoFundMe", "Google Analytics"
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

    // --- FORM STATE (Tracking all the user inputs) ---
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var projectUrl by remember { mutableStateOf("") }
    var githubUrl by remember { mutableStateOf("") } // Might change label if not tech
    var projectType by remember { mutableStateOf("") } // The most important one, drives the UI

    // File URIs
    var imageUri by remember { mutableStateOf<Uri?>(null) } // Main cover
    var mediaImageUris by remember { mutableStateOf(emptyList<Uri>()) } // Gallery
    var pdfUri by remember { mutableStateOf<Uri?>(null) } // Documentation

    // Tag Collections
    var selectedCategories by remember { mutableStateOf(emptyList<String>()) }

    // Tech/Tools Collections (These get filtered based on project type)
    var selectedLanguages by remember { mutableStateOf(emptyList<String>()) }
    var selectedDatabases by remember { mutableStateOf(emptyList<String>()) }
    var selectedTechStacks by remember { mutableStateOf(emptyList<String>()) }

    // ==========================================
    // 2. THE SMART LOGIC (Filtering)
    // ==========================================

    // Quick check: Is this a nerdy coding project?
    // If FALSE, we hide the "Databases" and "Languages" sections to not scare business students.
    val isTechProject = remember(projectType) {
        projectType in listOf("Mobile App", "Web Platform", "AI/Data Science", "IoT/Hardware", "Game Development")
    }

    // Filter the languages list so we don't show "Swift" to a Web Developer.
    val availableLanguages = remember(projectType) {
        when (projectType) {
            "Mobile App" -> MobileAppLanguages
            "Web Platform" -> WebDevelopmentLanguages
            "AI/Data Science" -> DataScienceLanguages
            "IoT/Hardware" -> EmbeddedSystemsLanguages
            "Game Development" -> listOf("C#", "C++", "Lua")
            else -> emptyList() // Return empty if it's not a coding project
        }.distinct().sorted()
    }

    // Filter the Tools/Stacks.
    // If it's a Charity project, show "Kickstarter" instead of "React".
    val availableToolsAndStacks = remember(projectType) {
        when (projectType) {
            "Mobile App" -> AllToolsAndFrameworks.filter { it in listOf("Compose", "Flutter", "React Native", "SwiftUI", "XML") }
            "Web Platform" -> AllToolsAndFrameworks.filter { it in listOf("React", "Vue", "Angular", "Django", "Spring Boot", "Tailwind CSS", "Next.js", "Laravel", ".NET", "Node.js") }
            "AI/Data Science" -> AllToolsAndFrameworks.filter { it in listOf("TensorFlow", "PyTorch", "Scikit-learn", "Pandas", "Keras", "Hadoop", "Power BI", "Tableau") }
            "IoT/Hardware" -> AllToolsAndFrameworks.filter { it in listOf("Arduino", "Raspberry Pi", "RTOS", "ESP32") }

            // The Non-Tech Logic
            "Business Venture/Startup", "E-Commerce" -> AllToolsAndFrameworks.filter { it in listOf("Shopify", "WordPress", "Excel/Sheets", "Power BI", "Google Analytics", "Trello", "Slack", "Notion") }
            "Social Impact/NGO", "Fundraising/Charity", "Eco-Friendly/Sustainability" -> AllToolsAndFrameworks.filter { it in listOf("Kickstarter", "GoFundMe", "Excel/Sheets", "Canva", "Trello", "Notion", "WordPress") }
            "Creative Arts/Media" -> AllToolsAndFrameworks.filter { it in listOf("Canva", "Figma", "Adobe XD", "Adobe Creative Cloud", "WordPress") }

            else -> AllToolsAndFrameworks // Fallback: Show everything
        }.distinct().sorted()
    }

    val availableDatabases = remember { AllDatabases.sorted() }

    // Reset the lists if the user changes their mind about Project Type
    // (prevents having "Python" selected for a "Fashion" project)
    LaunchedEffect(projectType) {
        selectedLanguages = emptyList()
        selectedTechStacks = emptyList()
        if (!isTechProject) selectedDatabases = emptyList() // Nuke the DBs if not tech
    }

    // --- BOILERPLATE LAUNCHERS (Don't touch) ---
    val coverImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    val mediaImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> -> mediaImageUris = mediaImageUris + uris }

    val pdfFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Simple validation to make sure they actually picked a PDF
        if (uri != null && context.contentResolver.getType(uri)?.startsWith("application/pdf") == true) {
            pdfUri = uri
        } else if (uri != null) {
            Toast.makeText(context, "Yo, please select a valid PDF.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- HANDLE SAVE/UPLOAD RESULT ---
    LaunchedEffect(projectState) {
        when (val state = projectState) {
            is ProjectState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                // Clear EVERYTHING so the next project starts fresh
                title = ""; description = ""; projectUrl = ""; githubUrl = ""; projectType = ""
                imageUri = null; mediaImageUris = emptyList(); pdfUri = null
                selectedCategories = emptyList(); selectedLanguages = emptyList()
                selectedDatabases = emptyList(); selectedTechStacks = emptyList()
                authViewModel.resetProjectState()
                navController.popBackStack() // Bye bye, go back to list
            }
            is ProjectState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetProjectState()
            }
            else -> Unit
        }
    }

    // --- UI START ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Project") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // The Big "Save" Button
            Button(
                onClick = {
                    // Basic validation so we don't send empty junk to the DB
                    if (title.isNotBlank() && description.isNotBlank() && projectType.isNotBlank()) {
                        authViewModel.saveProject(
                            title = title,
                            description = description,
                            projectUrl = projectUrl,
                            githubUrl = githubUrl,
                            projectType = projectType,
                            imageUri = imageUri,
                            mediaImageUris = mediaImageUris,
                            pdfUri = pdfUri,
                            categories = selectedCategories,
                            programmingLanguages = selectedLanguages,
                            databaseUsed = selectedDatabases,
                            techStack = selectedTechStacks,
                            onResult = {}
                        )
                    } else {
                        Toast.makeText(context, "Missing info! We need at least a Title, Description, and Type.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                enabled = projectState != ProjectState.Loading
            ) {
                if (projectState == ProjectState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
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
                .verticalScroll(rememberScrollState()) // Make it scrollable because this form is huge
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- 1. Cover Image (Must have one) ---
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
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(imageUri).crossfade(true).size(Size(1024, 1024)).allowHardware(false).build(),
                        contentDescription = "Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.sample_featured)
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray)
                        Text("Tap to select cover", color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. Gallery (The carousel) ---
            Text("Gallery Images (Optional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(mediaImageUris) { uri ->
                    Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp))) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(uri).size(Size(256, 256)).allowHardware(false).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Little 'X' button to remove image
                        IconButton(
                            onClick = { mediaImageUris = mediaImageUris - uri },
                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                // Add button at the end of the list
                item {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .clickable { mediaImagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. PDF (For the academic types) ---
            Text("Documentation (Optional PDF)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { pdfFilePickerLauncher.launch("application/pdf") }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pdfUri != null) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Red)
                    Spacer(Modifier.width(8.dp))
                    // Just parsing the filename roughly
                    Text(pdfUri?.lastPathSegment?.substringAfterLast("/") ?: "PDF Selected", modifier = Modifier.weight(1f))
                    IconButton(onClick = { pdfUri = null }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                    }
                } else {
                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = Color.Gray)
                    Text(" Tap to attach PDF", color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- 4. The Boring Text Inputs ---
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Project Title *") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description *") }, modifier = Modifier.fillMaxWidth().height(120.dp), minLines = 4)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = projectUrl, onValueChange = { projectUrl = it }, label = { Text("Project/Website URL") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            // Smart Label: Change "GitHub" to "Source" if it's not a tech project
            OutlinedTextField(value = githubUrl, onValueChange = { githubUrl = it }, label = { Text(if(isTechProject) "GitHub URL" else "Repository/Source URL") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            // --- 5. Project Type (The Trigger for Logic) ---
            Text("Project Type *", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PROJECT_TYPES.forEach { type ->
                    val isSelected = projectType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { projectType = if (isSelected) "" else type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.White)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 3. CONDITIONAL UI SECTIONS (The Magic)
            // ==========================================

            // SECTION A: Languages (Only if Tech)
            if (isTechProject && availableLanguages.isNotEmpty()) {
                Text("Programming Languages", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                TechSelectionRow(availableTags = availableLanguages, selectedTags = selectedLanguages, onTagToggle = { tag ->
                    selectedLanguages = if (selectedLanguages.contains(tag)) selectedLanguages - tag else selectedLanguages + tag
                })
                Spacer(modifier = Modifier.height(16.dp))
            }

            // SECTION B: Databases (Only if Tech)
            if (isTechProject) {
                Text("Databases", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                TechSelectionRow(availableTags = availableDatabases, selectedTags = selectedDatabases, onTagToggle = { tag ->
                    selectedDatabases = if (selectedDatabases.contains(tag)) selectedDatabases - tag else selectedDatabases + tag
                })
                Spacer(modifier = Modifier.height(16.dp))
            }

            // SECTION C: Tools & Frameworks (Dynamic Label!)
            if (projectType.isNotBlank()) {
                // If it's tech, call it "Tech Stack". If it's business, call it "Tools Used".
                val label = if (isTechProject) "Tech Stack / Frameworks" else "Tools & Platforms Used"
                val subLabel = if (isTechProject) "Frameworks used (e.g., React, Flutter)" else "Software or platforms used (e.g., Excel, Canva, Kickstarter)"

                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subLabel, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                if (availableToolsAndStacks.isNotEmpty()) {
                    TechSelectionRow(availableTags = availableToolsAndStacks, selectedTags = selectedTechStacks, onTagToggle = { tag ->
                        selectedTechStacks = if (selectedTechStacks.contains(tag)) selectedTechStacks - tag else selectedTechStacks + tag
                    })
                } else {
                    Text("No specific tools listed for this category, select general tags below.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // SECTION D: General Tags (For everyone)
            Text("General Categories / Key Topics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Select keywords that describe your project.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            TechSelectionRow(availableTags = allTags, selectedTags = selectedCategories, onTagToggle = { tag ->
                selectedCategories = if (selectedCategories.contains(tag)) selectedCategories - tag else selectedCategories + tag
            })

            // Spacer so the floating button or bottom nav doesn't hide content
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// Helper Composable for the filter chips row
// Keeping it clean here.
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        availableTags.forEach { tag ->
            val isSelected = selectedTags.contains(tag)
            FilterChip(
                selected = isSelected,
                onClick = { onTagToggle(tag) },
                label = { Text(tag) },
                leadingIcon = if (isSelected) { { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) } } else null,
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
}
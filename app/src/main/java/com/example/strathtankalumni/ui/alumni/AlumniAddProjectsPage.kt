package com.example.strathtankalumni.ui.alumni

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.strathtankalumni.R
import com.example.strathtankalumni.viewmodel.AuthViewModel
import com.example.strathtankalumni.viewmodel.ProjectState

// --- DATA LISTS (Kept exactly as you had them) ---
private val allTags = listOf(
    "AI", "Machine Learning", "Mobile", "Web", "DevOps", "Cybersecurity", "Blockchain", "IoT", "AR/VR", "Robotics", "SaaS",
    "Sustainability", "Renewable Energy", "Climate Action", "Circular Economy", "Waste Management", "Social Impact", "Community Dev", "Education", "Healthcare", "Poverty Alleviation",
    "Fintech", "E-commerce", "B2B", "B2C", "Marketing", "Entrepreneurship", "Venture Capital", "Supply Chain",
    "Design", "Media", "Journalism", "Architecture", "Fashion", "Research", "Policy"
)

private val PROJECT_TYPES = listOf(
    "Mobile App", "Web Platform", "AI/Data Science", "IoT/Hardware", "Game Development",
    "Business Venture/Startup", "Social Impact/NGO", "Eco-Friendly/Sustainability", "Fundraising/Charity",
    "Creative Arts/Media", "Research/Academic", "Other"
)

private val MobileAppLanguages = listOf("Kotlin", "Swift", "Dart (Flutter)", "JavaScript (React Native)", "Java")
private val WebDevelopmentLanguages = listOf("JavaScript", "TypeScript", "Python", "Java", "PHP", "Go", "Ruby", "C#")
private val DataScienceLanguages = listOf("Python", "R", "Julia", "SQL", "Scala")
private val EmbeddedSystemsLanguages = listOf("C", "C++", "Assembly", "Rust", "Verilog")
private val AllDatabases = listOf("Firestore", "Realtime DB", "PostgreSQL", "MySQL", "MongoDB", "SQLite", "Redis", "Supabase", "DynamoDB", "Oracle")
private val AllToolsAndFrameworks = listOf(
    "Compose", "Flutter", "React Native", "SwiftUI", "XML",
    "React", "Vue", "Angular", "Django", "Spring Boot", "Tailwind CSS", "Next.js", "Laravel", ".NET", "Node.js",
    "TensorFlow", "PyTorch", "Scikit-learn", "Pandas", "Keras", "Hadoop",
    "Arduino", "Raspberry Pi", "RTOS", "ESP32",
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

    // --- STATE ---
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var projectUrl by remember { mutableStateOf("") }
    var githubUrl by remember { mutableStateOf("") }
    var projectType by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var mediaImageUris by remember { mutableStateOf(emptyList<Uri>()) }
    var pdfUri by remember { mutableStateOf<Uri?>(null) }

    var selectedCategories by remember { mutableStateOf(emptyList<String>()) }
    var selectedLanguages by remember { mutableStateOf(emptyList<String>()) }
    var selectedDatabases by remember { mutableStateOf(emptyList<String>()) }
    var selectedTechStacks by remember { mutableStateOf(emptyList<String>()) }

    // --- LOGIC (Filtering) ---
    val isTechProject = remember(projectType) {
        projectType in listOf("Mobile App", "Web Platform", "AI/Data Science", "IoT/Hardware", "Game Development")
    }

    val availableLanguages = remember(projectType) {
        when (projectType) {
            "Mobile App" -> MobileAppLanguages
            "Web Platform" -> WebDevelopmentLanguages
            "AI/Data Science" -> DataScienceLanguages
            "IoT/Hardware" -> EmbeddedSystemsLanguages
            "Game Development" -> listOf("C#", "C++", "Lua")
            else -> emptyList()
        }.distinct().sorted()
    }

    val availableToolsAndStacks = remember(projectType) {
        when (projectType) {
            "Mobile App" -> AllToolsAndFrameworks.filter { it in listOf("Compose", "Flutter", "React Native", "SwiftUI", "XML") }
            "Web Platform" -> AllToolsAndFrameworks.filter { it in listOf("React", "Vue", "Angular", "Django", "Spring Boot", "Tailwind CSS", "Next.js", "Laravel", ".NET", "Node.js") }
            "AI/Data Science" -> AllToolsAndFrameworks.filter { it in listOf("TensorFlow", "PyTorch", "Scikit-learn", "Pandas", "Keras", "Hadoop", "Power BI", "Tableau") }
            "IoT/Hardware" -> AllToolsAndFrameworks.filter { it in listOf("Arduino", "Raspberry Pi", "RTOS", "ESP32") }
            "Business Venture/Startup", "E-Commerce" -> AllToolsAndFrameworks.filter { it in listOf("Shopify", "WordPress", "Excel/Sheets", "Power BI", "Google Analytics", "Trello", "Slack", "Notion") }
            "Social Impact/NGO", "Fundraising/Charity", "Eco-Friendly/Sustainability" -> AllToolsAndFrameworks.filter { it in listOf("Kickstarter", "GoFundMe", "Excel/Sheets", "Canva", "Trello", "Notion", "WordPress") }
            "Creative Arts/Media" -> AllToolsAndFrameworks.filter { it in listOf("Canva", "Figma", "Adobe XD", "Adobe Creative Cloud", "WordPress") }
            else -> AllToolsAndFrameworks
        }.distinct().sorted()
    }

    val availableDatabases = remember { AllDatabases.sorted() }

    LaunchedEffect(projectType) {
        selectedLanguages = emptyList()
        selectedTechStacks = emptyList()
        if (!isTechProject) selectedDatabases = emptyList()
    }

    // --- LAUNCHERS ---
    val coverImagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }
    val mediaImagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { mediaImageUris = mediaImageUris + it }
    val pdfFilePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null && context.contentResolver.getType(it)?.startsWith("application/pdf") == true) pdfUri = it
        else if (it != null) Toast.makeText(context, "Please select a valid PDF.", Toast.LENGTH_SHORT).show()
    }

    // --- RESULT HANDLING ---
    LaunchedEffect(projectState) {
        when (val state = projectState) {
            is ProjectState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                authViewModel.resetProjectState()
                navController.popBackStack()
            }
            is ProjectState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetProjectState()
            }
            else -> Unit
        }
    }

    // --- UI ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Project", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // SAVE BUTTON
            Surface(
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Button(
                    onClick = {
                        if (title.isNotBlank() && description.isNotBlank() && projectType.isNotBlank()) {
                            authViewModel.saveProject(
                                title, description, projectUrl, githubUrl, projectType,
                                imageUri, mediaImageUris, pdfUri, selectedCategories,
                                selectedLanguages, selectedDatabases, selectedTechStacks
                            ) {}
                        } else {
                            Toast.makeText(context, "Please fill in Title, Description, and Type.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = projectState != ProjectState.Loading
                ) {
                    if (projectState == ProjectState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Publish Project", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = Color(0xFFF9FAFB) // Light grey background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // --- SECTION 1: MEDIA ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat look with border usually looks cleaner
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Project Media", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Cover Image Upload (Dashed Border Area)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5))
                            .dashedBorder(2.dp, Color.Gray, 12.dp)
                            .clickable { coverImagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(imageUri).crossfade(true).build(),
                                contentDescription = "Cover",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Change overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Change Cover", color = Color.White, fontSize = 12.sp)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Add Cover Image", color = Color.Gray, fontWeight = FontWeight.Medium)
                                Text("(Required)", color = Color.Red.copy(alpha = 0.6f), fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gallery
                    if (mediaImageUris.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(mediaImageUris) { uri ->
                                Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))) {
                                    AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    IconButton(
                                        onClick = { mediaImageUris = mediaImageUris - uri },
                                        modifier = Modifier.align(Alignment.TopEnd).size(20.dp).background(Color.White, CircleShape).padding(2.dp)
                                    ) { Icon(Icons.Default.Close, null, tint = Color.Black) }
                                }
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                        .clickable { mediaImagePickerLauncher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Default.Add, null, tint = Color.Gray) }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { mediaImagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Collections, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add Gallery Images")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // PDF
                    if (pdfUri != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF0F0), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null, tint = Color.Red)
                            Spacer(Modifier.width(12.dp))
                            Text("Document Attached", modifier = Modifier.weight(1f), color = Color.Black)
                            IconButton(onClick = { pdfUri = null }) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
                        }
                    } else {
                        TextButton(onClick = { pdfFilePickerLauncher.launch("application/pdf") }) {
                            Icon(Icons.Default.AttachFile, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Attach Documentation (PDF)")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECTION 2: DETAILS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Project Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))

                    FormTextField(value = title, onValueChange = { title = it }, label = "Project Title", icon = Icons.Default.Title)
                    Spacer(Modifier.height(12.dp))

                    FormTextField(value = description, onValueChange = { description = it }, label = "Description", icon = Icons.Default.Description, singleLine = false, minLines = 4)
                    Spacer(Modifier.height(12.dp))

                    FormTextField(value = projectUrl, onValueChange = { projectUrl = it }, label = "Live Link (Optional)", icon = Icons.Default.Link)
                    Spacer(Modifier.height(12.dp))

                    FormTextField(
                        value = githubUrl,
                        onValueChange = { githubUrl = it },
                        label = if (isTechProject) "GitHub URL" else "Source URL",
                        icon = Icons.Default.Code
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECTION 3: CATEGORY & TAGS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Categorization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("This helps people find your project.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))

                    Text("Project Type *", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    TechSelectionRow(PROJECT_TYPES, listOf(projectType)) { projectType = if (projectType == it) "" else it }

                    // --- Conditional Sections ---

                    // Languages
                    if (isTechProject && availableLanguages.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                        Spacer(Modifier.height(16.dp))
                        Text("Languages", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        TechSelectionRow(availableLanguages, selectedLanguages) {
                            selectedLanguages = if (selectedLanguages.contains(it)) selectedLanguages - it else selectedLanguages + it
                        }
                    }

                    // Tools / Tech Stack
                    if (projectType.isNotBlank()) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                        Spacer(Modifier.height(16.dp))

                        val label = if (isTechProject) "Tech Stack" else "Tools Used"
                        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))

                        if (availableToolsAndStacks.isNotEmpty()) {
                            TechSelectionRow(availableToolsAndStacks, selectedTechStacks) {
                                selectedTechStacks = if (selectedTechStacks.contains(it)) selectedTechStacks - it else selectedTechStacks + it
                            }
                        }
                    }

                    // General Tags
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("General Keywords", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    TechSelectionRow(allTags, selectedCategories) {
                        selectedCategories = if (selectedCategories.contains(it)) selectedCategories - it else selectedCategories + it
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// --- HELPER COMPONENTS ---

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        ),
        singleLine = singleLine,
        minLines = minLines
    )
}

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
                    containerColor = Color(0xFFF0F2F5), // Subtle grey
                    labelColor = Color.Black
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if(isSelected) Color.Transparent else Color.LightGray.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

// Custom Modifier for Dashed Border
fun Modifier.dashedBorder(strokeWidth: androidx.compose.ui.unit.Dp, color: Color, cornerRadiusDp: androidx.compose.ui.unit.Dp) = drawBehind {
    val stroke = Stroke(width = strokeWidth.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    drawRoundRect(color = color, style = stroke, cornerRadius = CornerRadius(cornerRadiusDp.toPx()))
}
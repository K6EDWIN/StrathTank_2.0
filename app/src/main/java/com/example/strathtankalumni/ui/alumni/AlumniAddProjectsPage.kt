package com.example.strathtankalumni.ui.alumni

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest

// UI Theme sealed class
sealed class UITheme(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val cardColor: Color,
    val accent: Color,
    val name: String
) {
    object Sky : UITheme(
        primary = Color(0xFF3B82F6),
        secondary = Color(0xFF93C5FD),
        background = Color(0xFFF0F9FF),
        cardColor = Color.White,
        accent = Color(0xFF0EA5E9),
        name = "Sky"
    )
    
    object Ocean : UITheme(
        primary = Color(0xFF06B6D4),
        secondary = Color(0xFF67E8F9),
        background = Color(0xFFECFEFF),
        cardColor = Color.White,
        accent = Color(0xFF0891B2),
        name = "Ocean"
    )
    
    object Ice : UITheme(
        primary = Color(0xFF60A5FA),
        secondary = Color(0xFFBAE6FD),
        background = Color(0xFFF8FAFC),
        cardColor = Color(0xFFFEFEFE),
        accent = Color(0xFF3B82F6),
        name = "Ice"
    )
    
    object Azure : UITheme(
        primary = Color(0xFF2563EB),
        secondary = Color(0xFF93C5FD),
        background = Color(0xFFEFF6FF),
        cardColor = Color.White,
        accent = Color(0xFF1D4ED8),
        name = "Azure"
    )
    
    object Crystal : UITheme(
        primary = Color(0xFF38BDF8),
        secondary = Color(0xFFBAE6FD),
        background = Color(0xFFFAFBFF),
        cardColor = Color(0xFFFFFFFF),
        accent = Color(0xFF0284C7),
        name = "Crystal"
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlumniAddProjectsPage(
    navController: NavHostController,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    
    // Set Crystal as the fixed theme
    val currentTheme = UITheme.Crystal
    
    // State variables
    var projectImage by remember { mutableStateOf<Uri?>(null) }
    var projectTitle by remember { mutableStateOf("") }
    var projectDescription by remember { mutableStateOf("") }
    var githubLink by remember { mutableStateOf("") }
    var isGithubLink by remember { mutableStateOf(false) }
    var otherLinks by remember { mutableStateOf<List<Pair<String, Boolean>>>(emptyList()) }
    var newLink by remember { mutableStateOf("") }
    var newLinkIsGithub by remember { mutableStateOf(false) }
    var projectTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var newTag by remember { mutableStateOf("") }
    var projectCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { projectImage = it }
    }
    
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add New Project",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1A1A1A)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(currentTheme.background, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = currentTheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier.shadow(0.5.dp)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Button(
                    onClick = {
                        Toast.makeText(context, "Project created successfully!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = currentTheme.primary,
                        disabledContainerColor = Color(0xFFE0E0E0)
                    ),
                    enabled = projectTitle.isNotBlank() && projectDescription.isNotBlank(),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Publish Project", 
                        color = Color.White, 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(currentTheme.background)
                    .padding(
                        top = 0.dp,
                        bottom = innerPadding.calculateBottomPadding(),
                        start = 0.dp,
                        end = 0.dp
                    )
                    .verticalScroll(rememberScrollState())
            ) {
                // Enhanced Backdrop Image Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (projectImage != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(projectImage)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Project banner",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    currentTheme.primary,
                                                    currentTheme.accent
                                                )
                                            )
                                        )
                                ) {
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.White.copy(alpha = 0.25f),
                                            modifier = Modifier.size(80.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddAPhoto,
                                                contentDescription = "Add image",
                                                modifier = Modifier
                                                    .padding(20.dp)
                                                    .size(40.dp),
                                                tint = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "Add Project Image",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "Tap to upload",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.7f)
                                            ),
                                            startY = 400f
                                        )
                                    )
                            )
                            
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                OutlinedTextField(
                                    value = projectTitle,
                                    onValueChange = { projectTitle = it },
                                    placeholder = { 
                                        Text(
                                            "Project Title",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        ) 
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                                        cursorColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 22.sp
                                    )
                                )
                            }
                        }
                    }
                }
                
                // Tags Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "Tags",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTag,
                            onValueChange = { newTag = it },
                            placeholder = { 
                                Text(
                                    "Add tag (e.g., React, AI, Mobile)",
                                    color = Color(0xFF9E9E9E),
                                    fontSize = 14.sp
                                ) 
                            },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = currentTheme.primary,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedContainerColor = currentTheme.cardColor,
                                unfocusedContainerColor = currentTheme.cardColor,
                                cursorColor = currentTheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        IconButton(
                            onClick = {
                                if (newTag.isNotBlank()) {
                                    projectTags = projectTags + newTag.trim()
                                    newTag = ""
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    currentTheme.primary,
                                    RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add tag",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    if (projectTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            projectTags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = currentTheme.secondary.copy(alpha = 0.3f),
                                    modifier = Modifier.clickable {
                                        projectTags = projectTags - tag
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            color = currentTheme.accent,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier.size(16.dp),
                                            tint = currentTheme.accent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Main Content Cards
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Description Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = currentTheme.secondary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = currentTheme.primary,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Project Description",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF1A1A1A)
                                )
                            }
                            
                            OutlinedTextField(
                                value = projectDescription,
                                onValueChange = { projectDescription = it },
                                placeholder = { 
                                    Text(
                                        "Describe your project, its features, and purpose...",
                                        color = Color(0xFF9E9E9E)
                                    ) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = currentTheme.primary,
                                    unfocusedBorderColor = Color(0xFFE8E8E8),
                                    focusedContainerColor = currentTheme.background,
                                    unfocusedContainerColor = currentTheme.background,
                                    cursorColor = currentTheme.primary
                                ),
                                maxLines = 6
                            )
                        }
                    }
                    
                    // Links Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = currentTheme.secondary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = currentTheme.primary,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Project Links",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF1A1A1A)
                                )
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isGithubLink,
                                    onCheckedChange = { isGithubLink = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = currentTheme.primary,
                                        uncheckedColor = Color(0xFFCCCCCC)
                                    )
                                )
                                Text(
                                    text = "GitHub Repository",
                                    fontSize = 15.sp,
                                    color = Color(0xFF1A1A1A),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            OutlinedTextField(
                                value = githubLink,
                                onValueChange = { githubLink = it },
                                placeholder = { 
                                    Text(
                                        "https://github.com/username/project",
                                        color = Color(0xFF9E9E9E)
                                    ) 
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Code, 
                                        contentDescription = "GitHub",
                                        tint = if (isGithubLink) currentTheme.primary else Color(0xFF9E9E9E)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = currentTheme.primary,
                                    unfocusedBorderColor = Color(0xFFE8E8E8),
                                    focusedContainerColor = currentTheme.background,
                                    unfocusedContainerColor = currentTheme.background,
                                    disabledBorderColor = Color(0xFFE8E8E8),
                                    disabledContainerColor = Color(0xFFF5F5F5),
                                    cursorColor = currentTheme.primary
                                ),
                                enabled = isGithubLink,
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Divider(color = currentTheme.background, thickness = 2.dp)
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Text(
                                text = "Other Links",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color(0xFF666666),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newLink,
                                    onValueChange = { newLink = it },
                                    placeholder = { 
                                        Text(
                                            "Add link...",
                                            color = Color(0xFF9E9E9E)
                                        ) 
                                    },
                                    leadingIcon = { 
                                        Icon(
                                            Icons.Default.Link, 
                                            contentDescription = "Link",
                                            tint = Color(0xFF9E9E9E)
                                        ) 
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = currentTheme.primary,
                                        unfocusedBorderColor = Color(0xFFE8E8E8),
                                        focusedContainerColor = currentTheme.background,
                                        unfocusedContainerColor = currentTheme.background,
                                        cursorColor = currentTheme.primary
                                    ),
                                    singleLine = true
                                )
                                
                                IconButton(
                                    onClick = {
                                        if (newLink.isNotBlank()) {
                                            otherLinks = otherLinks + Pair(newLink, newLinkIsGithub)
                                            newLink = ""
                                            newLinkIsGithub = false
                                        }
                                    },
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(
                                            currentTheme.primary,
                                            RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add link",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            if (newLink.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = newLinkIsGithub,
                                        onCheckedChange = { newLinkIsGithub = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = currentTheme.primary
                                        )
                                    )
                                    Text(
                                        text = "Mark as GitHub link",
                                        fontSize = 13.sp,
                                        color = Color(0xFF757575)
                                    )
                                }
                            }
                            
                            if (otherLinks.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                otherLinks.forEach { (link, isGithub) ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = currentTheme.background,
                                        tonalElevation = 0.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = currentTheme.secondary.copy(alpha = 0.3f),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    if (isGithub) Icons.Default.Code else Icons.Default.Link,
                                                    contentDescription = null,
                                                    tint = currentTheme.primary,
                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = link,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontSize = 14.sp,
                                                color = Color(0xFF1A1A1A)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = { otherLinks = otherLinks.filter { it.first != link } },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = Color(0xFF9E9E9E),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Categories Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = currentTheme.cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = currentTheme.secondary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        tint = currentTheme.primary,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Categories",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF1A1A1A)
                                )
                            }
                            
                            val categories = listOf("AI", "WebApp", "Mobile", "Design", "IoT", "Blockchain", "Game Dev", "Other")
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                categories.forEach { category ->
                                    val isSelected = category in projectCategories
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            projectCategories = if (isSelected) {
                                                projectCategories - category
                                            } else {
                                                projectCategories + category
                                            }
                                        },
                                        enabled = true,
                                        label = { 
                                            Text(
                                                category,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                            ) 
                                        },
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
                                            selectedContainerColor = currentTheme.primary,
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White,
                                            containerColor = currentTheme.background,
                                            labelColor = Color(0xFF666666)
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
 
    }
}
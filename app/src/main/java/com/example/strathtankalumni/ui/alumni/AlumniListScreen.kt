package com.example.strathtankalumni.ui.alumni

import androidx.compose.foundation.LocalIndication // ✅ IMPORT ADDED
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource // ✅ IMPORT ADDED
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // ✅ CHANGED
import androidx.compose.material.icons.filled.Search
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.strathtankalumni.R
import com.example.strathtankalumni.data.User
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniListScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val allAlumni by authViewModel.alumniList.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        authViewModel.fetchAllAlumni()
    }

    val filteredList = remember(searchQuery, allAlumni) {
        if (searchQuery.isBlank()) {
            allAlumni
        } else {
            allAlumni.filter {
                it.firstName.contains(searchQuery, ignoreCase = true) ||
                        it.lastName.contains(searchQuery, ignoreCase = true) ||
                        it.email.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Alumni") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") // ✅ CHANGED
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by name or email...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color(0xFFF1F3F4),
                    disabledIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(filteredList) { user ->
                    AlumniListItem(
                        user = user,
                        onClick = {
                            navController.navigate(Screen.OtherProfile.createRoute(user.userId))
                        }
                    )
                    Divider()
                }
            }
        }
    }
}

/**
 * A Composable for displaying a single user in the list.
 */
@Composable
fun AlumniListItem(
    user: User,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // ✅ THIS IS THE FIX
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.profilePhotoUrl.takeIf { !it.isBlank() } ?: R.drawable.noprofile)
                .crossfade(true)
                .build(),
            contentDescription = "Profile photo",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F3F4)),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.noprofile)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${user.firstName} ${user.lastName}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            val userDetails = buildString {
                if (user.degree.isNotBlank()) append(user.degree)
                if (user.graduationYear.isNotBlank()) {
                    if (this.isNotEmpty()) append(" | ")
                    append("Class of ${user.graduationYear}")
                }
            }.ifEmpty { user.email }

            Text(
                text = userDetails,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
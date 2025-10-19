package com.example.strathtankalumni.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.strathtankalumni.data.User
import com.example.strathtankalumni.navigation.Screen
import com.example.strathtankalumni.viewmodel.AuthViewModel
import com.example.strathtankalumni.viewmodel.AuthState
import com.example.strathtankalumni.util.UniversityData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()


    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var universityName by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf("") }

    // State for recommendation drop-downs
    var isCountryDropdownExpanded by remember { mutableStateOf(false) }
    var isDegreeDropdownExpanded by remember { mutableStateOf(false) }

    // State for University suggestions based on Country
    val universitySuggestions = remember(country, universityName) {
        UniversityData.getUniversitiesForCountry(country).filter {
            it.contains(universityName, ignoreCase = true)
        }
    }

    // Handle AuthState changes
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                // Show success toast
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                // Navigate back to Login after successful registration
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                }
                authViewModel.resetAuthState()
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetAuthState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alumni Registration", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF00796B))
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Create Alumni Profile",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF00796B),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Country Dropdown
            item {
                OutlinedTextField(
                    value = country.ifEmpty { "Select Country" },
                    onValueChange = {}, // Read-only via typing
                    label = { Text("Country") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown",
                            Modifier.clickable { isCountryDropdownExpanded = true }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = isCountryDropdownExpanded,
                    onDismissRequest = { isCountryDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    UniversityData.countries.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                country = selection
                                universityName = "" // Reset university when country changes
                                isCountryDropdownExpanded = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // University Name Input with Suggestions
            item {
                OutlinedTextField(
                    value = universityName,
                    onValueChange = { universityName = it },
                    label = { Text("University Name (Start Typing)") },
                    placeholder = { Text(if (country.isEmpty()) "Select Country First" else "e.g., Strathmore University") },
                    enabled = country.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                // Displaying suggestions below the University input field
                if (universityName.isNotEmpty() && universitySuggestions.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-8).dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            universitySuggestions.take(5).forEach { suggestion ->
                                Text(
                                    text = suggestion,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { universityName = suggestion }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Degree Dropdown
            item {
                OutlinedTextField(
                    value = degree.ifEmpty { "Select Degree" },
                    onValueChange = {},
                    label = { Text("Degree") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown",
                            Modifier.clickable { isDegreeDropdownExpanded = true }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = isDegreeDropdownExpanded,
                    onDismissRequest = { isDegreeDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    UniversityData.sampleDegrees.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                degree = selection
                                isDegreeDropdownExpanded = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Registration Button
            item {
                Button(
                    onClick = {
                        if (password != confirmPassword) {
                            Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (country.isBlank() || universityName.isBlank() || degree.isBlank() || firstName.isBlank() || lastName.isBlank()) {
                            Toast.makeText(context, "Please complete all fields.", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        // Password length check
                        if (password.length < 6) {
                            Toast.makeText(context, "Password must be at least 6 characters.", Toast.LENGTH_LONG).show()
                            return@Button
                        }


                        val newUser = User(
                            email = email,
                            firstName = firstName,
                            lastName = lastName,
                            country = country,
                            universityName = universityName,
                            degree = degree,
                            role = "alumni" // Set default role
                        )
                        authViewModel.registerUser(newUser, password)
                    },
                    enabled = authState != AuthState.Loading,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (authState == AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Register as Alumni", style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Back to Login Link
            item {
                TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                    Text("Already have an account? Log In")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

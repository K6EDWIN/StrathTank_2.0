package com.example.strathtankalumni.ui.auth

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MailOutline
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
import java.util.Calendar
private val PrimaryBlue = Color(0xFF1976D2)
private val DarkText = Color(0xFF212121)

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

    var graduationYear by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("alumni") } // Default to alumni


    var isCountryDropdownExpanded by remember { mutableStateOf(false) }
    var isDegreeDropdownExpanded by remember { mutableStateOf(false) }
    var isYearDropdownExpanded by remember { mutableStateOf(false) }
    var isRoleDropdownExpanded by remember { mutableStateOf(false) }

    // Dynamic lists
    val universitySuggestions = remember(country, universityName) {
        UniversityData.getUniversitiesForCountry(country).filter {
            it.contains(universityName, ignoreCase = true)
        }
    }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val graduationYears = remember { (currentYear downTo 1970).map { it.toString() } }
    val roles = listOf("alumni", "admin")



    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {

                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()

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


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {

            Text(
                text = "Registration",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = PrimaryBlue,
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
                leadingIcon = { Icon(Icons.Filled.MailOutline, contentDescription = "Email", tint = PrimaryBlue) },
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
                onValueChange = {},
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


        item {
            OutlinedTextField(
                value = universityName,
                onValueChange = { universityName = it },
                label = { Text("University Name") },
                placeholder = { Text(if (country.isEmpty()) "Select Country First" else "e.g., Strathmore University") },
                enabled = country.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            )

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
            Spacer(modifier = Modifier.height(8.dp))
        }


        item {
            OutlinedTextField(
                value = graduationYear.ifEmpty { "Graduated In:" },
                onValueChange = {},
                label = { Text("Graduation Year") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown",
                        Modifier.clickable { isYearDropdownExpanded = true }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
                expanded = isYearDropdownExpanded,
                onDismissRequest = { isYearDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                graduationYears.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year) },
                        onClick = {
                            graduationYear = year
                            isYearDropdownExpanded = false
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }


        item {
            OutlinedTextField(
                value = role.ifEmpty { "Select Role" },
                onValueChange = {},
                label = { Text("Role") },
                readOnly = true,
                trailingIcon = {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown",
                        Modifier.clickable { isRoleDropdownExpanded = true }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
                expanded = isRoleDropdownExpanded,
                onDismissRequest = { isRoleDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                roles.forEach { selection ->
                    DropdownMenuItem(
                        text = { Text(selection) },
                        onClick = {
                            role = selection
                            isRoleDropdownExpanded = false
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }


        item {
            Button(
                onClick = {
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (country.isBlank() || universityName.isBlank() || degree.isBlank() || firstName.isBlank() || lastName.isBlank() || graduationYear.isBlank() || role.isBlank()) {
                        Toast.makeText(context, "Please complete all fields.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // Password length check
                    if (password.length < 8) {
                        Toast.makeText(context, "Password must be at least 8 characters.", Toast.LENGTH_LONG).show()
                        return@Button
                    }


                    val newUser = User(
                        email = email,
                        firstName = firstName,
                        lastName = lastName,
                        country = country,
                        universityName = universityName,
                        degree = degree,
                        graduationYear = graduationYear,
                        role = role
                    )
                    authViewModel.registerUser(newUser, password)
                },
                enabled = authState != AuthState.Loading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (authState == AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {

                    Text("Register", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Login
        item {
            TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                Text("Already have an account? Log In", color = DarkText)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

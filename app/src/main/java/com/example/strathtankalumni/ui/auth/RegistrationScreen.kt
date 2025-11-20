package com.example.strathtankalumni.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.strathtankalumni.util.UniversityData
import com.example.strathtankalumni.viewmodel.AuthState
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var country by remember { mutableStateOf(UniversityData.countries.first()) }
    var universityName by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf("") }
    var graduationYear by remember { mutableStateOf("") }
    val role by remember { mutableStateOf("alumni") }

    var countryMenuExpanded by remember { mutableStateOf(false) }
    val availableUniversities = remember(country) { UniversityData.getUniversitiesForCountry(country) }
    var universityMenuExpanded by remember { mutableStateOf(false) }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (1980..currentYear).map { it.toString() }.reversed()
    var yearMenuExpanded by remember { mutableStateOf(false) }

    // Observe authState for navigation and feedback
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                // Navigate to login after successful registration
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Welcome.route) { inclusive = true }
                }
                authViewModel.resetAuthState()
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetAuthState()
            }
            AuthState.Loading, AuthState.Idle -> Unit
        }
    }

    val onRegisterClicked: () -> Unit = {
        if (password != confirmPassword) {
            Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
        } else {
            // Check if fields are filled
            if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank() || universityName.isBlank() || graduationYear.isBlank()) {
                Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_LONG).show()
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
        }
    }

    // Determine loading state from AuthState
    val isLoading = authState == AuthState.Loading

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Create Account", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text("Join our alumni network", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(32.dp))
        }

        item { OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth()) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth()) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, leadingIcon = { Icon(Icons.Default.MailOutline, null) }, modifier = Modifier.fillMaxWidth()) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirm Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { OutlinedTextField(value = degree, onValueChange = { degree = it }, label = { Text("Degree (e.g., BSc. Computer Science)") }, modifier = Modifier.fillMaxWidth()) }
        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            ExposedDropdownMenuBox(expanded = countryMenuExpanded, onExpandedChange = { countryMenuExpanded = !countryMenuExpanded }) {
                OutlinedTextField(value = country, onValueChange = {}, readOnly = true, label = { Text("Country") }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = countryMenuExpanded, onDismissRequest = { countryMenuExpanded = false }) {
                    UniversityData.countries.forEach { selectionOption ->
                        DropdownMenuItem(text = { Text(selectionOption) }, onClick = { country = selectionOption; countryMenuExpanded = false; universityName = "" })
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            ExposedDropdownMenuBox(expanded = universityMenuExpanded, onExpandedChange = { universityMenuExpanded = !universityMenuExpanded }) {
                OutlinedTextField(value = universityName, onValueChange = {}, readOnly = true, label = { Text("University") }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = universityMenuExpanded, onDismissRequest = { universityMenuExpanded = false }) {
                    availableUniversities.forEach { selectionOption ->
                        DropdownMenuItem(text = { Text(selectionOption) }, onClick = { universityName = selectionOption; universityMenuExpanded = false })
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            ExposedDropdownMenuBox(expanded = yearMenuExpanded, onExpandedChange = { yearMenuExpanded = !yearMenuExpanded }) {
                OutlinedTextField(value = graduationYear, onValueChange = {}, readOnly = true, label = { Text("Graduation Year") }, trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = yearMenuExpanded, onDismissRequest = { yearMenuExpanded = false }) {
                    years.forEach { selectionOption ->
                        DropdownMenuItem(text = { Text(selectionOption) }, onClick = { graduationYear = selectionOption; yearMenuExpanded = false })
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }

        item {
            Button(
                onClick = onRegisterClicked,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Register", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                Text("Already have an account? Log In", color = MaterialTheme.colorScheme.onBackground)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
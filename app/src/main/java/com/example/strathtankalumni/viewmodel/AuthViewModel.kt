package com.example.strathtankalumni.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.strathtankalumni.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// authentication state feedback
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    // UPDATED: Now passes the user's role on success instead of a generic boolean
    data class Success(val message: String, val userRole: String? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    // Initialize Firestore instance
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // register a new user and save their full profile to Firestore
    fun registerUser(
        user: User,
        password: String
    ) {
        if (user.email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty.")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {

                val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    // Ensures the role is saved based on the registration input (alumni or admin)
                    val userProfile = user.copy(userId = firebaseUser.uid)


                    firestore.collection("users")
                        .document(firebaseUser.uid)
                        .set(userProfile.toMap())
                        .await()

                    // Registration success, but no automatic navigation home
                    _authState.value = AuthState.Success("Registration successful! Waiting for Admin approval.")
                } else {
                    _authState.value = AuthState.Error("Firebase registration failed.")
                }
            } catch (e: Exception) {

                _authState.value = AuthState.Error("Registration failed: ${e.message}")
            }
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please enter both email and password.")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {

                auth.signInWithEmailAndPassword(email, password).await()
                val uid = auth.currentUser?.uid

                if (uid != null) {

                    val userDoc = firestore.collection("users").document(uid).get().await()

                    if (userDoc.exists()) {
                        val user = userDoc.toObject(User::class.java)

                        // Check the role and pass it in the Success state
                        when (user?.role) {
                            "alumni" -> {
                                // Navigate to Alumni Home
                                _authState.value = AuthState.Success("Login successful!", userRole = "alumni")
                            }
                            "admin" -> {
                                // Navigate to Admin Home
                                _authState.value = AuthState.Success("Admin login successful!", userRole = "admin")
                            }
                            // Assuming all new users default to "alumni" in RegistrationScreen
                            else -> {
                                // Catch case for unapproved/pending accounts if the role logic is more complex
                                _authState.value = AuthState.Error("Account status pending or role not recognized.")
                            }
                        }
                    } else {
                        _authState.value = AuthState.Error("User profile data not found.")
                    }
                }
            } catch (e: Exception) {
                // Firebase exception for bad credentials
                _authState.value = AuthState.Error("Login failed: Invalid email or password.")
            }
        }
    }


    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

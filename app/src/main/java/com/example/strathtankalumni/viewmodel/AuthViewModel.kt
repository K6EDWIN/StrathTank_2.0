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

    data class Success(val message: String, val userRole: String? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    // Initialize Firestore instance
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

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
                // Create the user in Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    // Send the verification email
                    firebaseUser.sendEmailVerification().await()

                    // Save the user's data to Firestore
                    val newUser = user.copy(userId = firebaseUser.uid)
                    firestore.collection("users").document(firebaseUser.uid).set(newUser).await()

                    //Sign the user out
                    auth.signOut()

                    //Send success state to the UI
                    _authState.value = AuthState.Success(
                        message = "Registration successful! Please check your email to verify your account.",
                        userRole = null
                    )
                } else {
                    _authState.value = AuthState.Error("User creation failed.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty.")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                // Sign in the user
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {

                    // CHECK IF EMAIL IS VERIFIED
                    if (firebaseUser.isEmailVerified) {
                        // Fetch user document from Firestore
                        val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                        val user = userDoc.toObject(User::class.java)

                        if (user != null) {
                            // Check the role and pass it in the Success state
                            when (user.role) {
                                "alumni" -> {
                                    _authState.value = AuthState.Success("Login successful!", userRole = "alumni")
                                }
                                "admin" -> {
                                    _authState.value = AuthState.Success("Admin login successful!", userRole = "admin")
                                }
                                else -> {
                                    _authState.value = AuthState.Error("Role not recognized.")
                                }
                            }
                        } else {
                            _authState.value = AuthState.Error("User profile data not found.")
                        }
                    } else {
                        // email is NOT verified, sign out and show error
                        auth.signOut()
                        _authState.value = AuthState.Error("Please verify your email before logging in.")
                    }
                }
            } catch (e: Exception) {
                // Firebase exception for bad credentials
                _authState.value = AuthState.Error("Login failed: Invalid email or password.")
            }
        }
    }

    //Sign out
    fun signOut() {
        auth.signOut()
    }


    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
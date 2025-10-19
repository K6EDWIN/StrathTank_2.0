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

// Enum for handling authentication state feedback
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String, val navigateToHome: Boolean = false) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    // Initialize Firestore instance
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Function to register a new user and save their full profile to Firestore
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
                    val userProfile = user.copy(userId = firebaseUser.uid)


                    firestore.collection("users")
                        .document(firebaseUser.uid)
                        .set(userProfile.toMap())
                        .await()

                    _authState.value = AuthState.Success("Registration successful! Waiting for Admin approval.", navigateToHome = false)
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

                        if (user?.role == "alumni") {
                            _authState.value = AuthState.Success("Login successful!", navigateToHome = true)
                        } else if (user?.role == "pending") {

                            _authState.value = AuthState.Error("Your account is pending admin approval.")
                        } else {
                            _authState.value = AuthState.Error("Account verification required. Please contact admin.")
                        }
                    } else {

                        _authState.value = AuthState.Error("User profile data not found.")
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Login failed: Invalid email or password.")
            }
        }
    }


    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

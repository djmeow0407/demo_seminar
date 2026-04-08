package com.demo.seminar.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.seminar.data.firebase.FirebaseService
import com.demo.seminar.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class AuthEvent {
    data class Register(val email: String, val password: String) : AuthEvent()
    data class Login(val email: String, val password: String) : AuthEvent()
    object Logout : AuthEvent()
}

class AuthViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val repository = AuthRepository(firebaseService)

    val _authState = MutableSharedFlow<AuthState>()
    val authState: SharedFlow<AuthState> = _authState

    fun emitState(state: AuthState) {
        viewModelScope.launch {
            _authState.emit(state)
        }
    }

    fun register(email: String, password: String) {
        emitState(AuthState.Loading)
        viewModelScope.launch {
            val result = repository.register(email, password)
            result.onSuccess { message ->
                emitState(AuthState.Success(message))
            }.onFailure { e ->
                val errorMessage = when (e) {
                    is FirebaseAuthException -> when (e.errorCode) {
                        "weak-password" -> "Password is too weak"
                        "email-already-in-use" -> "Email already registered"
                        else -> "Registration failed: ${e.message}"
                    }
                    else -> "Registration failed: ${e.message}"
                }
                Log.e("AuthViewModel", "Register error", e)
                emitState(AuthState.Error(errorMessage))
            }
        }
    }

    fun login(email: String, password: String) {
        emitState(AuthState.Loading)
        viewModelScope.launch {
            val result = repository.login(email, password)
            result.onSuccess { message ->
                emitState(AuthState.Success(message))
            }.onFailure { e ->
                val errorMessage = when (e) {
                    is FirebaseAuthException -> when (e.errorCode) {
                        "invalid-email" -> "Invalid email address"
                        "user-disabled" -> "User account disabled"
                        "wrong-password" -> "Wrong password"
                        else -> "Login failed: ${e.message}"
                    }
                    else -> "Login failed: ${e.message}"
                }
                Log.e("AuthViewModel", "Login error", e)
                emitState(AuthState.Error(errorMessage))
            }
        }
    }

    fun logout() {
        emitState(AuthState.Loading)
        viewModelScope.launch {
            val result = repository.logout()
            result.onSuccess {
                emitState(AuthState.Success("Logged out"))
            }.onFailure { e ->
                Log.e("AuthViewModel", "Logout error", e)
                emitState(AuthState.Error("Logout failed: ${e.message}"))
            }
        }
    }

    fun getCurrentUserId(): String? {
        return repository.getCurrentUser()
    }

    fun getCurrentUserEmail(): String? {
        return repository.getCurrentUserEmail()
    }

    fun isUserLoggedIn(): Boolean {
        return repository.isUserLoggedIn()
    }
}

package com.demo.seminar.data.repository

import com.demo.seminar.data.firebase.FirebaseService
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await

class AuthRepository(private val firebaseService: FirebaseService) {

    suspend fun register(email: String, password: String): Result<String> {
        return try {
            val result = firebaseService.auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            user?.let {
                // Create Firestore user document with balance = 0
                val userData = mapOf(
                    "email" to it.email!!,
                    "balance" to 0L,
                    "createdAt" to System.currentTimeMillis()
                )
                firebaseService.firestore.collection("users")
                    .document(it.uid).set(userData).await()
            }
            Result.success("Registration successful")
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Password is too weak"))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("Email already registered"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<String> {
        return try {
            firebaseService.auth.signInWithEmailAndPassword(email, password).await()
            Result.success("Login successful")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            firebaseService.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser(): String? {
        return firebaseService.auth.currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return firebaseService.auth.currentUser?.email
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseService.auth.currentUser != null
    }
}

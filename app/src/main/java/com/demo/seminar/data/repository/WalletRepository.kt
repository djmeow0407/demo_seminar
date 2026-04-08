package com.demo.seminar.data.repository

import com.demo.seminar.data.firebase.FirebaseService
import com.demo.seminar.model.Transaction
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class WalletRepository(private val firebaseService: FirebaseService) {

    suspend fun getBalance(userId: String): Result<Long> {
        return try {
            val document = firebaseService.firestore.collection("users")
                .document(userId).get().await()
            val balance = document.getLong("balance") ?: 0L
            Result.success(balance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addIncome(userId: String, amount: Long, category: String): Result<String> {
        return try {
            val userRef = firebaseService.firestore.collection("users").document(userId)
            val transactionRef = firebaseService.firestore.collection("transactions").document()

            firebaseService.firestore.runTransaction { transaction ->
                transaction.update(userRef, "balance", FieldValue.increment(amount))
                
                val transactionData = Transaction(
                    userId = userId,
                    type = "income",
                    amount = amount,
                    category = category,
                    createdAt = System.currentTimeMillis()
                )
                transaction.set(transactionRef, transactionData)
            }.await()

            Result.success("Income added successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addExpense(userId: String, amount: Long, category: String): Result<String> {
        return try {
            val userRef = firebaseService.firestore.collection("users").document(userId)
            val transactionRef = firebaseService.firestore.collection("transactions").document()

            firebaseService.firestore.runTransaction { transaction ->
                transaction.update(userRef, "balance", FieldValue.increment(-amount))
                
                val transactionData = Transaction(
                    userId = userId,
                    type = "expense",
                    amount = amount,
                    category = category,
                    createdAt = System.currentTimeMillis()
                )
                transaction.set(transactionRef, transactionData)
            }.await()

            Result.success("Expense added successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactionHistory(userId: String): Result<List<Transaction>> {
        return try {
            val transactions = firebaseService.firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()

            val list = transactions.map { doc ->
                Transaction(
                    userId = doc.getString("userId") ?: "",
                    type = doc.getString("type") ?: "",
                    amount = doc.getLong("amount") ?: 0L,
                    category = doc.getString("category") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenToBalance(userId: String, callback: (Long) -> Unit) {
        firebaseService.firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val balance = snapshot?.getLong("balance") ?: 0L
                callback(balance)
            }
    }

    fun listenToTransactionHistory(userId: String, callback: (List<Transaction>) -> Unit) {
        firebaseService.firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val list = snapshot?.map { doc ->
                    Transaction(
                        userId = doc.getString("userId") ?: "",
                        type = doc.getString("type") ?: "",
                        amount = doc.getLong("amount") ?: 0L,
                        category = doc.getString("category") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                } ?: emptyList()

                callback(list)
            }
    }
}

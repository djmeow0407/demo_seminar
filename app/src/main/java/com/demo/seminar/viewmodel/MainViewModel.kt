package com.demo.seminar.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.seminar.data.firebase.FirebaseService
import com.demo.seminar.data.repository.WalletRepository
import com.demo.seminar.model.Transaction
import com.demo.seminar.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class WalletState {
    object Idle : WalletState()
    object Loading : WalletState()
    data class Success(val message: String) : WalletState()
    data class Error(val message: String) : WalletState()
}

class MainViewModel : ViewModel() {
    private val firebaseService = FirebaseService()
    private val repository = WalletRepository(firebaseService)

    private val _balance = MutableStateFlow(0L)
    val balance: StateFlow<Long> = _balance

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _walletState = MutableStateFlow<WalletState>(WalletState.Idle)
    val walletState: StateFlow<WalletState> = _walletState

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage

    fun setUserId(userId: String) {
        this.userId = userId
    }

    private var userId: String? = null

    fun initialize() {
        userId?.let { loadUserData(it) }
    }

    private fun loadUserData(userId: String) {
        viewModelScope.launch {
            // Get initial balance
            repository.getBalance(userId).onSuccess { balance ->
                _balance.value = balance
            }.onFailure { e ->
                Log.e("MainViewModel", "Error getting balance", e)
            }

            // Get initial transaction history
            repository.getTransactionHistory(userId).onSuccess { list ->
                _transactions.value = list
            }.onFailure { e ->
                Log.e("MainViewModel", "Error getting transactions", e)
            }

            // Set up real-time listeners
            repository.listenToBalance(userId) { newBalance ->
                _balance.value = newBalance
            }

            repository.listenToTransactionHistory(userId) { newTransactions ->
                _transactions.value = newTransactions
            }
        }
    }

    fun addIncome(amount: Long, category: String) {
        userId?.let { performWalletOperation { repository.addIncome(it, amount, category) } }
    }

    fun addExpense(amount: Long, category: String) {
        userId?.let { performWalletOperation { repository.addExpense(it, amount, category) } }
    }

    private fun performWalletOperation(operation: suspend () -> Result<String>) {
        viewModelScope.launch {
            _walletState.value = WalletState.Loading

            val result = operation()

            result.onSuccess { message ->
                _walletState.value = WalletState.Success(message)
                _uiMessage.value = message
                // Refresh data after successful operation
                userId?.let { loadUserData(it) }
            }.onFailure { e ->
                _walletState.value = WalletState.Error(e.message ?: "Unknown error")
                _uiMessage.value = e.message ?: "Unknown error"
                Log.e("MainViewModel", "Wallet operation error", e)
            }
        }
    }

    fun clearMessage() {
        _uiMessage.value = null
    }
}

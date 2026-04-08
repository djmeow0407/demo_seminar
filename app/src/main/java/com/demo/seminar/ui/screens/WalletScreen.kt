package com.demo.seminar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.demo.seminar.model.Transaction
import com.demo.seminar.viewmodel.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val balance by viewModel.balance.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val walletState by viewModel.walletState.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    var incomeAmount by remember { mutableStateOf("") }
    var incomeCategory by remember { mutableStateOf("") }

    var expenseAmount by remember { mutableStateOf("") }
    var expenseCategory by remember { mutableStateOf("") }

    // Show snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Wallet") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Current Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = String.format("$%d", balance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Error/Success Message
            uiMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar(message)
                    viewModel.clearMessage()
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        incomeAmount.toIntOrNull()?.let { amount ->
                            viewModel.addIncome(amount.toLong(), incomeCategory)
                        }
                        incomeAmount = ""
                        incomeCategory = ""
                    },
                    modifier = Modifier.weight(1f),
                    enabled = incomeAmount.isNotEmpty() && incomeCategory.isNotEmpty()
                ) {
                    Text("Add Income")
                }

                OutlinedButton(
                    onClick = {
                        expenseAmount.toIntOrNull()?.let { amount ->
                            viewModel.addExpense(amount.toLong(), expenseCategory)
                        }
                        expenseAmount = ""
                        expenseCategory = ""
                    },
                    modifier = Modifier.weight(1f),
                    enabled = expenseAmount.isNotEmpty() && expenseCategory.isNotEmpty()
                ) {
                    Text("Add Expense")
                }
            }

            // Amount Input Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = incomeAmount,
                        onValueChange = { incomeAmount = it },
                        label = { Text("Income Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                    OutlinedTextField(
                        value = incomeCategory,
                        onValueChange = { incomeCategory = it },
                        label = { Text("Category (e.g. Salary)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = { expenseAmount = it },
                        label = { Text("Expense Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                    OutlinedTextField(
                        value = expenseCategory,
                        onValueChange = { expenseCategory = it },
                        label = { Text("Category (e.g. Food)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Transaction History
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            if (transactions.isEmpty()) {
                Text(
                    text = "No transactions yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(transactions) { transaction ->
                        TransactionItem(transaction)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val isIncome = transaction.type == "income"
    val color = if (isIncome) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.category.uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "${transaction.type.capitalize()}: $${transaction.amount}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(transaction.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun String.capitalize(): String {
    return if (isEmpty()) {
        this
    } else {
        replaceFirstChar { it.uppercase() }
    }
}

package com.demo.seminar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.demo.seminar.ui.screens.LoginScreen
import com.demo.seminar.ui.screens.RegisterScreen
import com.demo.seminar.ui.screens.WalletScreen
import com.demo.seminar.viewmodel.AuthEvent
import com.demo.seminar.viewmodel.AuthViewModel
import com.demo.seminar.viewmodel.MainViewModel
import com.demo.seminar.ui.theme.Demo_seminarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Demo_seminarTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    val authViewModel: AuthViewModel = viewModel()

    var currentScreen by remember { mutableStateOf(if (authViewModel.isUserLoggedIn()) "wallet" else "login") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            "login" -> {
                if (authViewModel.isUserLoggedIn()) {
                    currentScreen = "wallet"
                } else {
                    LoginScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = { currentScreen = "wallet" },
                        onNavigateToRegister = { currentScreen = "register" }
                    )
                }
            }
            "register" -> {
                RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = { currentScreen = "wallet" },
                    onNavigateToLogin = { currentScreen = "login" }
                )
            }
            "wallet" -> {
                if (!authViewModel.isUserLoggedIn()) {
                    currentScreen = "login"
                } else {
                    val mainViewModel: MainViewModel = viewModel()
                    // Set user ID when wallet screen is loaded
                    val userId = authViewModel.getCurrentUserId()
                    if (userId != null) {
                        mainViewModel.setUserId(userId)
                        mainViewModel.initialize()
                    }
                    WalletScreen(
                        viewModel = mainViewModel,
                        onLogout = {
                            authViewModel.logout()
                            currentScreen = "login"
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    Demo_seminarTheme {
        App()
    }
}

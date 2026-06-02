package com.rostelcom.servicedesk.ui.login

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rostelcom.servicedesk.ui.theme.Purple
import com.rostelcom.servicedesk.util.SessionManager

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.loginState.collectAsState()
    val context = LocalContext.current
    val session = remember { SessionManager(context) }

    var askEnableBiometric by remember { mutableStateOf(false) }
    var showBiometricPrompt by remember { mutableStateOf(false) }
    var isCheckingBiometric by remember { mutableStateOf(true) }

    // После успешного входа — предлагаем биометрию, если ещё не включена
    LaunchedEffect(state) {
        if (state is LoginState.Success) {
            if (!session.biometricEnabled &&
                BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
            ) {
                askEnableBiometric = true
            } else {
                onLoginSuccess()
            }
        }
    }

    // При открытии экрана входа: если биометрия включена — сразу сканер
    LaunchedEffect(Unit) {
        if (session.isLoggedIn && session.biometricEnabled) {
            showBiometricPrompt = true
        }
        isCheckingBiometric = false
    }

    // Запуск сканера при повторном входе
    LaunchedEffect(showBiometricPrompt) {
        if (!showBiometricPrompt) return@LaunchedEffect
        val activity = context as? FragmentActivity ?: return@LaunchedEffect
        BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    session.lastActiveTime = System.currentTimeMillis()
                    showBiometricPrompt = false
                    onLoginSuccess()
                }
                override fun onAuthenticationFailed() {}
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    showBiometricPrompt = false
                }
            }
        ).authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Вход по отпечатку")
                .setNegativeButtonText("Войти по паролю")
                .build()
        )
    }

    // Диалог «Включить биометрию?»
    if (askEnableBiometric) {
        AlertDialog(
            onDismissRequest = { askEnableBiometric = false; onLoginSuccess() },
            title = { Text("Вход по отпечатку пальца") },
            text = { Text("Хотите использовать отпечаток для быстрого входа?") },
            confirmButton = {
                TextButton(onClick = {
                    askEnableBiometric = false
                    val activity = context as? FragmentActivity
                    if (activity != null) {
                        BiometricPrompt(
                            activity,
                            ContextCompat.getMainExecutor(context),
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    session.biometricEnabled = true
                                    session.lastActiveTime = System.currentTimeMillis()
                                    onLoginSuccess()
                                }
                                override fun onAuthenticationFailed() {
                                    Toast.makeText(context, "Не удалось распознать", Toast.LENGTH_SHORT).show()
                                }
                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                    session.biometricEnabled = false
                                    onLoginSuccess()
                                }
                            }
                        ).authenticate(
                            BiometricPrompt.PromptInfo.Builder()
                                .setTitle("Подтвердите отпечаток")
                                .setDescription("Приложите палец для включения входа по отпечатку")
                                .setNegativeButtonText("Пропустить")
                                .build()
                        )
                    } else {
                        onLoginSuccess()
                    }
                }) { Text("Да") }
            },
            dismissButton = {
                TextButton(onClick = {
                    askEnableBiometric = false
                    session.biometricEnabled = false
                    onLoginSuccess()
                }) { Text("Нет") }
            }
        )
    }

    // Пока проверяем, нужна ли биометрия — показываем загрузку, чтобы не мелькали экраны
    if (isCheckingBiometric) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = androidx.compose.ui.graphics.Color(0xFF9B30FF))
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📡 РОСТЕЛЕКОМ", fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("Сервисное обслуживание", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.signIn(email, password) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple)
        ) {
            Text("Войти", fontSize = 16.sp)
        }

        if (state is LoginState.Error) {
            Spacer(Modifier.height(16.dp))
            Text((state as LoginState.Error).message, color = MaterialTheme.colorScheme.error)
        }
    }
}
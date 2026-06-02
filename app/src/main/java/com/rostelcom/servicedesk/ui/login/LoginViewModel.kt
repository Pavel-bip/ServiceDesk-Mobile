package com.rostelcom.servicedesk.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.rostelcom.servicedesk.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Введите email и пароль")
            return
        }
        _loginState.value = LoginState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.getIdToken(false)?.addOnSuccessListener { result ->
                        val claims = result.claims
                        val role = claims["role"] as? String ?: ""
                        if (role == "field_engineer") {
                            _loginState.value = LoginState.Success
                        } else {
                            auth.signOut()
                            _loginState.value = LoginState.Error("Доступ запрещён. Только для инженеров.")
                        }
                    }?.addOnFailureListener {
                        auth.signOut()
                        _loginState.value = LoginState.Error("Ошибка проверки роли")
                    }
                } else {
                    val errorMsg = when (task.exception?.message) {
                        "The email address is badly formatted." -> "Некорректный email"
                        "There is no user record corresponding to this identifier. The user may have been deleted." -> "Пользователь не найден"
                        "The password is invalid or the user does not have a password." -> "Неверный пароль"
                        "The user account has been disabled by an administrator." -> "Аккаунт заблокирован"
                        "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> "Ошибка сети. Проверьте подключение к интернету"
                        else -> "Неверный email или пароль"
                    }
                    _loginState.value = LoginState.Error(errorMsg)
                }
            }
    }

    fun signInBiometric(context: Context) {
        val session = SessionManager(context)
        if (session.isLoggedIn && session.userId.isNotBlank()) {
            // Проверяем, не прошло ли 24 часа
            val elapsed = System.currentTimeMillis() - session.lastActiveTime
            if (elapsed > 24 * 60 * 60 * 1000) {
                session.logout()
                _loginState.value = LoginState.Error("Сессия истекла. Войдите заново.")
            } else {
                // Восстанавливаем вход
                val user = auth.currentUser
                if (user != null && user.uid == session.userId) {
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error("Пользователь не найден")
                }
            }
        } else {
            _loginState.value = LoginState.Error("Нет сохранённой сессии")
        }
    }

    fun resetState() { _loginState.value = LoginState.Idle }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
package com.rostelcom.servicedesk

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.rostelcom.servicedesk.data.model.ServiceRequest
import com.rostelcom.servicedesk.ui.detail.DetailScreen
import com.rostelcom.servicedesk.ui.login.LoginScreen
import com.rostelcom.servicedesk.ui.main.MainScreen
import com.rostelcom.servicedesk.ui.stats.StatsScreen
import com.rostelcom.servicedesk.ui.theme.ServiceDeskTheme
import com.rostelcom.servicedesk.util.SessionManager
import java.net.URLEncoder

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        val auth = FirebaseAuth.getInstance()
        val sessionManager = SessionManager(this)

        val wasLoggedIn = auth.currentUser != null && sessionManager.isLoggedIn

        if (wasLoggedIn) {
            val elapsed = System.currentTimeMillis() - sessionManager.lastActiveTime
            if (elapsed > 24 * 60 * 60 * 1000) {
                sessionManager.isLoggedIn = false
                auth.signOut()
            } else {
                sessionManager.userId = auth.currentUser?.uid ?: ""
                sessionManager.userEmail = auth.currentUser?.email ?: ""
                sessionManager.lastActiveTime = System.currentTimeMillis()
            }
        } else {
            sessionManager.logout()
            auth.signOut()
        }

        setContent {
            val startLoggedIn = auth.currentUser != null && sessionManager.isLoggedIn && !sessionManager.biometricEnabled
            var isLoggedIn by remember { mutableStateOf(startLoggedIn) }
            var isDarkTheme by remember { mutableStateOf(sessionManager.isDarkTheme) }
            val navController = rememberNavController()

            ServiceDeskTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLoggedIn) {
                        val uid = auth.currentUser?.uid ?: sessionManager.userId
                        NavHost(navController = navController, startDestination = "main") {
                            composable("main") {
                                val context = LocalContext.current
                                LaunchedEffect(Unit) {
                                    SessionManager(context).lastActiveTime = System.currentTimeMillis()
                                }
                                MainScreen(
                                    userId = uid,
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = {
                                        isDarkTheme = !isDarkTheme
                                        sessionManager.isDarkTheme = isDarkTheme
                                    },
                                    onRequestClick = { request ->
                                        val json = Gson().toJson(request)
                                        navController.navigate("detail/${URLEncoder.encode(json, "UTF-8")}")
                                    },
                                    onLogout = {
                                        sessionManager.logout()
                                        auth.signOut()
                                        isLoggedIn = false
                                    },
                                    onStatsClick = { navController.navigate("stats") }
                                )
                            }
                            composable("detail/{requestJson}") { backStackEntry ->
                                val json = backStackEntry.arguments?.getString("requestJson") ?: ""
                                val request = Gson().fromJson(json, ServiceRequest::class.java)
                                DetailScreen(
                                    request = request,
                                    onBack = { navController.popBackStack("main", inclusive = false) }
                                )
                            }
                            composable("stats") {
                                StatsScreen(
                                    userId = uid,
                                    onBack = { navController.popBackStack("main", inclusive = false) }
                                )
                            }
                        }
                    } else {
                        LoginScreen(
                            onLoginSuccess = {
                                val user = auth.currentUser
                                if (user != null) {
                                    sessionManager.isLoggedIn = true
                                    sessionManager.userId = user.uid
                                    sessionManager.userEmail = user.email ?: ""
                                    sessionManager.lastActiveTime = System.currentTimeMillis()
                                    isLoggedIn = true

                                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val token = task.result
                                            FirebaseFirestore.getInstance()
                                                .collection("users").document(user.uid)
                                                .set(mapOf("fcmToken" to token), SetOptions.merge())
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
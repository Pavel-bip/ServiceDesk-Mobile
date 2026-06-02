package com.rostelcom.servicedesk.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rostelcom.servicedesk.MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Сохраним токен в Firestore для текущего пользователя
        saveTokenToFirestore(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Показываем уведомление
        showNotification(message.notification?.title ?: "Новая заявка",
            message.notification?.body ?: "")
    }

    private fun saveTokenToFirestore(token: String) {
        val session = SessionManager(this)
        if (session.userId.isNotEmpty()) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(session.userId)
                .update("fcmToken", token)
                .addOnFailureListener {
                    // Если документ не существует, создаём
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(session.userId)
                        .set(mapOf("fcmToken" to token))
                }
        }
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "service_requests"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Заявки",
                NotificationManager.IMPORTANCE_HIGH
            )
            NotificationManagerCompat.from(this).createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notification)
    }
}
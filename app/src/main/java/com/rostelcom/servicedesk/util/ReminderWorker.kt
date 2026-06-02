package com.rostelcom.servicedesk.util

import android.content.Context
import android.util.Log
import androidx.work.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val session = SessionManager(applicationContext)
        val uid = session.userId
        if (uid.isBlank()) return Result.success()

        val requests = try {
            FirebaseFirestore.getInstance()
                .collection("requests")
                .whereEqualTo("AssignedEngineerId", uid)
                .get()
                .await()
                .documents
                .mapNotNull { it.getString("Status") }
        } catch (e: Exception) {
            return Result.retry()
        }

        val activeCount = requests.count { it == "Назначена" || it == "В работе" }

        if (activeCount > 0) {
            NotificationHelper.showReminder(applicationContext, activeCount)
        }

        // Планируем следующее уведомление на завтра в 8:00
        schedule(applicationContext)

        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork("reminder_work")

            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now.timeInMillis) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            val delay = target.timeInMillis - now.timeInMillis

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "reminder_work",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }
    }
}
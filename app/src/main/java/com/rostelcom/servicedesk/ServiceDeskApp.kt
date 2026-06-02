package com.rostelcom.servicedesk

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.rostelcom.servicedesk.util.NotificationHelper
import com.rostelcom.servicedesk.util.ReminderWorker

class ServiceDeskApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseFirestore.getInstance().apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
        }
        NotificationHelper.createChannel(this)
        ReminderWorker.schedule(this)
    }
}
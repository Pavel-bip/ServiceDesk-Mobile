package com.rostelcom.servicedesk.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.rostelcom.servicedesk.data.model.ServiceRequest
import kotlinx.coroutines.tasks.await

class RequestRepository(private val firestore: FirebaseFirestore) {

    suspend fun getAssignedRequests(engineerId: String): List<ServiceRequest> {
        return try {
            val snapshot = firestore.collection("requests")
                .whereEqualTo("AssignedEngineerId", engineerId)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ServiceRequest::class.java)?.copy(documentId = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateStatus(requestId: String, newStatus: String) {
        val updates = mutableMapOf(  // <-- убрал <String, Any>
            "Status" to newStatus,
            "lastModified" to FieldValue.serverTimestamp()
        )
        when (newStatus) {
            "В работе" -> updates["workStartedAt"] = FieldValue.serverTimestamp()
            "Выполнена" -> updates["workCompletedAt"] = FieldValue.serverTimestamp()
        }
        firestore.collection("requests").document(requestId).update(updates)
    }

    suspend fun addComment(requestId: String, comment: String) {
        firestore.collection("requests").document(requestId)
            .update("ExternalComment", comment, "lastModified", FieldValue.serverTimestamp())
    }
}
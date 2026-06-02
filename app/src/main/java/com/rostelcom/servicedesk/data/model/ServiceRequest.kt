package com.rostelcom.servicedesk.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.net.URLDecoder

data class ServiceRequest(
    val documentId: String = "",
    @PropertyName("Id") val id: String = "",
    @PropertyName("ClientInfo") val clientInfoRaw: String = "",
    @PropertyName("Address") val addressRaw: String = "",
    @PropertyName("Phone") val phoneRaw: String = "",
    @PropertyName("EquipmentType") val equipmentTypeRaw: String = "",
    @PropertyName("IssueDescription") val issueDescriptionRaw: String = "",
    @PropertyName("Status") val statusRaw: String = "Новая",
    @PropertyName("Priority") val priorityRaw: String = "Плановый",
    @PropertyName("createdAt") val createdAt: Timestamp? = null,
    @PropertyName("SerialNumber") val serialNumber: String = "",
    @PropertyName("AssignedEngineerId") val assignedEngineerId: String = "",
    @PropertyName("ExternalComment") val externalCommentRaw: String = "",
    @PropertyName("lastModified") val lastModified: Timestamp? = null,
    @PropertyName("workStartedAt") val workStartedAt: Timestamp? = null,
    @PropertyName("workCompletedAt") val workCompletedAt: Timestamp? = null
) {
    constructor() : this("", "", "", "", "", "", "", "Новая", "Плановый", null, "", "", "", null, null, null)

    val clientInfo: String get() = decode(clientInfoRaw)
    val address: String get() = decode(addressRaw)
    val phone: String get() {
        val decoded = decode(phoneRaw).replace(" ", "")
        return if (decoded.isNotBlank() && !decoded.startsWith("+")) "+$decoded" else decoded
    }
    val issueDescription: String get() = decode(issueDescriptionRaw)
    val externalComment: String get() = decode(externalCommentRaw)
    val status: String get() = decode(statusRaw)
    val equipmentType: String get() = decode(equipmentTypeRaw)
    val priority: String get() = decode(priorityRaw)

    private fun decode(s: String): String {
        return try {
            URLDecoder.decode(s, "UTF-8")
        } catch (e: Exception) {
            s
        }
    }
}
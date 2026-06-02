package com.rostelcom.servicedesk.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.rostelcom.servicedesk.data.model.ServiceRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.concurrent.TimeUnit

class StatsViewModel(application: Application, private val userId: String) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()

    private val _stats = MutableStateFlow(StatsData())
    val stats: StateFlow<StatsData> = _stats

    data class StatsData(
        val totalCompleted: Int = 0,
        val successRate: Float = 0f,
        val avgTimeMinutes: Float = 0f
    )

    fun loadStats(period: String) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            when (period) {
                "day" -> {}
                "week" -> calendar.add(Calendar.DAY_OF_YEAR, -7)
                "month" -> calendar.add(Calendar.MONTH, -1)
            }
            val fromDate = Timestamp(calendar.time)

            val snapshot = try {
                firestore.collection("requests")
                    .whereEqualTo("AssignedEngineerId", userId)
                    .whereGreaterThanOrEqualTo("createdAt", fromDate)
                    .get()
                    .await()
            } catch (e: Exception) { null }

            val requests = snapshot?.documents?.mapNotNull {
                it.toObject(ServiceRequest::class.java)
            } ?: emptyList()

            val completed = requests.count { it.status == "Выполнена" }
            val total = requests.size
            val rate = if (total > 0) completed.toFloat() / total * 100f else 0f

            // Среднее время выполнения (в минутах)
            val completedRequests = requests.filter { it.status == "Выполнена" && it.workStartedAt != null && it.workCompletedAt != null }
            val avgTime = if (completedRequests.isNotEmpty()) {
                val totalMinutes = completedRequests.sumOf {
                    val diff = it.workCompletedAt!!.toDate().time - it.workStartedAt!!.toDate().time
                    TimeUnit.MILLISECONDS.toMinutes(diff).toInt()
                }
                totalMinutes.toFloat() / completedRequests.size
            } else 0f

            _stats.value = StatsData(
                totalCompleted = completed,
                successRate = rate,
                avgTimeMinutes = avgTime
            )
        }
    }
}
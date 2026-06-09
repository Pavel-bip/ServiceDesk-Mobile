package com.rostelcom.servicedesk.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.rostelcom.servicedesk.data.model.ServiceRequest
import com.rostelcom.servicedesk.data.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val userId: String,
    application: Application
) : AndroidViewModel(application) {

    private val repository = RequestRepository(FirebaseFirestore.getInstance())

    private val _requests = MutableStateFlow<List<ServiceRequest>>(emptyList())
    val requests: StateFlow<List<ServiceRequest>> = _requests

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _statusFilter = MutableStateFlow("Все")
    val statusFilter: StateFlow<String> = _statusFilter

    private val _typeFilter = MutableStateFlow("Все")
    val typeFilter: StateFlow<String> = _typeFilter

    private val _priorityFilter = MutableStateFlow("Все")
    val priorityFilter: StateFlow<String> = _priorityFilter

    fun loadRequests() {
        android.util.Log.d("MAIN_VM", "loadRequests called, userId = '$userId'")
        if (userId.isBlank()) {
            android.util.Log.d("MAIN_VM", "userId is BLANK, returning")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            android.util.Log.d("MAIN_VM", "Fetching requests from Firestore for userId = $userId")
            val list = repository.getAssignedRequests(userId)
            android.util.Log.d("MAIN_VM", "Got ${list.size} requests")
            _requests.value = list
            _isLoading.value = false
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setStatusFilter(status: String) { _statusFilter.value = status }
    fun setTypeFilter(type: String) { _typeFilter.value = type }
    fun setPriorityFilter(priority: String) { _priorityFilter.value = priority }
    fun getFilteredRequests(): List<ServiceRequest> {
        var filtered = _requests.value
        if (_searchQuery.value.isNotBlank()) {
            val q = _searchQuery.value.lowercase()
            filtered = filtered.filter {
                it.address.lowercase().contains(q) ||
                        it.clientInfo.lowercase().contains(q) ||
                        it.serialNumber.lowercase().contains(q) ||
                        it.id.lowercase().contains(q) ||
                        it.issueDescription.lowercase().contains(q)
            }
        }
        if (_statusFilter.value != "Все") filtered = filtered.filter { it.status == _statusFilter.value }
        if (_typeFilter.value != "Все") filtered = filtered.filter { it.equipmentType == _typeFilter.value }
        if (_priorityFilter.value != "Все") filtered = filtered.filter { it.priority == _priorityFilter.value }
        return filtered.sortedWith(
            compareByDescending<ServiceRequest> { it.priority == "Аварийный" }
                .thenByDescending { it.priority == "Срочный" }
                .thenByDescending { it.createdAt?.seconds ?: 0 }
        )
    }
}
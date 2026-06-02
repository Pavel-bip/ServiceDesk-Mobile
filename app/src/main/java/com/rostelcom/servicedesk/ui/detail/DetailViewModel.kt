package com.rostelcom.servicedesk.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.rostelcom.servicedesk.data.repository.RequestRepository
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {
    private val repository = RequestRepository(FirebaseFirestore.getInstance())

    fun updateStatus(requestId: String, newStatus: String) {
        viewModelScope.launch { repository.updateStatus(requestId, newStatus) }
    }

    fun addComment(requestId: String, comment: String) {
        viewModelScope.launch { repository.addComment(requestId, comment) }
    }
}
package com.rostelcom.servicedesk.ui.stats

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StatsViewModelFactory(
    private val userId: String,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return StatsViewModel(application, userId) as T
    }
}
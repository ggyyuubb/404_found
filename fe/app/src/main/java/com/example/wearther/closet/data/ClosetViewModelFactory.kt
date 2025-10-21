package com.example.wearther.closet.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ClosetViewModelFactory(
    private val closetApi: ClosetApi,
    private val tokenHeader: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClosetViewModel::class.java)) {
            return ClosetViewModel(closetApi, tokenHeader) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
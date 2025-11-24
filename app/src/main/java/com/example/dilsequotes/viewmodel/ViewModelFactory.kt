package com.example.dilsequotes.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dilsequotes.data.repository.QuoteRepository

class ViewModelFactory(
    private val repository: QuoteRepository,
    private val context: Context? // Add context to the factory for network check
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuoteViewModel(repository, context) as T // Pass context to QuoteViewModel
        }
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, context) as T // Pass context to HomeViewModel
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

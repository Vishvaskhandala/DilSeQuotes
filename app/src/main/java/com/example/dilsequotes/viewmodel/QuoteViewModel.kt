package com.example.dilsequotes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dilsequotes.data.model.Quote
import com.example.dilsequotes.data.repository.QuoteRepository
import kotlinx.coroutines.launch


// ============================================
class QuoteViewModel(private val repository: QuoteRepository) : ViewModel() {

    /**
     * Get quotes filtered by category
     */
    fun getQuotes(category: String): LiveData<List<Quote>> {
        return repository.getQuotesByCategory(category)
    }

    /**
     * Get all favorite quotes
     */
    fun getFavorites(): LiveData<List<Quote>> {
        return repository.getFavorites()
    }

    /**
     * Toggle favorite status of a quote
     */
    fun toggleFavorite(quote: Quote) {
        viewModelScope.launch {
            repository.toggleFavorite(quote)
        }
    }

    /**
     * Get quote of the day
     */
    fun getQuoteOfDay(onResult: (Quote?) -> Unit) {
        viewModelScope.launch {
            val quote = repository.getQuoteOfDay()
            onResult(quote)
        }
    }

    /**
     * Remove a quote from database
     */
    fun removeQuote(quote: Quote) {
        viewModelScope.launch {
            repository.removeQuote(quote)
        }
    }
}

// ============================================
// QuoteViewModelFactory
// ============================================
class QuoteViewModelFactory(
    private val repository: QuoteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuoteViewModel::class.java)) {
            return QuoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
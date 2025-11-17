package com.example.dilsequotes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dilsequotes.data.model.Quote
import com.example.dilsequotes.data.repository.GetRandomQuoteUseCase
import kotlinx.coroutines.launch

// No Hilt annotations - this is now a plain ViewModel
class HomeViewModel(private val getRandomQuoteUseCase: GetRandomQuoteUseCase) : ViewModel() {

    private val _quoteOfDay = MutableLiveData<Quote?>()
    val quoteOfDay: LiveData<Quote?> = _quoteOfDay

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadDailyQuote() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val quote = getRandomQuoteUseCase()
                _quoteOfDay.value = quote
            } catch (e: Exception) {
                // Handle error case, e.g., show a toast or a default quote
                _quoteOfDay.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// The factory is still needed for manual dependency injection
class HomeViewModelFactory(private val getRandomQuoteUseCase: GetRandomQuoteUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(getRandomQuoteUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
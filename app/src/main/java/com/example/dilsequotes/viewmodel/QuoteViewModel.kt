package com.example.dilsequotes.viewmodel

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dilsequotes.Logger
import com.example.dilsequotes.data.model.Quote
import com.example.dilsequotes.data.repository.QuoteRepository
import kotlinx.coroutines.launch

class QuoteViewModel(
    private val repository: QuoteRepository,
    private val context: Context?
) : ViewModel() {

    private val _quotes = MutableLiveData<List<Quote>>()
    val quotes: LiveData<List<Quote>> = _quotes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Store current category and language for reloading
    private var currentCategory: String? = null
    private var currentLanguage: String? = null

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context?.let {
            it.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager?.activeNetwork
            val networkCapabilities = connectivityManager!!.getNetworkCapabilities(activeNetwork)
            return networkCapabilities != null &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager?.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    // Load quotes by category and language
    fun loadQuotesByCategory(categoryKey: String, language: String) {
        viewModelScope.launch @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) {
            _isLoading.value = true

            // Store for reloading after favorite toggle
            currentCategory = categoryKey
            currentLanguage = language

            val isNetworkAvailable = isNetworkAvailable()
            try {
                val result = repository.getQuotesByCategoryAndLanguage(
                    categoryKey,
                    language,
                    isNetworkAvailable
                )
                _quotes.value = result
                Logger.d("QuoteViewModel: Loaded ${result.size} quotes for category: $categoryKey")
            } catch (e: Exception) {
                Logger.e("Failed to load quotes for category: $categoryKey", e)
                _quotes.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Get favorites
    fun getFavorites(): LiveData<List<Quote>> {
        return repository.getFavorites()
    }

    // CRITICAL FIX: Toggle favorite and reload quotes to get updated data
    fun toggleFavorite(quote: Quote) {
        viewModelScope.launch {
            try {
                // Toggle in database
                repository.toggleFavorite(quote)
                Logger.d("QuoteViewModel: Toggled favorite for quote ID: ${quote.id}, new status: ${!quote.isFavorite}")

                // CRITICAL: Reload current category to get updated favorite status
                if (currentCategory != null && currentLanguage != null) {
                    val isNetworkAvailable = isNetworkAvailable()
                    val updatedQuotes = repository.getQuotesByCategoryAndLanguage(
                        currentCategory!!,
                        currentLanguage!!,
                        isNetworkAvailable
                    )
                    _quotes.postValue(updatedQuotes)
                    Logger.d("QuoteViewModel: Reloaded quotes after favorite toggle")
                }
            } catch (e: Exception) {
                Logger.e("Failed to toggle favorite for quote ID: ${quote.id}", e)
            }
        }
    }

    fun fetchQuotesFromApi() {
        viewModelScope.launch {
            try {
                val quotes = repository.fetchQuotesFromApi()
                repository.insertQuotes(quotes)
                _quotes.value = quotes
            } catch (e: Exception) {
                Logger.e("Failed to fetch quotes from API", e)
            }
        }
    }

    fun getRandomQuoteFromDb(language: String) {
        viewModelScope.launch {
            try {
                val quote = repository.getRandomQuoteFromDb(language)
                if (quote != null) {
                    _quotes.value = listOf(quote)
                } else {
                    _quotes.value = emptyList()
                }
            } catch (e: Exception) {
                Logger.e("Failed to get random quote from DB", e)
            }
        }
    }
}
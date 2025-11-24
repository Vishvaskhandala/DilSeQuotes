package com.example.dilsequotes.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dilsequotes.data.api.RetrofitInstance
import com.example.dilsequotes.data.model.CategoryConstants
import com.example.dilsequotes.data.model.CategoryData
import com.example.dilsequotes.data.model.Quote
import com.example.dilsequotes.data.repository.QuoteRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val quoteRepository: QuoteRepository, // Inject the repository
    private val context: Context? // Inject context for network check
) : ViewModel() {

    private val _quoteOfDay = MutableLiveData<Quote?>()
    val quoteOfDay: LiveData<Quote?> = _quoteOfDay

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _categories = MutableLiveData<List<CategoryData>>()
    val categories: LiveData<List<CategoryData>> = _categories

    // Check network connectivity
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = (context?.getSystemService(Context.CONNECTIVITY_SERVICE) ?: null ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    // Load daily quote (with network check)
    fun loadDailyQuote(language: String) {
        viewModelScope.launch {
            Log.d("HomeViewModel", "Loading daily quote for language: $language")
            _isLoading.value = true
            val isNetworkAvailable = isNetworkAvailable() // Check network status

            try {
                val response = if (isNetworkAvailable) {
                    RetrofitInstance.api.getQuoteOfTheDay() // Fetch from API if network is available
                } else {
                    quoteRepository.getRandomQuoteFromDb(language) // Fallback to DB if no network
                }
                _quoteOfDay.postValue(response?.copy(language = language))
                Log.d("HomeViewModel", "Fetched quoteOfDay: ${response?.text}")
            } catch (e: Exception) {
                _quoteOfDay.postValue(null)
                Log.e("HomeViewModel", "Error fetching quote", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Load categories (with network check)
    fun loadCategories() {
        viewModelScope.launch {
            Log.d("HomeViewModel", "Loading categories")
            _isLoading.value = true
            val isNetworkAvailable = isNetworkAvailable() // Check network status

            try {
                if (isNetworkAvailable) {
                    val response = RetrofitInstance.api.getQuotes() // Fetch from API if network is available
                    Log.d("HomeViewModel", "Fetched ${response.size} quotes")
                } else {
                    Log.d("HomeViewModel", "No network available, loading from local database.")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching quotes", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
        _categories.value = CategoryConstants.ALL_CATEGORIES // Static list as fallback
    }

    // Load quotes by category and language (with network check)
    /*fun loadQuotesByCategoryAndLanguage(categoryKey: String, language: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val isNetworkAvailable = isNetworkAvailable() // Check network status
            try {
                val quotes = quoteRepository.getQuotesByCategoryAndLanguage(
                    categoryKey,
                    language,
                    isNetworkAvailable
                )
                Log.d("HomeViewModel", "Fetched ${quotes.size} quotes for category: $categoryKey")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching quotes for category: $categoryKey", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }*/
}

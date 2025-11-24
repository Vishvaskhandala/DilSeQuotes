package com.example.dilsequotes.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.LiveData
import com.example.dilsequotes.Logger
import com.example.dilsequotes.data.api.RetrofitInstance
import com.example.dilsequotes.data.dao.QuoteDao
import com.example.dilsequotes.data.model.Quote

class QuoteRepository(
    private val quoteDao: QuoteDao,
    private val context: Context
) {

    suspend fun getQuotesByCategoryAndLanguage(
        categoryKey: String,
        language: String,
        isNetworkAvailable: Boolean
    ): List<Quote> {
        if (isNetworkAvailable) {
            try {
                val normalizedKey = categoryKey.lowercase().replace(" ", "").replace("_", "")
                Logger.d("Requesting quotes for normalized category: $normalizedKey, language: $language")

                // Fetch quotes from API
                val allQuotes = RetrofitInstance.api.getQuotes()
                Logger.d("Fetched ${allQuotes.size} quotes from API")

                // Filter quotes by category and language
                val filteredQuotes = allQuotes.filter { quote ->
                    quote.category.lowercase().replace(" ", "").replace("_", "") == normalizedKey &&
                            quote.language == language
                }

                Logger.d("Filtered to ${filteredQuotes.size} quotes matching category: $categoryKey, language: $language")

                // 🔥 CRITICAL FIX: Preserve favorite status when updating from API
                if (filteredQuotes.isNotEmpty()) {
                    // Get existing quotes from DB to preserve favorite status
                    val existingQuotes = quoteDao.getQuotesByCategoryAndLanguage(categoryKey, language)
                    val existingFavorites = existingQuotes.associateBy({ it.id }, { it.isFavorite })

                    // Merge API data with existing favorite status
                    val quotesToInsert = filteredQuotes.map { apiQuote ->
                        apiQuote.copy(isFavorite = existingFavorites[apiQuote.id] ?: false)
                    }

                    quoteDao.insertAll(quotesToInsert)
                    return quotesToInsert
                }

                return filteredQuotes
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    Logger.w("Category file not found on server: $categoryKey (404)")
                } else {
                    Logger.e("API HTTP error for category: $categoryKey (${e.code()})", e)
                }
            } catch (e: Exception) {
                Logger.e("API fetch failed for category: $categoryKey. ${e.message}", e)
            }
        } else {
            Logger.w("No network available, falling back to local database.")
        }

        // Fallback: Return filtered data from local Room DB
        val localQuotes = quoteDao.getQuotesByCategoryAndLanguage(categoryKey, language)
        if (localQuotes.isEmpty()) {
            Logger.w("No quotes found in local database for category: $categoryKey, language: $language")
        }
        return localQuotes
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    fun getFavorites(): LiveData<List<Quote>> {
        return quoteDao.getFavorites()
    }

    suspend fun toggleFavorite(quote: Quote) {
        // 🔥 CRITICAL FIX: Create updated quote and save to database
        val updatedQuote = quote.copy(isFavorite = !quote.isFavorite)
        quoteDao.updateQuote(updatedQuote)
        Logger.d("Toggled favorite for quote ID: ${quote.id}, new status: ${updatedQuote.isFavorite}")
    }

    suspend fun fetchQuotesFromApi(): List<Quote> {
        return RetrofitInstance.api.getQuotes()
    }

    suspend fun insertQuotes(quotes: List<Quote>) {
        // Preserve existing favorites when inserting bulk quotes
        val existingQuotes = quoteDao.getAllQuotesByLanguage(quotes.firstOrNull()?.language ?: "en")
        val existingFavorites = existingQuotes.associateBy({ it.id }, { it.isFavorite })

        val quotesToInsert = quotes.map { apiQuote ->
            apiQuote.copy(isFavorite = existingFavorites[apiQuote.id] ?: false)
        }

        quoteDao.insertAll(quotesToInsert)
    }

    suspend fun getRandomQuoteFromDb(language: String): Quote? {
        return quoteDao.getRandomQuoteByLanguage(language)
    }

    suspend fun getRandomQuoteFromApi(): Quote {
        return RetrofitInstance.api.getRandomQuote()
    }
}
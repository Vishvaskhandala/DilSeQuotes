package com.example.dilsequotes.data.repository

import androidx.lifecycle.LiveData
import com.example.dilsequotes.data.dao.QuoteDao
import com.example.dilsequotes.data.model.Quote
import java.util.Calendar

class QuoteRepository(private val quoteDao: QuoteDao) {

    // Initialize database with sample quotes if empty
    suspend fun initializeDatabaseWithSampleQuotes() {
        val existingQuotes = quoteDao.getAllQuotes()

        // Only insert if database is empty
        if (existingQuotes.isEmpty()) {
            val sampleQuotes = getSampleQuotes()
            quoteDao.insertAll(sampleQuotes)
        }
    }

    // Get all quotes from database
    suspend fun getAllQuotes(): List<Quote> {
        return quoteDao.getAllQuotes()
    }

    // Get quotes filtered by category (LiveData for real-time updates)
    fun getQuotesByCategory(category: String): LiveData<List<Quote>> {
        return quoteDao.getQuotesByCategory(category)
    }

    // Get all favorite quotes (LiveData for real-time updates)
    fun getFavorites(): LiveData<List<Quote>> {
        return quoteDao.getFavorites()
    }

    // Get count of favorite quotes
    suspend fun getFavoritesCount(): Int {
        return quoteDao.getFavoritesCount()
    }

    // Get a consistent quote for the day based on current date
    suspend fun getQuoteOfDay(): Quote? {
        val allQuotes = quoteDao.getAllQuotes()

        if (allQuotes.isEmpty()) {
            return null
        }

        // Generate a seed based on current date to get the same quote all day
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        val seed = (dayOfYear + year).toLong()

        // Use modulo to select a quote based on the seed
        val index = (seed % allQuotes.size).toInt()
        return allQuotes[index]
    }

    // Toggle the favorite status of a quote
    suspend fun toggleFavorite(quote: Quote) {
        val updatedQuote = quote.copy(isFavorite = !quote.isFavorite)
        quoteDao.updateQuote(updatedQuote)
    }

    // Remove a quote from the database
    suspend fun removeQuote(quote: Quote) {
        quoteDao.delete(quote)
    }

    // Sample quotes to populate the database with language support
    private fun getSampleQuotes(): List<Quote> {
        return listOf(
            // Motivation Category - English
            Quote(
                id = 1,
                text = "The only way to do great work is to love what you do.",
                author = "Steve Jobs",
                category = "Motivation",
                language = "en",
                isFavorite = false
            ),
            Quote(
                id = 2,
                text = "Believe you can and you're halfway there.",
                author = "Theodore Roosevelt",
                category = "Motivation",
                language = "en",
                isFavorite = false
            ),
            Quote(
                id = 3,
                text = "Success is not final, failure is not fatal: it is the courage to continue that counts.",
                author = "Winston Churchill",
                category = "Motivation",
                language = "en",
                isFavorite = false
            ),

            // Wisdom Category - English
            Quote(
                id = 4,
                text = "The only true wisdom is in knowing you know nothing.",
                author = "Socrates",
                category = "Wisdom",
                language = "en",
                isFavorite = false
            ),
            Quote(
                id = 5,
                text = "In the middle of difficulty lies opportunity.",
                author = "Albert Einstein",
                category = "Wisdom",
                language = "en",
                isFavorite = false
            ),
            Quote(
                id = 6,
                text = "The greatest glory in living lies not in never falling, but in rising every time we fall.",
                author = "Nelson Mandela",
                category = "Wisdom",
                language = "en",
                isFavorite = false
            ),

            // Life Category - English
            Quote(
                id = 7,
                text = "Life is what happens when you're busy making other plans.",
                author = "John Lennon",
                category = "Life",
                language = "en",
                isFavorite = false
            ),
            Quote(
                id = 8,
                text = "The purpose of our lives is to be happy.",
                author = "Dalai Lama",
                category = "Life",
                language = "en",
                isFavorite = false
            ),
            Quote(
                id = 9,
                text = "Life is really simple, but we insist on making it complicated.",
                author = "Confucius",
                category = "Life",
                language = "en",
                isFavorite = false
            ),

            // Success Category - English
            Quote(
                id = 10,
                text = "Success is not the key to happiness. Happiness is the key to success.",
                author = "Albert Schweitzer",
                category = "Success",
                language = "en",
                isFavorite = false
            ),
            Quote(
                id = 11,
                text = "Don't watch the clock; do what it does. Keep going.",
                author = "Sam Levenson",
                category = "Success",
                language = "en",
                isFavorite = false
            ),
            Quote(
                id = 12,
                text = "The way to get started is to quit talking and begin doing.",
                author = "Walt Disney",
                category = "Success",
                language = "en",
                isFavorite = false
            ),

            // Inspiration Category - English
            Quote(
                id = 13,
                text = "Everything you've ever wanted is on the other side of fear.",
                author = "George Addair",
                category = "Inspiration",
                language = "en",
                isFavorite = false
            ),
            Quote(
                id = 14,
                text = "Dream big and dare to fail.",
                author = "Norman Vaughan",
                category = "Inspiration",
                language = "en",
                isFavorite = false
            ),
            Quote(
                id = 15,
                text = "The best time to plant a tree was 20 years ago. The second best time is now.",
                author = "Chinese Proverb",
                category = "Inspiration",
                language = "en",
                isFavorite = false
            ),

            // Additional quotes in Hindi
            Quote(
                id = 16,
                text = "कर्म करो, फल की चिंता मत करो।",
                author = "भगवद गीता",
                category = "Wisdom",
                language = "hi",
                isFavorite = false
            ),
            Quote(
                id = 17,
                text = "जहाँ चाह वहाँ राह।",
                author = "हिंदी कहावत",
                category = "Motivation",
                language = "hi",
                isFavorite = false
            ),
            Quote(
                id = 18,
                text = "समय सबसे बड़ा शिक्षक है।",
                author = "कबीर दास",
                category = "Life",
                language = "hi",
                isFavorite = false
            ),

            // Additional quotes in Spanish
            Quote(
                id = 19,
                text = "No hay mal que por bien no venga.",
                author = "Proverbio Español",
                category = "Wisdom",
                language = "es",
                isFavorite = false
            ),
            Quote(
                id = 20,
                text = "El que no arriesga, no gana.",
                author = "Proverbio Español",
                category = "Success",
                language = "es",
                isFavorite = false
            )
        )
    }
}

class GetRandomQuoteUseCase(private val quoteRepository: QuoteRepository) {
    suspend operator fun invoke(): Quote? {
        return quoteRepository.getQuoteOfDay()
    }
}
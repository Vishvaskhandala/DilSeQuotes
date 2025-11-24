package com.example.dilsequotes.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dilsequotes.data.model.Quote

@Dao
interface QuoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quotes: List<Quote>)

    @Query("SELECT * FROM quote_table")
    fun getAllQuotes(): LiveData<List<Quote>>

    @Query("SELECT * FROM quote_table WHERE language = :language")
    suspend fun getAllQuotesByLanguage(language: String): List<Quote>

    @Query("SELECT COUNT(*) FROM quote_table")
    suspend fun getCount(): Int

    @Query("SELECT * FROM quote_table WHERE category = :categoryKey AND language = :language")
    suspend fun getQuotesByCategoryAndLanguage(categoryKey: String, language: String): List<Quote>

    @Query("SELECT * FROM quote_table WHERE isFavorite = 1")
    fun getFavorites(): LiveData<List<Quote>>

    @Update
    suspend fun updateQuote(quote: Quote)

    @Query("SELECT * FROM quote_table WHERE language = :language ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuoteByLanguage(language: String): Quote?

    // The old method is no longer used, replaced by the language-aware one above.
    @Query("SELECT * FROM quote_table ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuote(): Quote?
}

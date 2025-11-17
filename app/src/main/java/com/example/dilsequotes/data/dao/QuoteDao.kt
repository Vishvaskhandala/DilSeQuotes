package com.example.dilsequotes.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dilsequotes.data.model.Quote

@Dao
interface QuoteDao {

    @Query("SELECT * FROM quotes")
    suspend fun getAllQuotes(): List<Quote>

    @Query("SELECT * FROM quotes WHERE category = :category")
    fun getQuotesByCategory(category: String): LiveData<List<Quote>>

    @Query("SELECT * FROM quotes WHERE isFavorite = 1")
    fun getFavorites(): LiveData<List<Quote>>

    @Query("SELECT COUNT(*) FROM quotes WHERE isFavorite = 1")
    suspend fun getFavoritesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quotes: List<Quote>)

    @Update
    suspend fun updateQuote(quote: Quote)

    // This is the correct way to delete a single quote object
    @Delete
    suspend fun delete(quote: Quote)
}

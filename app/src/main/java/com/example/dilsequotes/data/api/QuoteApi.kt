package com.example.dilsequotes.data.api

import com.example.dilsequotes.data.model.Quote
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QuoteApi {

    @GET("quotes.json")
    suspend fun getQuotes(): List<Quote>

    @GET("today.json")
    suspend fun getQuoteOfTheDay(): Quote

    @GET("random.json")
    suspend fun getRandomQuote(): Quote

    @GET("categories/{category}.json")
    suspend fun getQuotesByCategory(@Path("category") category: String): List<Quote>

    // In your Retrofit API interface
    @GET("quotes")
    suspend fun getQuotes(
        @Query("category") category: String,
        @Query("language") language: String
    ): List<Quote>
}

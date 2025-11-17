package com.example.dilsequotes.data.api

import com.example.dilsequotes.data.model.Quote
import retrofit2.http.GET
import retrofit2.http.Path

interface QuoteApi {
    @GET("quotes")
    suspend fun getQuotes(): List<Quote>

    @GET("quotes/today")
    suspend fun getQuoteOfTheDay(): Quote
    @GET("quotes/random")
    suspend fun getRandomQuote(): Quote
    @GET("quotes/category/{category}")
    suspend fun getQuotesByCategory(@Path("category") category: String): List<Quote>
}
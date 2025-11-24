package com.example.dilsequotes.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    // Correct base URL - pointing to the raw folder
    private const val BASE_URL = "https://raw.githubusercontent.com/Vishvaskhandala/shayari-api/main/raw/"

    // Add logging to see what URLs are being called
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: QuoteApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuoteApi::class.java)
    }
}

/*
 * This will generate the following URLs:
 *
 * getQuotes() -> https://raw.githubusercontent.com/Vishvaskhandala/shayari-api/main/raw/quotes.json
 * getQuoteOfTheDay() -> https://raw.githubusercontent.com/Vishvaskhandala/shayari-api/main/raw/today.json
 * getRandomQuote() -> https://raw.githubusercontent.com/Vishvaskhandala/shayari-api/main/raw/random.json
 * getQuotesByCategory("friendship") -> https://raw.githubusercontent.com/Vishvaskhandala/shayari-api/main/raw/categories/friendship.json
 */
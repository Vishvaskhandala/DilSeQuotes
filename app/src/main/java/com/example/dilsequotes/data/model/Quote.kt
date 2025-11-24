package com.example.dilsequotes.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "quote_table")
data class Quote(
    @PrimaryKey(autoGenerate = false) // CRITICAL FIX: Use a unique ID from API
    val id: Int = 0,
    val text: String,
    val authorName: String,
    val category: String, // e.g., "love", "sad". Links to CategoryData key
    val language: String, // e.g., "en", "hi", "gu"
    val source: String? = null, // Optional source like a book or movie
    val likes: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val emoji: String, // Emoji for the category
    val isFavorite: Boolean = false // Keep favorite status
) : Parcelable
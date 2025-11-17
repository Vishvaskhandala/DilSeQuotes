package com.example.dilsequotes.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val category: String,
    val language: String,
    val author: String? = null,
    val dateAdded: String? = null,
    val isFavorite: Boolean = false
)
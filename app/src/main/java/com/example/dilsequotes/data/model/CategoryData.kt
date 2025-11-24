package com.example.dilsequotes.data.model

import com.example.dilsequotes.R

data class CategoryData(
    val categoryId: String,        // This should be lowercase to match API files
    val nameResId: Int,            // For localized display name
    val emoji: String,
    val quoteCount: Int = 0
)

// Example usage in CategoryConstants.kt:
object CategoryConstants {
    val ALL_CATEGORIES = listOf(
        CategoryData(
            categoryId = "friendship",  // lowercase to match friendship.json
            nameResId = R.string.category_friendship,
            emoji = "🤝",
            quoteCount = 25
        ),
        CategoryData(
            categoryId = "love",  // lowercase to match love.json
            nameResId = R.string.category_love,
            emoji = "❤️",
            quoteCount = 30
        ),
        CategoryData(
            categoryId = "sad",
            nameResId = R.string.category_sad,
            emoji = "😢",
            quoteCount = 20
        ),
        CategoryData(
            categoryId = "motivation",
            nameResId = R.string.category_motivation,
            emoji = "💪",
            quoteCount = 28
        ),
        CategoryData(
            categoryId = "attitude",
            nameResId = R.string.category_attitude,
            emoji = "😎",
            quoteCount = 22
        ),
        CategoryData(
            categoryId = "breakup",
            nameResId = R.string.category_breakup,
            emoji = "💔",
            quoteCount = 18
        ),
        CategoryData(
            categoryId = "romantic",
            nameResId = R.string.category_romantic,
            emoji = "🌹",
            quoteCount = 26
        ),
        CategoryData(
            categoryId = "life",
            nameResId = R.string.category_life,
            emoji = "🌟",
            quoteCount = 24
        ),
        CategoryData(
            categoryId = "alone",
            nameResId = R.string.category_alone,
            emoji = "🚶",
            quoteCount = 15
        ),
        CategoryData(
            categoryId = "daily",
            nameResId = R.string.category_daily,
            emoji = "📅",
            quoteCount = 31
        ),
        CategoryData(
            categoryId = "festival",
            nameResId = R.string.category_festival,
            emoji = "🎉",
            quoteCount = 12
        )
    )
}

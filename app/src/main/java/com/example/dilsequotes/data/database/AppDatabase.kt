package com.example.dilsequotes.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dilsequotes.data.dao.QuoteDao
import com.example.dilsequotes.data.model.CategoryConstants
import com.example.dilsequotes.data.model.Quote

@Database(entities = [Quote::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun quoteDao(): QuoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quotes_database"
                )
                    // If a migration is not found, it will destroy and re-create the database.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Made internal to be accessible from MainActivity for seeding
        internal fun getSampleQuotes(): List<Quote> {
            val love = CategoryConstants.ALL_CATEGORIES.first { it.categoryId == "love" }
            val sad = CategoryConstants.ALL_CATEGORIES.first { it.categoryId == "sad" }
            val motivation = CategoryConstants.ALL_CATEGORIES.first { it.categoryId == "motivation" }
            val friendship = CategoryConstants.ALL_CATEGORIES.first { it.categoryId == "friendship" }
            val festival = CategoryConstants.ALL_CATEGORIES.first { it.categoryId == "festival" }
            val daily = CategoryConstants.ALL_CATEGORIES.first { it.categoryId == "daily" }

            // All quotes now use the categoryId as the category key.
            return listOf(
                // Love Quotes
                Quote(text = "Love all, trust a few, do wrong to none.", authorName = "William Shakespeare", category = love.categoryId, language = "en", emoji = love.emoji),
                Quote(text = "प्यार अंधा होता है।", authorName = "Unknown", category = love.categoryId, language = "hi", emoji = love.emoji),
                Quote(text = "પ્રેમ એજ જીવન છે.", authorName = "Unknown", category = love.categoryId, language = "gu", emoji = love.emoji),

                // Sad Quotes
                Quote(text = "Tears come from the heart and not from the brain.", authorName = "Leonardo da Vinci", category = sad.categoryId, language = "en", emoji = sad.emoji),
                Quote(text = "आंसू दिल से आते हैं, दिमाग से नहीं।", authorName = "Unknown", category = sad.categoryId, language = "hi", emoji = sad.emoji),
                Quote(text = "દુઃખ વગર સુખની કોઈ કિંમત નથી.", authorName = "Unknown", category = sad.categoryId, language = "gu", emoji = sad.emoji),

                // Motivation Quotes
                Quote(text = "The only way to do great work is to love what you do.", authorName = "Steve Jobs", category = motivation.categoryId, language = "en", emoji = motivation.emoji),
                Quote(text = "महान काम करने का एकमात्र तरीका यह है कि आप जो करते हैं उससे प्यार करें।", authorName = "Unknown", category = motivation.categoryId, language = "hi", emoji = motivation.emoji),
                Quote(text = "તમારી જાત પર વિશ્વાસ રાખો.", authorName = "Unknown", category = motivation.categoryId, language = "gu", emoji = motivation.emoji),

                // Friendship Quotes
                Quote(text = "A friend is someone who knows all about you and still loves you.", authorName = "Elbert Hubbard", category = friendship.categoryId, language = "en", emoji = friendship.emoji),
                Quote(text = "दोस्ती में धन्यवाद और सॉरी नहीं होता।", authorName = "Unknown", category = friendship.categoryId, language = "hi", emoji = friendship.emoji),
                Quote(text = "મિત્રતા એ જીવનનો સૌથી મોટો આશીર્વાદ છે.", authorName = "Unknown", category = friendship.categoryId, language = "gu", emoji = friendship.emoji),

                // Festival Quotes
                Quote(text = "Every festival is a reason to celebrate life.", authorName = "Unknown", category = festival.categoryId, language = "en", emoji = festival.emoji),
                Quote(text = "त्योहार जीवन का उत्सव हैं।", authorName = "Unknown", category = festival.categoryId, language = "hi", emoji = festival.emoji),
                Quote(text = "દરેક તહેવાર જીવનની ઉજવણી કરવાનો એક કારણ છે.", authorName = "Unknown", category = festival.categoryId, language = "gu", emoji = festival.emoji),

                // Daily Quotes
                Quote(text = "The sun is new each day.", authorName = "Heraclitus", category = daily.categoryId, language = "en", emoji = daily.emoji),
                Quote(text = "हर दिन एक नया सवेरा है।", authorName = "Unknown", category = daily.categoryId, language = "hi", emoji = daily.emoji),
                Quote(text = "આજ નો દિવસ શ્રેષ્ઠ છે.", authorName = "Unknown", category = daily.categoryId, language = "gu", emoji = daily.emoji)
            )
        }
    }
}

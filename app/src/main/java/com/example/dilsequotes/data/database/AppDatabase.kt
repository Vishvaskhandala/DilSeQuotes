package com.example.dilsequotes.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dilsequotes.data.dao.QuoteDao
import com.example.dilsequotes.data.model.Quote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Quote::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun quoteDao(): QuoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val roomCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    // To call a suspend function, we must launch a coroutine.
                    CoroutineScope(Dispatchers.IO).launch {
                        database.quoteDao().insertAll(getSampleQuotes())
                    }
                }
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quotes_database"
                )
                    .addCallback(roomCallback)  // Attach the callback to populate data on creation
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun getSampleQuotes(): List<Quote> {
            return listOf(
                // Love Category
                Quote(text = "प्रेम ही सबसे बड़ी शक्ति है।", category = "❤️ Love", language = "hi"),
                Quote(text = "दिल की सुनो, दिमाग की मत सुनो।", category = "❤️ Love", language = "hi"),
                Quote(text = "તમને પ્રેમ મારો જીવન છે।", category = "❤️ Love", language = "gu"),
                Quote(text = "Love is the greatest power in the world.", category = "❤️ Love", language = "en"),
                Quote(text = "दो दिल एक साथ हो सकते हैं।", category = "❤️ Love", language = "hi"),

                // Sad Category
                Quote(text = "दुःख जीवन का हिस्सा है।", category = "😢 Sad", language = "hi"),
                Quote(text = "हर आँसू में एक कहानी है।", category = "😢 Sad", language = "hi"),
                Quote(text = "દર્દ આપણને મજબૂત બનાવે છે।", category = "😢 Sad", language = "gu"),
                Quote(text = "Sometimes, sadness is the price of love.", category = "😢 Sad", language = "en"),

                // Motivation Category
                Quote(text = "हर दिन एक नया अवसर है।", category = "💪 Motivation", language = "hi"),
                Quote(text = "सफलता का कोई शॉर्टकट नहीं है।", category = "💪 Motivation", language = "hi"),
                Quote(text = "તમે જે વિચાર કરો છો તે બનો છો।", category = "💪 Motivation", language = "gu"),
                Quote(text = "Success is not final, failure is not fatal.", category = "💪 Motivation", language = "en"),
                Quote(text = "मंजिल उन्हीं को मिलती है।", category = "💪 Motivation", language = "hi"),

                // Friendship Category
                Quote(text = "दोस्ती एक सुंदर रिश्ता है।", category = "🤝 Friendship", language = "hi"),
                Quote(text = "सच्चा दोस्त दुर्लभ होता है।", category = "🤝 Friendship", language = "hi"),
                Quote(text = "મિત્રતા જીવનનો સાથી મોટો આશીર્વાદ છે।", category = "🤝 Friendship", language = "gu"),
                Quote(text = "A friend in need is a friend indeed.", category = "🤝 Friendship", language = "en"),

                // Festival Category
                Quote(text = "त्योहार खुशियों का त्योहार है।", category = "🎉 Festival", language = "hi"),
                Quote(text = "रंगों का त्योहार है होली।", category = "🎉 Festival", language = "hi"),
                Quote(text = "દિવાલી પ્રકાશ અને આનંદનો પર્વ છે।", category = "🎉 Festival", language = "gu"),
                Quote(text = "Festivals bring people together.", category = "🎉 Festival", language = "en"),

                // Daily Quotes
                Quote(text = "आज एक नई शुरुआत है।", category = "📅 Daily", language = "hi"),
                Quote(text = "जीवन को जियो, सोचो मत।", category = "📅 Daily", language = "hi"),
                Quote(text = "દર જીવન એક ભણતર છે।", category = "📅 Daily", language = "gu"),
                Quote(text = "Every day is a new beginning.", category = "📅 Daily", language = "en")
            )
        }
    }
}
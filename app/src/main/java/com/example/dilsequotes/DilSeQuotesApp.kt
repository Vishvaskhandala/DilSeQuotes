package com.example.dilsequotes

import android.app.Application
import com.example.dilsequotes.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DilSeQuotesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        seedDatabaseIfNeeded()
    }

    private fun seedDatabaseIfNeeded() {
        GlobalScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getInstance(applicationContext)
            if (database.quoteDao().getCount() == 0) {
                Logger.i("DilSeQuotesApp: Database is empty. Seeding...")
                database.quoteDao().insertAll(AppDatabase.getSampleQuotes())
                Logger.i("DilSeQuotesApp: Seeding complete.")
            }
        }
    }
}

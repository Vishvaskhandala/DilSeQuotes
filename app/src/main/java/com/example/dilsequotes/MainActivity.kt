package com.example.dilsequotes

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.dilsequotes.data.database.AppDatabase
import com.example.dilsequotes.data.repository.GetRandomQuoteUseCase
import com.example.dilsequotes.data.repository.QuoteRepository
import com.example.dilsequotes.databinding.ActivityMainBinding
import com.example.dilsequotes.viewmodel.HomeViewModelFactory
import com.example.dilsequotes.viewmodel.QuoteViewModelFactory
import kotlinx.coroutines.launch

// ============================================
// Logger Object for consistent logging
// ============================================
object Logger {
    private const val TAG = "DilSeQuotesApp"

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }

    fun w(message: String) {
        Log.w(TAG, message)
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }
}

// ============================================
// MainActivity
// ============================================
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Public factories for fragments to access
    lateinit var homeViewModelFactory: HomeViewModelFactory
    lateinit var quoteViewModelFactory: QuoteViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // IMPORTANT: Initialize factories BEFORE setContentView
        // This prevents the crash when fragments try to access them
        initializeDependencies()

        // Apply saved theme
        applySavedTheme()

        // Setup UI (this inflates layout and creates fragments)
        setupUI()
    }

    /**
     * Apply the saved theme from SharedPreferences
     */
    private fun applySavedTheme() {
        val sharedPreferences = getSharedPreferences("DilSeShayari", Context.MODE_PRIVATE)
        val themeMode = sharedPreferences.getInt(
            "theme_mode",
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    /**
     * Initialize database, repository, and ViewModelFactories
     * MUST be called BEFORE setContentView()
     */
    private fun initializeDependencies() {
        Logger.d("MainActivity: Initializing dependencies...")

        // Create database instance
        val database = AppDatabase.getInstance(applicationContext)
        val quoteDao = database.quoteDao()

        // Create repository
        val quoteRepository = QuoteRepository(quoteDao)

        // Create use case
        val getRandomQuoteUseCase = GetRandomQuoteUseCase(quoteRepository)

        // Create ViewModelFactories BEFORE setContentView
        homeViewModelFactory = HomeViewModelFactory(getRandomQuoteUseCase)
        quoteViewModelFactory = QuoteViewModelFactory(quoteRepository)

        Logger.d("MainActivity: Factories initialized successfully")

        // Initialize database with sample quotes (async, doesn't block UI)
        lifecycleScope.launch {
            quoteRepository.initializeDatabaseWithSampleQuotes()
            Logger.i("MainActivity: Database initialized with sample quotes")
        }
    }

    /**
     * Setup UI components
     */
    private fun setupUI() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Logger.d("MainActivity: onCreate - Main screen displayed")

        // Setup Navigation
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup Bottom Navigation
        binding.bottomNav.setupWithNavController(navController)
    }
}
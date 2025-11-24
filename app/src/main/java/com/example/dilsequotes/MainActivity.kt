package com.example.dilsequotes

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.dilsequotes.data.database.AppDatabase
import com.example.dilsequotes.data.repository.QuoteRepository
import com.example.dilsequotes.databinding.ActivityMainBinding
import com.example.dilsequotes.viewmodel.QuoteViewModel
import com.example.dilsequotes.viewmodel.ViewModelFactory
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

object Logger {
    private const val TAG = "DilSeQuotesApp"
    fun d(message: String) = Log.d(TAG, message)
    fun e(message: String, throwable: Throwable? = null) = Log.e(TAG, message, throwable)
    fun w(message: String) = Log.w(TAG, message)
    fun i(message: String) = Log.i(TAG, message)
}

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currentLanguage: String
    private lateinit var navController: NavController

    private val quoteViewModel: QuoteViewModel by viewModels {
        val database = AppDatabase.getInstance(applicationContext)
        val repository = QuoteRepository(database.quoteDao(), application)
        ViewModelFactory(repository, application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                applySavedLanguage()
                applySavedTheme()
                binding = ActivityMainBinding.inflate(layoutInflater)
                setContentView(binding.root)

                sharedPreferences = getSharedPreferences("DilSeShayari", Context.MODE_PRIVATE)
                currentLanguage = sharedPreferences.getString("app_language", "en") ?: "en"

                setupDrawer()
                setupLanguageToggle()
                setupNavigation()
                setupFavoriteBadge()
                Logger.d("MainActivity: UI setup is complete.")
            }
        }
    }

    private fun setupDrawer() {
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        // Set logout icon color
        val logoutItem = binding.navView.menu.findItem(R.id.nav_logout)
        logoutItem.icon?.setTintList(ColorStateList.valueOf(Color.RED))

        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun applySavedLanguage() {
        val sharedPreferences = getSharedPreferences("DilSeShayari", Context.MODE_PRIVATE)
        val languageCode = sharedPreferences.getString("app_language", "en") ?: "en"
        if (resources.configuration.locale.language != languageCode) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
            Logger.d("MainActivity: Language applied: $languageCode")
        }
    }

    private fun applySavedTheme() {
        val sharedPreferences = getSharedPreferences("DilSeShayari", Context.MODE_PRIVATE)
        val themeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    private fun setupLanguageToggle() {
        updateLanguageDisplay(currentLanguage)
        binding.btnLanguageToggle.setOnClickListener {
            showLanguageSelectionDialog()
        }
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("English", "हिंदी", "ગુજરાતી")
        val languageCodes = arrayOf("en", "hi", "gu")
        val selectedIndex = languageCodes.indexOf(currentLanguage)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_language))
            .setSingleChoiceItems(languages, selectedIndex) { dialog, which ->
                val selectedLanguage = languageCodes[which]
                if (selectedLanguage != currentLanguage) {
                    sharedPreferences.edit().putString("app_language", selectedLanguage).apply()
                    recreate()
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateLanguageDisplay(language: String) {
        binding.btnLanguageToggle.text = when (language) {
            "hi" -> "HI"
            "gu" -> "GU"
            else -> "EN"
        }
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
    }

    private fun setupFavoriteBadge() {
        quoteViewModel.getFavorites().observe(this) { favorites ->
            val favoritesBadge = binding.bottomNav.getOrCreateBadge(R.id.favorites)
            if (favorites.isNotEmpty()) {
                favoritesBadge.isVisible = true
                favoritesBadge.number = favorites.size
            } else {
                favoritesBadge.isVisible = false
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> navController.navigate(R.id.home)
            R.id.nav_profile -> navController.navigate(R.id.profile)
            R.id.nav_history -> navController.navigate(R.id.nav_history)
            R.id.nav_author -> navController.navigate(R.id.nav_author)
            R.id.nav_notifications -> navController.navigate(R.id.nav_notifications)
            R.id.nav_help -> { /* Show Help Dialog */ }
            R.id.nav_settings -> navController.navigate(R.id.settings)
            R.id.nav_about -> { /* Show About Dialog */ }
            R.id.nav_share -> shareApp()
            R.id.nav_logout -> performLogout()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out DilSe Quotes! - https://play.google.com/store/apps/details?id=$packageName")
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun performLogout() {
        sharedPreferences.edit().clear().apply()
        // Here you would typically navigate to a login screen
        // For now, we'll just close the app
        finish()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

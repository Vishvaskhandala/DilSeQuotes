package com.example.dilsequotes.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.dilsequotes.data.database.AppDatabase
import com.example.dilsequotes.data.repository.QuoteRepository
import com.example.dilsequotes.databinding.FragmentProfileBinding
import com.example.dilsequotes.viewmodel.QuoteViewModel
import com.example.dilsequotes.viewmodel.ViewModelFactory


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val quoteViewModel: QuoteViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        val repository = QuoteRepository(database.quoteDao(), requireContext())
        ViewModelFactory(repository, requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserProfile()
        setupStatistics()
        setupClickListeners()
    }

    private fun setupUserProfile() {
        val sharedPreferences = requireContext().getSharedPreferences("DilSeShayari", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "Guest User") ?: "Guest User"
        val userEmail = sharedPreferences.getString("user_email", "guest@example.com") ?: "guest@example.com"

        binding.userName.text = userName
        binding.userEmail.text = userEmail
    }

    private fun setupStatistics() {
        // Observe favorites count
        quoteViewModel.getFavorites().observe(viewLifecycleOwner) { favorites ->
            binding.favoritesCount.text = favorites.size.toString()
        }

        // Observe total quotes
      /*  quoteViewModel.getAllQuotes().observe(viewLifecycleOwner) { quotes ->
            binding.quotesCount.text = quotes.size.toString()
        }*/
    }

    private fun setupClickListeners() {
        binding.editProfileBtn.setOnClickListener {
            Toast.makeText(requireContext(), "Edit Profile - Coming Soon", Toast.LENGTH_SHORT).show()
        }
/*
        binding.changeLanguageBtn.setOnClickListener {
            showLanguageDialog()
        }*/

        binding.themeToggleBtn.setOnClickListener {
            toggleTheme()
        }

        binding.logoutBtn.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Hindi", "Urdu")
        val languageCodes = arrayOf("en", "hi", "ur")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Language")
            .setSingleChoiceItems(languages, 0) { dialog, which ->
                changeLanguage(languageCodes[which])
                dialog.dismiss()
            }
            .show()
    }

    private fun changeLanguage(languageCode: String) {
        val sharedPreferences = requireContext().getSharedPreferences("DilSeShayari", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("app_language", languageCode).apply()

        // Set locale
        val locale = java.util.Locale(languageCode)
        java.util.Locale.setDefault(locale)
        val config = requireContext().resources.configuration
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)

        Toast.makeText(requireContext(), "Language changed to $languageCode", Toast.LENGTH_SHORT).show()

        // Restart activity to apply changes
        requireActivity().recreate()
    }

    private fun toggleTheme() {
        val sharedPreferences = requireContext().getSharedPreferences("DilSeShayari", Context.MODE_PRIVATE)
        val currentTheme = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        val newTheme = when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        sharedPreferences.edit().putInt("theme_mode", newTheme).apply()
        AppCompatDelegate.setDefaultNightMode(newTheme)

        val themeName = when (newTheme) {
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark Mode"
            AppCompatDelegate.MODE_NIGHT_NO -> "Light Mode"
            else -> "System Default"
        }

        binding.themeToggleBtn.text = themeName
        Toast.makeText(requireContext(), "Theme changed to $themeName", Toast.LENGTH_SHORT).show()
    }

    private fun showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        val sharedPreferences = requireContext().getSharedPreferences("DilSeShayari", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

//        yet not implements
        // Navigate to home or login screen
//        findNavController().navigate(R.id.home)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
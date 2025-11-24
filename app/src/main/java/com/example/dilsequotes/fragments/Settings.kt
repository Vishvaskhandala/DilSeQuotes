package com.example.dilsequotes.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.dilsequotes.R
import com.example.dilsequotes.databinding.FragmentSettingsBinding

class Settings : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.cardTheme.setOnClickListener {
            showThemeSelectionDialog()
        }

        binding.cardRateUs.setOnClickListener {
            openPlayStoreForRating()
        }

        binding.cardShareApp.setOnClickListener {
            shareApp()
        }

        binding.cardPrivacyPolicy.setOnClickListener {
            openPrivacyPolicy()
        }
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf("Light", "Dark", "System Default")
        val themeModes = arrayOf(
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )

        val sharedPreferences = requireContext().getSharedPreferences("DilSeShayari", Context.MODE_PRIVATE)
        val currentTheme = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val selectedIndex = themeModes.indexOf(currentTheme)

        AlertDialog.Builder(requireContext())
            .setTitle("Select Theme")
            .setSingleChoiceItems(themes, selectedIndex) { dialog, which ->
                val selectedMode = themeModes[which]
                if (selectedMode != currentTheme) {
                    sharedPreferences.edit().putInt("theme_mode", selectedMode).apply()
                    AppCompatDelegate.setDefaultNightMode(selectedMode)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openPlayStoreForRating() {
        // Replace with your app's package name
        val packageName = requireContext().packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun shareApp() {
        val appName = getString(R.string.app_name)
        val packageName = requireContext().packageName
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this amazing quotes app: $appName\n\nhttps://play.google.com/store/apps/details?id=$packageName")
        }
        startActivity(Intent.createChooser(shareIntent, "Share app via"))
    }

    private fun openPrivacyPolicy() {
        // Replace with your privacy policy URL
        val url = "https://www.yourprivacypolicy.com"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

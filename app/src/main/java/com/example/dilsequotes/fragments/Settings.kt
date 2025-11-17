package com.example.dilsequotes.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.dilsequotes.Logger
import com.example.dilsequotes.databinding.FragmentSettingsBinding

class Settings : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

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

        Logger.d("SettingsFragment: onViewCreated - Settings screen displayed")
        sharedPreferences = requireContext().getSharedPreferences("DilSeShayari", Context.MODE_PRIVATE)

        binding.cardTheme.setOnClickListener {
            Logger.d("SettingsFragment: Appearance card tapped")
            showThemeSelectionDialog()
        }

        binding.cardRateUs.setOnClickListener {
            Logger.d("SettingsFragment: Rate Us card tapped")
            // Add rate us logic here
        }

        binding.cardShareApp.setOnClickListener {
            Logger.d("SettingsFragment: Share App card tapped")
            // Add share app logic here
        }

        binding.cardPrivacyPolicy.setOnClickListener {
            Logger.d("SettingsFragment: Privacy Policy card tapped")
            // Add privacy policy logic here
        }
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf("Light", "Dark", "System Default")
        val modes = arrayOf(
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )

        val currentNightMode = AppCompatDelegate.getDefaultNightMode()
        val selectedIndex = modes.indexOf(currentNightMode)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Theme")
            .setSingleChoiceItems(themes, selectedIndex) { dialog, which ->
                val selectedMode = modes[which]
                AppCompatDelegate.setDefaultNightMode(selectedMode)
                sharedPreferences.edit().putInt("theme_mode", selectedMode).apply()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
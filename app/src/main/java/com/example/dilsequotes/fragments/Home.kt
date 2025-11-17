package com.example.dilsequotes.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dilsequotes.Logger
import com.example.dilsequotes.MainActivity
import com.example.dilsequotes.R
import com.example.dilsequotes.adapters.CategoryAdapter
import com.example.dilsequotes.data.model.Category
import com.example.dilsequotes.databinding.FragmentHomeBinding
import com.example.dilsequotes.viewmodel.HomeViewModel

class Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewModel: HomeViewModel

    private val categories = listOf(
        Category(name = "❤️ Love", quoteCount = 150, gradientDrawable = 0),
        Category(name = "😢 Sad", quoteCount = 120, gradientDrawable = 0),
        Category(name = "💪 Motivation", quoteCount = 200, gradientDrawable = 0),
        Category(name = "🤝 Friendship", quoteCount = 100, gradientDrawable = 0),
        Category(name = "🎉 Festival", quoteCount = 80, gradientDrawable = 0),
        Category(name = "📅 Daily", quoteCount = 365, gradientDrawable = 0)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Logger.d("HomeFragment: onViewCreated - Home screen displayed")

        // Get the factory from the Activity
        val viewModelFactory = (requireActivity() as MainActivity).homeViewModelFactory
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]

        sharedPreferences = requireContext().getSharedPreferences("DilSeShayari", Context.MODE_PRIVATE)

        setupCategoryRecyclerView()
        setupLanguageToggle()
        setupObservers()

        viewModel.loadDailyQuote()

        val savedLanguage = sharedPreferences.getString("app_language", "en") ?: "en"
        updateUILanguage(savedLanguage)
    }

    private fun setupObservers() {
        viewModel.quoteOfDay.observe(viewLifecycleOwner) { quote ->
            if (quote != null) {
                binding.tvQuoteOfDay.text = "\"${quote.text}\"\n\n- ${quote.category}"
            } else {
                binding.tvQuoteOfDay.text = getString(R.string.quote_placeholder)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.tvQuoteOfDay.text = if (isLoading) "Loading..." else binding.tvQuoteOfDay.text
        }
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            onCategorySelected(category)
        }
        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
        categoryAdapter.submitList(categories)
    }

    private fun setupLanguageToggle() {
        binding.btnLanguageToggle.setOnClickListener {
            showLanguageSelectionDialog()
        }
        val currentLanguage = sharedPreferences.getString("app_language", "en") ?: "en"
        updateLanguageDisplay(currentLanguage)
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("English", "हिंदी", "ગુજરાતી")
        val languageCodes = arrayOf("en", "hi", "gu")
        val currentLang = sharedPreferences.getString("app_language", "en") ?: "en"
        val selectedIndex = languageCodes.indexOf(currentLang)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Language")
            .setSingleChoiceItems(languages, selectedIndex) { dialog, which ->
                val selectedLanguage = languageCodes[which]
                if (selectedLanguage != currentLang) {
                    sharedPreferences.edit().putString("app_language", selectedLanguage).apply()
                    updateLanguageDisplay(selectedLanguage)
                    updateUILanguage(selectedLanguage)
                    Toast.makeText(requireContext(), "Language Changed to ${languages[which]}", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun updateLanguageDisplay(language: String) {
        binding.btnLanguageToggle.text = when (language) {
            "hi" -> "HI"
            "gu" -> "GU"
            else -> "EN"
        }
    }

    private fun updateUILanguage(language: String) {
        when (language) {
            "en" -> {
                binding.tvHeader.text = "💝 DilSe Shayari"
                binding.tvTagline.text = "Express Your Feelings"
                binding.tvDailyTitle.text = "Quote of the Day 🌤️"
                binding.tvBrowseCategories.text = "Browse Categories"
            }
            "hi" -> {
                binding.tvHeader.text = "💝 दिलसे शायरी"
                binding.tvTagline.text = "अपनी भावनाओं को व्यक्त करें"
                binding.tvDailyTitle.text = "आज का अनमोल विचार 🌤️"
                binding.tvBrowseCategories.text = "श्रेणियाँ देखें"
            }
            "gu" -> {
                binding.tvHeader.text = "💝 દિલસે શાયરી"
                binding.tvTagline.text = "તમારી લાગણીઓ વ્યક્ત કરો"
                binding.tvDailyTitle.text = "આજનો અમૂલ્ય વિચાર 🌤️"
                binding.tvBrowseCategories.text = "શ્રેણીઓ બ્રાઉઝ કરો"
            }
        }
    }

    private fun onCategorySelected(category: Category) {
        Logger.d("HomeFragment: onCategorySelected - ${category.name}")
        val bundle = Bundle().apply {
            putString("categoryName", category.name)
        }
        findNavController().navigate(R.id.action_home_to_categoryQuotes, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

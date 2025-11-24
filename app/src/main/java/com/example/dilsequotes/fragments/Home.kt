package com.example.dilsequotes.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dilsequotes.Logger
import com.example.dilsequotes.R
import com.example.dilsequotes.adapters.CategoryAdapter
import com.example.dilsequotes.data.database.AppDatabase
import com.example.dilsequotes.data.model.CategoryData
import com.example.dilsequotes.data.model.Quote
import com.example.dilsequotes.data.repository.QuoteRepository
import com.example.dilsequotes.databinding.FragmentHomeBinding
import com.example.dilsequotes.viewmodel.HomeViewModel
import com.example.dilsequotes.viewmodel.ViewModelFactory

class Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var sharedPreferences: SharedPreferences

    private val viewModel: HomeViewModel by viewModels {
        val database = AppDatabase.getInstance(requireContext())
        val repository = QuoteRepository(database.quoteDao(), requireContext())
        ViewModelFactory(repository, requireContext())
    }

    private lateinit var currentLanguage: String

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

        sharedPreferences = requireContext().getSharedPreferences("DilSeShayari", Context.MODE_PRIVATE)
        currentLanguage = sharedPreferences.getString("app_language", "en") ?: "en"

        setupCategoryRecyclerView()
        setupObservers()
        setupClickListeners()

        viewModel.loadDailyQuote(currentLanguage)
        viewModel.loadCategories()
    }

    private fun setupClickListeners() {
        // Create Quote button
        binding.btnCreateQuote.setOnClickListener {
            // Create a blank quote for the editor
            val blankQuote = Quote(
                id = 0,
                text = "",
                authorName = "",
                category = "custom",
                language = currentLanguage,
                source = "user_created",
                likes = 0,
                dateAdded = System.currentTimeMillis(),
                emoji = "✨",
                isFavorite = false
            )

            val bundle = Bundle().apply {
                putParcelable("quote", blankQuote)
            }
            findNavController().navigate(R.id.quoteEditorFragment, bundle)
        }

        // Trending button
        binding.btnTrending.setOnClickListener {
            Toast.makeText(requireContext(), "Trending quotes coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        viewModel.quoteOfDay.observe(viewLifecycleOwner) { quote ->
            if (quote != null) {
                Logger.d("HomeFragment: Quote received and displayed - ${quote.text}")
                binding.tvQuoteOfDay.text = "\"${quote.text}\"\n\n- ${quote.authorName}"
                binding.quoteCard.setOnClickListener { onQuoteClick(quote) }
            } else {
                Logger.d("HomeFragment: Received null quote")
                binding.tvQuoteOfDay.text = getString(R.string.quote_placeholder)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.tvQuoteOfDay.text = if (isLoading) getString(R.string.loading) else viewModel.quoteOfDay.value?.text
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = CategoryAdapter(
            onCategoryClick = { category -> onCategorySelected(category) },
            getLocalizedName = { category -> requireContext().getString(category.nameResId) }
        )
        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
    }

    private fun onCategorySelected(category: CategoryData) {
        Logger.d("HomeFragment: onCategorySelected - ID: ${category.categoryId}")
        val bundle = Bundle().apply {
            putString("categoryId", category.categoryId)
        }
        findNavController().navigate(R.id.action_home_to_categoryQuotes, bundle)
    }

    private fun onQuoteClick(quote: Quote) {
        val bottomSheet = QuoteDetailsBottomSheet.newInstance(quote)

        bottomSheet.onEditClicked = { selectedQuote ->
            val bundle = Bundle().apply {
                putParcelable("quote", selectedQuote)
            }
            findNavController().navigate(R.id.quoteEditorFragment, bundle)
        }

        bottomSheet.show(childFragmentManager, "QuoteDetailsBottomSheet")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
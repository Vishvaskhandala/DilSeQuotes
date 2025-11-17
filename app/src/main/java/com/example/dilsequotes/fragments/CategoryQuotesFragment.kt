package com.example.dilsequotes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dilsequotes.Logger
import com.example.dilsequotes.adapter.QuoteAdapter
import com.example.dilsequotes.data.database.AppDatabase
import com.example.dilsequotes.data.model.Quote
import com.example.dilsequotes.data.repository.QuoteRepository
import com.example.dilsequotes.databinding.FragmentCategoryQuotesBinding
import com.example.dilsequotes.viewmodel.QuoteViewModel
import com.example.dilsequotes.viewmodel.QuoteViewModelFactory

class CategoryQuotesFragment : Fragment() {

    // View binding for type-safe view access
    private var _binding: FragmentCategoryQuotesBinding? = null
    private val binding get() = _binding!!

    // ViewModel and adapter
    private lateinit var viewModel: QuoteViewModel
    private lateinit var quoteAdapter: QuoteAdapter

    // Category name passed from previous screen
    private val categoryName: String?
        get() = arguments?.getString(ARG_CATEGORY_NAME)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryQuotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Logger.d("CategoryQuotesFragment: Displaying quotes for category: $categoryName")

        // Setup ViewModel with dependency injection
        setupViewModel()

        // Setup RecyclerView with adapter
        setupRecyclerView()

        // Load quotes for the selected category
        loadQuotesForCategory()
    }

    /**
     * Initialize ViewModel with repository and factory
     */
    private fun setupViewModel() {
        val database = AppDatabase.getInstance(requireContext())
        val quoteDao = database.quoteDao()
        val repository = QuoteRepository(quoteDao)
        val factory = QuoteViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[QuoteViewModel::class.java]
    }

    /**
     * Setup RecyclerView with adapter and layout manager
     */
    private fun setupRecyclerView() {
        quoteAdapter = QuoteAdapter(
            onQuoteClick = { quote ->
                Logger.d("CategoryQuotesFragment: Quote clicked - ${quote.text}")
                // Show quote details in a toast or dialog
                showQuoteDetails(quote)
            },
            onFavoriteClick = { quote ->
                Logger.d("CategoryQuotesFragment: Toggling favorite for quote - ${quote.text}")
                viewModel.toggleFavorite(quote)

                // Show feedback to user
                val message = if (quote.isFavorite) {
                    "Removed from favorites"
                } else {
                    "Added to favorites"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )

        binding.categoryQuotesRecyclerView.apply {
            adapter = quoteAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    /**
     * Show quote details when clicked
     */
    private fun showQuoteDetails(quote: Quote) {
        val details = """
            ${quote.text}
            
            - ${quote.author}
            Category: ${quote.category}
            Language: ${getLanguageName(quote.language)}
        """.trimIndent()

        Toast.makeText(requireContext(), details, Toast.LENGTH_LONG).show()

        // TODO: You can replace this with a bottom sheet or dialog for better UX
    }

    /**
     * Convert language code to readable name
     */
    private fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "hi" -> "Hindi"
            "es" -> "Spanish"
            else -> "Unknown"
        }
    }

    /**
     * Load and observe quotes for the selected category
     */
    private fun loadQuotesForCategory() {
        val category = categoryName

        if (category.isNullOrEmpty()) {
            Logger.e("CategoryQuotesFragment: Category name is null or empty")
            Toast.makeText(requireContext(), "Error: Invalid category", Toast.LENGTH_SHORT).show()
            // Navigate back or show error
            return
        }

        // Observe quotes from database
        viewModel.getQuotes(category).observe(viewLifecycleOwner) { quotes ->
            Logger.d("CategoryQuotesFragment: Received ${quotes.size} quotes for category: $category")

            if (quotes.isEmpty()) {
                Logger.w("CategoryQuotesFragment: No quotes found for category: $category")
                showEmptyState()
            } else {
                hideEmptyState()
            }

            quoteAdapter.submitList(quotes)
        }
    }

    /**
     * Show empty state when no quotes are found
     */
    private fun showEmptyState() {
        // TODO: Implement empty state UI
        Toast.makeText(requireContext(), "No quotes found in this category", Toast.LENGTH_SHORT).show()
    }

    /**
     * Hide empty state when quotes are available
     */
    private fun hideEmptyState() {
        // TODO: Hide empty state UI if implemented
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CATEGORY_NAME = "categoryName"

        /**
         * Create a new instance of CategoryQuotesFragment with category name
         */
        fun newInstance(categoryName: String): CategoryQuotesFragment {
            return CategoryQuotesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_NAME, categoryName)
                }
            }
        }
    }
}
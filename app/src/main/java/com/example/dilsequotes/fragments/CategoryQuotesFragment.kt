package com.example.dilsequotes.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dilsequotes.Logger
import com.example.dilsequotes.R
import com.example.dilsequotes.adapters.QuoteAdapter
import com.example.dilsequotes.data.database.AppDatabase
import com.example.dilsequotes.data.model.CategoryConstants
import com.example.dilsequotes.data.model.Quote
import com.example.dilsequotes.data.repository.QuoteRepository
import com.example.dilsequotes.databinding.FragmentCategoryQuotesBinding
import com.example.dilsequotes.viewmodel.QuoteViewModel
import com.example.dilsequotes.viewmodel.ViewModelFactory

class CategoryQuotesFragment : Fragment() {

    private var _binding: FragmentCategoryQuotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuoteViewModel by activityViewModels {
        val database = AppDatabase.getInstance(requireContext())
        val repository = QuoteRepository(database.quoteDao(), requireContext())
        ViewModelFactory(repository, requireContext())
    }
    private lateinit var quoteAdapter: QuoteAdapter
    private lateinit var sharedPreferences: SharedPreferences

    private var categoryId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryId = it.getString("categoryId")
        }
    }

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

        if (categoryId == null) {
            Logger.e("CategoryQuotesFragment: Invalid categoryId provided.")
            Toast.makeText(requireContext(), "Error: Invalid category", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        sharedPreferences = requireContext().getSharedPreferences("DilSeShayari", Context.MODE_PRIVATE)

        setupRecyclerView()
        setupToolbar()
        observeViewModel()
        loadQuotes()
    }

    private fun setupRecyclerView() {
        quoteAdapter = QuoteAdapter(
            onQuoteClick = { quote -> showQuoteDetails(quote) },
            onFavoriteClick = { quote ->
                viewModel.toggleFavorite(quote)

                val message = if (!quote.isFavorite) {
                    "Added to Favorites ❤️"
                } else {
                    "Removed from Favorites"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
        binding.categoryQuotesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = quoteAdapter
        }
    }

    private fun setupToolbar() {
        val category = CategoryConstants.ALL_CATEGORIES.firstOrNull { it.categoryId == categoryId }
        if (category != null) {
            val localizedTitle = getString(category.nameResId)
            binding.toolbar.title = "${category.emoji} $localizedTitle"
        } else {
            binding.toolbar.title = "Quotes"
        }

        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun observeViewModel() {
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            Logger.d("CategoryQuotesFragment: Received ${quotes.size} quotes")
            quoteAdapter.submitList(quotes.toList())
            updateEmptyState(quotes.isEmpty())
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun loadQuotes() {
        val language = sharedPreferences.getString("app_language", "en") ?: "en"
        categoryId?.let {
            viewModel.loadQuotesByCategory(it, language)
        } ?: Logger.e("CategoryQuotesFragment: Could not find category for ID: $categoryId")
    }

    private fun showQuoteDetails(quote: Quote) {
        val bottomSheet = QuoteDetailsBottomSheet.newInstance(quote)

        // NEW: Add edit callback
        bottomSheet.onEditClicked = { selectedQuote ->
            // Navigate to editor
            val bundle = Bundle().apply {
                putParcelable("quote", selectedQuote)
            }
            findNavController().navigate(R.id.quoteEditorFragment, bundle)
        }

        bottomSheet.show(childFragmentManager, "QuoteDetailsBottomSheet")
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyMessage.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.categoryQuotesRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadQuotes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
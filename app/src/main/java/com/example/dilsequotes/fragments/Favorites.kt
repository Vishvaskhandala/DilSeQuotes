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
import com.example.dilsequotes.MainActivity
import com.example.dilsequotes.adapter.QuoteAdapter
import com.example.dilsequotes.data.model.Quote
import com.example.dilsequotes.databinding.FragmentFavoritesBinding
import com.example.dilsequotes.viewmodel.QuoteViewModel

class Favorites : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: QuoteViewModel
    private lateinit var quoteAdapter: QuoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Logger.d("FavoritesFragment: onViewCreated - Favorites screen displayed")

        // Get the factory from the Activity
        val factory = (requireActivity() as MainActivity).quoteViewModelFactory
        viewModel = ViewModelProvider(this, factory)[QuoteViewModel::class.java]

        setupRecyclerView()
        observeFavorites()
    }

    /**
     * Setup RecyclerView with adapter
     */
    private fun setupRecyclerView() {
        quoteAdapter = QuoteAdapter(
            onQuoteClick = { quote ->
                Logger.d("FavoritesFragment: Quote clicked - ${quote.text}")
                showQuoteDetails(quote)
            },
            onFavoriteClick = { quote ->
                Logger.d("FavoritesFragment: Toggling favorite - ${quote.text}")
                viewModel.toggleFavorite(quote)
                Toast.makeText(
                    requireContext(),
                    "Removed from favorites",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        binding.favoritesRecyclerView.apply {
            adapter = quoteAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    /**
     * Observe favorites list from database
     */
    private fun observeFavorites() {
        viewModel.getFavorites().observe(viewLifecycleOwner) { favorites ->
            Logger.d("FavoritesFragment: Received ${favorites.size} favorite quotes")
            quoteAdapter.submitList(favorites)

            // Show/hide empty state
            updateEmptyState(favorites.isEmpty())
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

        // TODO: Replace with a bottom sheet or dialog for better UX
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
     * Update empty state visibility
     */
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyMessage.visibility = View.VISIBLE
            binding.favoritesRecyclerView.visibility = View.GONE
            Logger.w("FavoritesFragment: No favorite quotes found")
        } else {
            binding.emptyMessage.visibility = View.GONE
            binding.favoritesRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
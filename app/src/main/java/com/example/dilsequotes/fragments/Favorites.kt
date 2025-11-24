package com.example.dilsequotes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dilsequotes.Logger
import com.example.dilsequotes.adapters.QuoteAdapter
import com.example.dilsequotes.data.model.Quote
import com.example.dilsequotes.databinding.FragmentFavoritesBinding
import com.example.dilsequotes.viewmodel.QuoteViewModel

class Favorites : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuoteViewModel by activityViewModels()
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

        setupRecyclerView()
        observeFavorites()
    }

    private fun setupRecyclerView() {
        quoteAdapter = QuoteAdapter(
            onQuoteClick = { quote -> showQuoteDetails(quote) },
            onFavoriteClick = { quote ->
                viewModel.toggleFavorite(quote)
                Toast.makeText(requireContext(), "Removed From Favorite ", Toast.LENGTH_SHORT).show()
            }
        )

        binding.favoritesRecyclerView.apply {
            adapter = quoteAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeFavorites() {
        viewModel.getFavorites().observe(viewLifecycleOwner) { favorites ->
            Logger.d("FavoritesFragment: Received ${favorites.size} favorite quotes")
            quoteAdapter.submitList(favorites)
            updateEmptyState(favorites.isEmpty())
        }
    }

    private fun showQuoteDetails(quote: Quote) {
        val bottomSheet = QuoteDetailsBottomSheet.newInstance(quote)
        bottomSheet.show(parentFragmentManager, "QuoteDetailsBottomSheet")
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyMessage.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.favoritesRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

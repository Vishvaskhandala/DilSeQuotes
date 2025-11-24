package com.example.dilsequotes.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.dilsequotes.R
import com.example.dilsequotes.data.model.Quote
import com.example.dilsequotes.databinding.BottomSheetQuoteDetailsBinding
import com.example.dilsequotes.viewmodel.QuoteViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class QuoteDetailsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetQuoteDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuoteViewModel by activityViewModels()
    private var currentQuote: Quote? = null

    // Callback for navigation to editor
    var onEditClicked: ((Quote) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetQuoteDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            currentQuote = it.getParcelable(ARG_QUOTE)
        }

        currentQuote?.let {
            setupViews(it)
            setupClickListeners()
            applyCategoryGradient(it.category)
        }
    }

    private fun setupViews(quote: Quote) {
        binding.quoteText.text = quote.text
        binding.quoteAuthor.text = "- ${quote.authorName}"
        updateFavoriteButton(quote.isFavorite)
    }

    private fun applyCategoryGradient(category: String) {
        val gradientResId = when (category.lowercase()) {
            "love", "romantic", "ishq" -> R.drawable.quote_gradient_love
            "sad", "dard", "broken" -> R.drawable.quote_gradient_sad
            "motivational", "inspiration" -> R.drawable.quote_gradient_motivational
            "friendship", "dosti" -> R.drawable.quote_gradient_friendship
            "life", "zindagi" -> R.drawable.quote_gradient_life
            "attitude" -> R.drawable.quote_gradient_attitude
            "nature", "beauty" -> R.drawable.quote_gradient_nature
            "success" -> R.drawable.quote_gradient_success
            else -> R.drawable.bottom_sheet_gradient_bg
        }

        val drawable = ContextCompat.getDrawable(requireContext(), gradientResId)
        binding.root.background = drawable
    }

    private fun setupClickListeners() {
        binding.copyButton.setOnClickListener {
            animateButton(it)
            currentQuote?.let { quote ->
                copyToClipboard(quote)
            }
        }

        binding.favoriteButton.setOnClickListener {
            animateButton(it)
            currentQuote?.let { quote ->
                viewModel.toggleFavorite(quote)
                currentQuote = quote.copy(isFavorite = !quote.isFavorite)
                updateFavoriteButton(currentQuote!!.isFavorite)

                val message = if (currentQuote!!.isFavorite) {
                    "Added to Favorites ❤️"
                } else {
                    "Removed from Favorites"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        // NEW: Edit button functionality
        binding.editButton.setOnClickListener {
            animateButton(it)
            currentQuote?.let { quote ->
                dismiss()
                onEditClicked?.invoke(quote)
            }
        }

        binding.shareButton.setOnClickListener {
            animateButton(it)
            currentQuote?.let { quote ->
                shareQuote(quote)
            }
        }
    }

    private fun animateButton(view: View) {
        view.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        val favoriteIcon = if (isFavorite) {
            R.drawable.ic_favorite
        } else {
            R.drawable.ic_favorite_border
        }
        binding.favoriteButton.setImageResource(favoriteIcon)
    }

    private fun copyToClipboard(quote: Quote) {
        val clipboard = ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
        val clip = ClipData.newPlainText("quote", "\"${quote.text}\" - ${quote.authorName}")
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Copied to clipboard ✓", Toast.LENGTH_SHORT).show()
    }

    private fun shareQuote(quote: Quote) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "\"${quote.text}\" - ${quote.authorName}\n\nShared via DilSe Quotes")
        }
        startActivity(Intent.createChooser(shareIntent, "Share quote via"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_QUOTE = "quote"

        fun newInstance(quote: Quote): QuoteDetailsBottomSheet {
            return QuoteDetailsBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_QUOTE, quote)
                }
            }
        }
    }
}
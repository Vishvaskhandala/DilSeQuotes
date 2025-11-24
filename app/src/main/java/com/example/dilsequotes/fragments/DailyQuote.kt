package com.example.dilsequotes.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.dilsequotes.Logger
import com.example.dilsequotes.R
import com.example.dilsequotes.data.database.AppDatabase
import com.example.dilsequotes.data.model.CategoryConstants
import com.example.dilsequotes.data.model.Quote
import com.example.dilsequotes.data.repository.QuoteRepository
import com.example.dilsequotes.databinding.FragmentDailyQuoteBinding
import com.example.dilsequotes.viewmodel.QuoteViewModel
import com.example.dilsequotes.viewmodel.ViewModelFactory
import java.io.File
import java.io.FileOutputStream

class DailyQuote : Fragment() {

    private var _binding: FragmentDailyQuoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuoteViewModel by activityViewModels {
        val database = AppDatabase.getInstance(requireContext())
        val repository = QuoteRepository(database.quoteDao(), requireContext()) // Pass context for network check
        ViewModelFactory(repository,context)
    }
    private lateinit var sharedPreferences: SharedPreferences
    private var currentQuote: Quote? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyQuoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Logger.d("DailyQuote: onViewCreated - Daily quote screen displayed")

        sharedPreferences = requireContext().getSharedPreferences("DilSeShayari", Context.MODE_PRIVATE)

        setupClickListeners()
        observeViewModel()
        loadDailyQuote()
    }

    private fun setupClickListeners() {
        binding.refreshButton.setOnClickListener {
            Logger.d("DailyQuote: Refresh button tapped")
            loadDailyQuote() // Refresh quotes based on network status
        }

        binding.favoriteButton.setOnClickListener {
            currentQuote?.let {
                Logger.d("DailyQuote: Favorite button tapped for quote: ${it.text}")
                viewModel.toggleFavorite(it)
                val updatedQuote = it.copy(isFavorite = !it.isFavorite)
                currentQuote = updatedQuote
                updateFavoriteButton(updatedQuote)
            }
        }

        binding.copyButton.setOnClickListener {
            currentQuote?.let {
                Logger.d("DailyQuote: Copy button tapped for quote: ${it.text}")
                copyToClipboard(it.text)
            }
        }

        binding.shareButton.setOnClickListener {
            currentQuote?.let {
                Logger.d("DailyQuote: Share button tapped for quote: ${it.text}")
                shareQuote(it.text, it.authorName)
            }
        }

    }

    private fun observeViewModel() {
        viewModel.quotes.observe(viewLifecycleOwner) { quotes ->
            if (quotes.isNotEmpty()) {
                val quote = quotes.random()
                currentQuote = quote
                val fadeIn = AlphaAnimation(0f, 1f)
                fadeIn.duration = 800
                binding.quoteText.startAnimation(fadeIn)
                binding.quoteText.text = quote.text
                binding.quoteAuthor.text = "- ${quote.authorName}"
                updateFavoriteButton(quote)
            } else {
                binding.quoteText.text = "No quotes available for today."
                binding.quoteAuthor.text = ""
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Handle loading state, e.g., show or hide a progress indicator
        }
    }

    private fun loadDailyQuote() {
        val language = sharedPreferences.getString("app_language", "en") ?: "en"
        val dailyCategory = CategoryConstants.ALL_CATEGORIES.firstOrNull { it.categoryId == "daily" }
        dailyCategory?.let {
            viewModel.loadQuotesByCategory(it.categoryId, language)
        } ?: Logger.e("DailyQuote: Could not find 'daily' category.")
    }

    private fun updateFavoriteButton(quote: Quote) {
        val icon = if (quote.isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        binding.favoriteButton.setIconResource(icon)
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("quote", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Quote copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun shareQuote(text: String, author: String) {
        val shareText = "\"$text\"\n- $author\n\nShared from DilSe Quotes"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share quote via"))
    }

    private fun shareQuoteAsImage(quoteText: String, authorText: String) {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.quote_image_layout, null)

        val quoteTextView = view.findViewById<TextView>(R.id.quote_text_image)
        val authorTextView = view.findViewById<TextView>(R.id.quote_author_image)

        quoteTextView.text = quoteText
        authorTextView.text = "- $authorText"

        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.draw(canvas)

        val uri = saveBitmapToCache(bitmap)

        if (uri != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share quote as image"))
        } else {
            Toast.makeText(requireContext(), "Failed to share quote as image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        val imagePath = File(requireContext().cacheDir, "images")
        imagePath.mkdirs()
        val file = File(imagePath, "quote_image.png")
        return try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        } catch (e: Exception) {
            Logger.e("Failed to save bitmap to cache", e)
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.dilsequotes.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.ViewModelProvider
import com.example.dilsequotes.Logger
import com.example.dilsequotes.MainActivity
import com.example.dilsequotes.R
import com.example.dilsequotes.data.model.Quote
import com.example.dilsequotes.databinding.FragmentDailyQuoteBinding
import com.example.dilsequotes.viewmodel.QuoteViewModel
import java.io.File
import java.io.FileOutputStream

class DailyQuote : Fragment() {

    private var _binding: FragmentDailyQuoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: QuoteViewModel
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

        // Get the factory from the Activity
        val factory = (requireActivity() as MainActivity).quoteViewModelFactory
        viewModel = ViewModelProvider(this, factory)[QuoteViewModel::class.java]

        loadDailyQuote()

        binding.refreshButton.setOnClickListener {
            Logger.d("DailyQuote: Refresh button tapped")
            loadDailyQuote()
        }

        binding.favoriteButton.setOnClickListener {
            currentQuote?.let {
                Logger.d("DailyQuote: Favorite button tapped for quote: ${it.text}")
                viewModel.toggleFavorite(it)
                updateFavoriteButton(it)
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
                shareQuote(it.text)
            }
        }

        binding.shareImageButton.setOnClickListener {
            currentQuote?.let {
                Logger.d("DailyQuote: Share as image button tapped for quote: ${it.text}")
                shareQuoteAsImage(it.text)
            }
        }
    }

    private fun loadDailyQuote() {
        viewModel.getQuotes("📅 Daily").observe(viewLifecycleOwner) { quotes ->
            if (quotes.isNotEmpty()) {
                val quote = quotes.random()
                currentQuote = quote
                val fadeIn = AlphaAnimation(0f, 1f)
                fadeIn.duration = 1000
                binding.quoteText.startAnimation(fadeIn)
                binding.quoteText.text = quote.text
                updateFavoriteButton(quote)
            } else {
                binding.quoteText.text = "No quotes available for today."
            }
        }
    }

    private fun updateFavoriteButton(quote: Quote) {
        if (quote.isFavorite) {
            binding.favoriteButton.setIconResource(R.drawable.ic_favorite)
        } else {
            binding.favoriteButton.setIconResource(R.drawable.ic_favorite_border)
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("quote", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Quote copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun shareQuote(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Share quote via"))
    }

    private fun shareQuoteAsImage(quoteText: String) {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.quote_image_layout, null)
        val quoteTextView = view.findViewById<TextView>(R.id.quote_text_image)
        quoteTextView.text = quoteText

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
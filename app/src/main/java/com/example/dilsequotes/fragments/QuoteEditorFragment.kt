package com.example.dilsequotes.fragments

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dilsequotes.adapters.BackgroundAdapter
import com.example.dilsequotes.data.model.Quote
import com.example.dilsequotes.databinding.FragmentQuoteEditorBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class QuoteEditorFragment : Fragment() {

    private var _binding: FragmentQuoteEditorBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentQuote: Quote
    private var isCreateMode = false

    // Editor state
    private var currentBackgroundIndex = -1 // -1 means white default
    private var currentFontSize = 20f
    private var currentAlignment = Paint.Align.CENTER
    private var currentTextColor = Color.parseColor("#333333")

    // Background gradients
    private val backgroundColors = listOf(
        intArrayOf(0xFF667eea.toInt(), 0xFF764ba2.toInt()),
        intArrayOf(0xFFf093fb.toInt(), 0xFFF5576c.toInt()),
        intArrayOf(0xFF4facfe.toInt(), 0xFF00f2fe.toInt()),
        intArrayOf(0xFF43e97b.toInt(), 0xFF38f9d7.toInt()),
        intArrayOf(0xFFfa709a.toInt(), 0xFFfee140.toInt()),
        intArrayOf(0xFF30cfd0.toInt(), 0xFF330867.toInt()),
        intArrayOf(0xFFa8edea.toInt(), 0xFFfed6e3.toInt()),
        intArrayOf(0xFFff9a9e.toInt(), 0xFFfecfef.toInt()),
        intArrayOf(0xFFffecd2.toInt(), 0xFFfcb69f.toInt()),
        intArrayOf(0xFFff6e7f.toInt(), 0xFFbfe9ff.toInt())
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuoteEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentQuote = arguments?.getParcelable(ARG_QUOTE) ?: run {
            Toast.makeText(requireContext(), "Error: No quote data", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        isCreateMode = currentQuote.text.isEmpty()

        setupUI()
        setupListeners()
        updatePreview()
    }

    private fun setupUI() {
        binding.toolbarTitle.text = if (isCreateMode) "Create Quote" else "Edit Quote"

        if (!isCreateMode) {
            binding.etQuoteText.setText(currentQuote.text)
            binding.etAuthorName.setText(currentQuote.authorName)
        }

        binding.sliderFontSize.value = currentFontSize
        binding.tvFontSizeValue.text = "${currentFontSize.toInt()}sp"

        // Initialize alignment buttons
        updateAlignmentButtons()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            if (validateInput()) {
                saveQuoteAsImage()
            }
        }

        binding.btnShare.setOnClickListener {
            if (validateInput()) {
                shareQuoteImage()
            }
        }

        // Background adapter
        binding.rvBackgrounds.adapter = BackgroundAdapter(backgroundColors) { index ->
            currentBackgroundIndex = index
            updatePreview()
        }

        // Alignment buttons
        binding.btnAlignLeft.setOnClickListener {
            currentAlignment = Paint.Align.LEFT
            updateAlignmentButtons()
            updatePreview()
        }

        binding.btnAlignCenter.setOnClickListener {
            currentAlignment = Paint.Align.CENTER
            updateAlignmentButtons()
            updatePreview()
        }

        binding.btnAlignRight.setOnClickListener {
            currentAlignment = Paint.Align.RIGHT
            updateAlignmentButtons()
            updatePreview()
        }

        // Font size slider
        binding.sliderFontSize.addOnChangeListener { _, value, _ ->
            currentFontSize = value
            binding.tvFontSizeValue.text = "${value.toInt()}sp"
            updatePreview()
        }

        // Text change listeners
        binding.etQuoteText.addTextChangedListener {
            updatePreview()
        }

        binding.etAuthorName.addTextChangedListener {
            updatePreview()
        }
    }

    private fun updateAlignmentButtons() {
        val activeColor = resources.getColor(com.example.dilsequotes.R.color.purple_500, null)
        val inactiveColor = Color.parseColor("#3D3D3D")
        val activeTextColor = Color.WHITE
        val inactiveTextColor = Color.parseColor("#AAAAAA")

        // Left button
        binding.btnAlignLeft.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (currentAlignment == Paint.Align.LEFT) activeColor else inactiveColor
        )
        binding.btnAlignLeft.setTextColor(
            if (currentAlignment == Paint.Align.LEFT) activeTextColor else inactiveTextColor
        )

        // Center button
        binding.btnAlignCenter.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (currentAlignment == Paint.Align.CENTER) activeColor else inactiveColor
        )
        binding.btnAlignCenter.setTextColor(
            if (currentAlignment == Paint.Align.CENTER) activeTextColor else inactiveTextColor
        )

        // Right button
        binding.btnAlignRight.backgroundTintList = android.content.res.ColorStateList.valueOf(
            if (currentAlignment == Paint.Align.RIGHT) activeColor else inactiveColor
        )
        binding.btnAlignRight.setTextColor(
            if (currentAlignment == Paint.Align.RIGHT) activeTextColor else inactiveTextColor
        )
    }

    private fun validateInput(): Boolean {
        val text = binding.etQuoteText.text.toString().trim()

        if (text.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter quote text", Toast.LENGTH_SHORT).show()
            binding.etQuoteText.requestFocus()
            return false
        }

        if (text.length < 10) {
            Toast.makeText(requireContext(), "Quote is too short (min 10 characters)", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun updatePreview() {
        val quoteText = binding.etQuoteText.text.toString()
        val authorText = binding.etAuthorName.text.toString()

        // Update text
        binding.tvPreviewQuote.text = if (quoteText.isNotEmpty()) {
            "\"$quoteText\""
        } else {
            "\"Your quote will appear here...\""
        }

        binding.tvPreviewAuthor.text = if (authorText.isNotEmpty()) {
            "- $authorText"
        } else {
            "- Anonymous"
        }

        // Update font size
        binding.tvPreviewQuote.textSize = currentFontSize
        binding.tvPreviewAuthor.textSize = currentFontSize * 0.75f

        // Update alignment - FIXED: Use gravity instead of textAlignment
        val gravity = when (currentAlignment) {
            Paint.Align.LEFT -> Gravity.CENTER_VERTICAL or Gravity.START
            Paint.Align.CENTER -> Gravity.CENTER
            Paint.Align.RIGHT -> Gravity.CENTER_VERTICAL or Gravity.END
            else -> Gravity.CENTER
        }

        binding.tvPreviewQuote.gravity = gravity
        binding.tvPreviewAuthor.gravity = gravity

        // Update background
        if (currentBackgroundIndex >= 0) {
            val colors = backgroundColors[currentBackgroundIndex]
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                colors
            )
            gradient.cornerRadius = 20f * resources.displayMetrics.density
            binding.previewBackground.background = gradient

            // White text on colored background
            binding.tvPreviewQuote.setTextColor(Color.WHITE)
            binding.tvPreviewAuthor.setTextColor(Color.parseColor("#E0FFFFFF"))
            currentTextColor = Color.WHITE
        } else {
            // White background with dark text
            binding.previewBackground.setBackgroundColor(Color.WHITE)
            binding.tvPreviewQuote.setTextColor(Color.parseColor("#333333"))
            binding.tvPreviewAuthor.setTextColor(Color.parseColor("#666666"))
            currentTextColor = Color.parseColor("#333333")
        }
    }

    private fun createQuoteBitmap(): Bitmap {
        val width = 1080
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw background
        if (currentBackgroundIndex >= 0) {
            val colors = backgroundColors[currentBackgroundIndex]
            val paint = Paint().apply {
                shader = android.graphics.LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    colors[0], colors[1],
                    android.graphics.Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        // Text paints
        val textPaint = Paint().apply {
            color = if (currentBackgroundIndex >= 0) Color.WHITE else Color.parseColor("#333333")
            textSize = currentFontSize * 3.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = currentAlignment
        }

        val authorPaint = Paint().apply {
            color = if (currentBackgroundIndex >= 0) {
                Color.parseColor("#E0FFFFFF")
            } else {
                Color.parseColor("#666666")
            }
            textSize = currentFontSize * 2.4f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = currentAlignment
        }

        val quoteText = "\"${binding.etQuoteText.text}\""
        val authorText = "- ${binding.etAuthorName.text.toString().ifEmpty { "Anonymous" }}"

        // FIXED: Better X position calculation for alignment
        val xPos = when (currentAlignment) {
            Paint.Align.LEFT -> width * 0.15f      // Padding from left
            Paint.Align.CENTER -> width / 2f       // Center
            Paint.Align.RIGHT -> width * 0.85f     // Padding from right
            else -> width / 2f
        }

        // Calculate maxWidth based on alignment
        val maxWidth = when (currentAlignment) {
            Paint.Align.LEFT, Paint.Align.RIGHT -> width * 0.7f  // 70% width with padding
            Paint.Align.CENTER -> width * 0.8f                    // 80% width centered
            else -> width * 0.8f
        }

        drawMultiLineText(canvas, quoteText, xPos, height * 0.35f, textPaint, maxWidth)
        canvas.drawText(authorText, xPos, height * 0.72f, authorPaint)

        // Watermark
        val watermarkPaint = Paint().apply {
            color = if (currentBackgroundIndex >= 0) {
                Color.parseColor("#40FFFFFF")
            } else {
                Color.parseColor("#40000000")
            }
            textSize = 28f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("DilSe Quotes", width - 40f, height - 40f, watermarkPaint)

        return bitmap
    }

    private fun drawMultiLineText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: Paint,
        maxWidth: Float
    ) {
        val words = text.split(" ")
        var line = ""
        var yPos = y

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            val width = paint.measureText(testLine)

            if (width > maxWidth && line.isNotEmpty()) {
                canvas.drawText(line, x, yPos, paint)
                line = word
                yPos += paint.textSize * 1.3f
            } else {
                line = testLine
            }
        }

        if (line.isNotEmpty()) {
            canvas.drawText(line, x, yPos, paint)
        }
    }

    private fun saveQuoteAsImage() {
        try {
            val bitmap = createQuoteBitmap()
            val savedUri = saveBitmapToGallery(bitmap)

            if (savedUri != null) {
                Toast.makeText(requireContext(), "✓ Quote saved to gallery!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap): Uri? {
        val filename = "DilSeQuote_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requireContext().contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/DilSeQuotes")
                }

                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(imagesDir, "DilSeQuotes")
            if (!appDir.exists()) appDir.mkdirs()

            val image = File(appDir, filename)
            fos = FileOutputStream(image)
            imageUri = Uri.fromFile(image)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
        }

        return imageUri
    }

    private fun shareQuoteImage() {
        try {
            val bitmap = createQuoteBitmap()
            val cachePath = File(requireContext().cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "quote_share.jpg")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fileOutputStream)
            fileOutputStream.close()

            val contentUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                type = "image/jpeg"
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(android.content.Intent.createChooser(shareIntent, "Share Quote"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_QUOTE = "quote"

        fun newInstance(quote: Quote): QuoteEditorFragment {
            return QuoteEditorFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_QUOTE, quote)
                }
            }
        }
    }
}
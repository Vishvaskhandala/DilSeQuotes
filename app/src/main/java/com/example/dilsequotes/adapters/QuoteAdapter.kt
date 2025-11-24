package com.example.dilsequotes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dilsequotes.R
import com.example.dilsequotes.data.model.Quote
import com.example.dilsequotes.databinding.ItemQuoteBinding

class QuoteAdapter(
    private val onQuoteClick: (Quote) -> Unit,
    private val onFavoriteClick: (Quote) -> Unit
) : ListAdapter<Quote, QuoteAdapter.QuoteViewHolder>(QuoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val binding = ItemQuoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        val quote = getItem(position)
        holder.bind(quote)
    }

    inner class QuoteViewHolder(private val binding: ItemQuoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(quote: Quote) {
            // Set quote text
            binding.quoteText.text = quote.text

            // Set category-specific gradient background
            applyCategoryGradient(quote.category)

            // Set favorite icon
            updateFavoriteIcon(quote.isFavorite)

            // Handle click on the entire quote item
            binding.root.setOnClickListener {
                onQuoteClick(quote)
            }

            // Handle click on favorite button with animation
            binding.favoriteButton.setOnClickListener {
                // Provide immediate visual feedback
                updateFavoriteIcon(!quote.isFavorite)

                // Animate the button
                it.animate()
                    .scaleX(0.7f)
                    .scaleY(0.7f)
                    .setDuration(100)
                    .withEndAction {
                        it.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                    .start()

                onFavoriteClick(quote)
            }
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
                else -> R.drawable.quote_gradient_default
            }

            val drawable = ContextCompat.getDrawable(binding.root.context, gradientResId)
            binding.quoteBackground.background = drawable
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            binding.favoriteButton.setImageResource(
                if (isFavorite) R.drawable.ic_favorite
                else R.drawable.ic_favorite_border
            )
        }
    }
}

class QuoteDiffCallback : DiffUtil.ItemCallback<Quote>() {
    override fun areItemsTheSame(oldItem: Quote, newItem: Quote): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Quote, newItem: Quote): Boolean {
        return oldItem == newItem
    }
}
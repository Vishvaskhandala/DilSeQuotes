package com.example.dilsequotes.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
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
            // Set quote text and author
            binding.quoteText.text = quote.text
//            binding.quoteAuthor.text = "- ${quote.author}"

            // Set favorite icon based on favorite status
            binding.favoriteButton.setImageResource(
                if (quote.isFavorite) R.drawable.ic_favoritepng
                else R.drawable.ic_favorite_border
            )

            // Handle click on the entire quote item
            binding.root.setOnClickListener {
                onQuoteClick(quote)
            }

            // Handle click on favorite button
            binding.favoriteButton.setOnClickListener {
                onFavoriteClick(quote)
            }
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
package com.example.dilsequotes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dilsequotes.data.model.CategoryData
import com.example.dilsequotes.databinding.ItemCategoryRefinedBinding

class CategoryAdapter(
    private val onCategoryClick: (CategoryData) -> Unit,
    private val getLocalizedName: (CategoryData) -> String
) : ListAdapter<CategoryData, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryRefinedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding, onCategoryClick, getLocalizedName)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryViewHolder(
        private val binding: ItemCategoryRefinedBinding,
        private val onCategoryClick: (CategoryData) -> Unit,
        private val getLocalizedName: (CategoryData) -> String
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryData) {
            val localizedName = getLocalizedName(category)
            binding.tvCategoryName.text = "${category.emoji} $localizedName"
            binding.tvQuoteCount.text = "${category.quoteCount} Quotes" // This can also be a string resource

            binding.root.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }
}

class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryData>() {
    override fun areItemsTheSame(oldItem: CategoryData, newItem: CategoryData): Boolean {
        return oldItem.categoryId == newItem.categoryId
    }

    override fun areContentsTheSame(oldItem: CategoryData, newItem: CategoryData): Boolean {
        return oldItem == newItem
    }
}
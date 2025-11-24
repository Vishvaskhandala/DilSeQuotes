package com.example.dilsequotes.adapters

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dilsequotes.R

class BackgroundAdapter(
    private val backgrounds: List<IntArray>,
    private val onBackgroundSelected: (Int) -> Unit
) : RecyclerView.Adapter<BackgroundAdapter.BackgroundViewHolder>() {

    private var selectedPosition = 0

    inner class BackgroundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val backgroundView: View = itemView.findViewById(R.id.backgroundView)

        fun bind(colors: IntArray, position: Int) {
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                colors
            )
            gradientDrawable.cornerRadius = 16f
            backgroundView.background = gradientDrawable

            // Show selection indicator
            itemView.isSelected = position == selectedPosition

            itemView.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                onBackgroundSelected(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackgroundViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_background, parent, false)
        return BackgroundViewHolder(view)
    }

    override fun onBindViewHolder(holder: BackgroundViewHolder, position: Int) {
        holder.bind(backgrounds[position], position)
    }

    override fun getItemCount() = backgrounds.size
}
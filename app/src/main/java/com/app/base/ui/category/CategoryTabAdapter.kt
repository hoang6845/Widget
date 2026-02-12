package com.app.base.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brally.mobile.data.model.CategoryItem
import com.brally.mobile.utils.singleClick
import com.app.base.R
import com.app.base.databinding.ItemCategoryTabBinding

class CategoryTabAdapter(val onItemTapped: ((CategoryItem) -> Unit)? = null) :
    RecyclerView.Adapter<CategoryTabAdapter.ViewHolder>() {

    private val categories = mutableListOf<CategoryItem>()
    private var selectedPosition = 0

    fun setCategories(newCategories: MutableList<CategoryItem>) {
        categories.clear()
        categories.addAll(newCategories.distinct())
        notifyDataSetChanged()
    }

    fun selectCategory(position: Int) {
        if (position != selectedPosition && position < categories.size) {
            val oldPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryTabBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = categories.size

    inner class ViewHolder(private val binding: ItemCategoryTabBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryItem, isSelected: Boolean) {
            binding.tvCategory.text = category.value

            if (isSelected) {
                binding.cardCategory.setBackgroundResource(R.drawable.ic_category_selected)
            } else {
                binding.cardCategory.setBackgroundResource(R.drawable.ic_category_normal)
            }

            binding.root.singleClick {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val oldPosition = selectedPosition
                    selectedPosition = adapterPosition
                    notifyItemChanged(oldPosition)
                    notifyItemChanged(selectedPosition)
                    onItemTapped?.invoke(categories[adapterPosition])
                }
            }
        }
    }
}

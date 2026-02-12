package com.brally.mobile.base.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.brally.mobile.base.databinding.EmptyViewBinding
import com.brally.mobile.base.databinding.ItemNativeAdsListBinding
import com.brally.mobile.data.model.AdStyle
import com.brally.mobile.data.model.ItemListAds
import com.brally.mobile.utils.BindingReflex
import com.brally.mobile.utils.RecyclerViewType

abstract class BaseRecyclerViewAdsAdapter<T, VB : ViewBinding> :
    RecyclerView.Adapter<BaseViewHolder<VB>>() {

    var dataList: MutableList<ItemListAds<T>> = mutableListOf()


    var setOnClickItemListener: ((T, position: Int) -> Unit)? = null

    open fun addData(@IntRange(from = 0) position: Int, data: ItemListAds<T>) {
        this.dataList.add(position, data)
        notifyItemInserted(position)
        compatibilityDataSizeChanged(1)
    }

    open fun setAllData(data: List<ItemListAds<T>>) {
        this.dataList = data.toMutableList()
        notifyDataSetChanged()
    }

    protected fun compatibilityDataSizeChanged(size: Int) {
        if (this.dataList.size == size) {
            notifyDataSetChanged()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataList[position]) {
            is ItemListAds.DataItemListAds<T> -> RecyclerViewType.TYPE_DATA.value
            is ItemListAds.Ad -> {
                val ad = dataList[position] as ItemListAds.Ad
                if (ad.style == AdStyle.FULL_ITEMS) RecyclerViewType.TYPE_AD_FULL.value else RecyclerViewType.TYPE_AD_ONE.value
            }

            is ItemListAds.Placeholder -> RecyclerViewType.TYPE_PLACEHOLDER.value
            else -> RecyclerViewType.TYPE_PLACEHOLDER.value
        }
    }

    fun setOnClickItemRecyclerView(listener: (T, position: Int) -> Unit) {
        setOnClickItemListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        return when (viewType) {
            RecyclerViewType.TYPE_DATA.value -> {
                BaseViewHolder<VB>(reflexViewBinding(parent)).apply {
                    bindViewClickListener(this, viewType)
                }
            }

            RecyclerViewType.TYPE_AD_FULL.value -> {
                BaseViewHolder(
                    ItemNativeAdsListBinding.inflate(
                        getLayoutInflater(parent.context), parent, false
                    ) as VB
                )
            }

            RecyclerViewType.TYPE_AD_ONE.value -> {
                BaseViewHolder(
                    ItemNativeAdsListBinding.inflate(
                        getLayoutInflater(parent.context), parent, false
                    ) as VB
                ).apply {
                    bindViewClickListener(this, viewType)
                }
            }

            else -> BaseViewHolder(
                EmptyViewBinding.inflate(
                    getLayoutInflater(parent.context), parent, false
                ) as VB
            )
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        try {
            when (val item = dataList[position]) {
                is ItemListAds.DataItemListAds<T> -> {
                    holder.setIsRecyclable(true)
                    bindData(
                        holder.binding,
                        (dataList[position] as ItemListAds.DataItemListAds<T>).item,
                        position
                    )
                }

                is ItemListAds.Ad -> {
                    when (item.style) {
                        AdStyle.FULL_ITEMS -> {
                            holder.setIsRecyclable(false)
                            bindAdFull(holder.binding as ItemNativeAdsListBinding)
                        }

                        AdStyle.FULL_ONE_ITEM -> (holder as AdOneVH).bind(item)
                    }
                }

                is ItemListAds.Placeholder -> {
                    // No binding needed for placeholder
                }

                is ItemListAds.DataItemListAdsPlaceholder -> TODO()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected abstract fun bindData(binding: VB, item: T, position: Int)
    protected abstract fun bindAdFull(binding: ItemNativeAdsListBinding)

    override fun getItemCount(): Int = dataList.size


    abstract class BindingViewHolder<T, VB : ViewBinding>(
        protected val binding: VB
    ) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(item: T, position: Int)
    }

    class DataVH<T, VB : ViewBinding>(
        binding: VB, private val bindAction: (VB, T, Int) -> Unit
    ) : BindingViewHolder<T, VB>(binding) {
        override fun bind(item: T, position: Int) {
            bindAction(binding, item, position)
        }
    }

    class AdOneVH(private val binding: ItemNativeAdsListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(adItem: ItemListAds.Ad) {

        }
    }

    private fun reflexViewBinding(parent: ViewGroup): VB {
        return try {
            BindingReflex.reflexViewBinding(
                javaClass, getLayoutInflater(parent.context), parent, false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            EmptyViewBinding.inflate(getLayoutInflater(parent.context), parent, false) as VB
        }
    }

    private fun reflexAdsViewBinding(parent: ViewGroup): VB {
        return ItemNativeAdsListBinding.inflate(
            getLayoutInflater(parent.context), parent, false
        ) as VB
    }

    private fun getLayoutInflater(context: Context): LayoutInflater {
        return LayoutInflater.from(context)
    }

    open fun getItem(@IntRange(from = 0) position: Int): ItemListAds<T> {
        return dataList[position]
    }

    open fun bindViewClickListener(viewHolder: BaseViewHolder<VB>, _viewType: Int) {
        viewHolder.itemView.setOnClickListener { _ ->
            val position = viewHolder.bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }
            getItem(position).let { data ->
                setOnClickItemListener?.invoke((data as ItemListAds.DataItemListAds).item, position)
            }
        }
    }
}

abstract class BaseAdsViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: T, position: Int)
}

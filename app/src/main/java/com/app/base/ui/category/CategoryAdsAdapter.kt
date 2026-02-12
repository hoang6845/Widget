package com.app.base.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.base.databinding.ItemArtBinding
import com.brally.mobile.base.databinding.EmptyViewBinding
import com.brally.mobile.base.databinding.ItemNativeAdsListBinding
import com.brally.mobile.data.model.AdStyle
import com.brally.mobile.data.model.ArtItem
import com.brally.mobile.data.model.ItemListAds
import com.brally.mobile.service.ads.AdManager
import com.brally.mobile.service.ads.showNative
import com.brally.mobile.utils.singleClick

class CategoryAdsAdapter(
    private val fragment: CategoryFragment,
    private val onItemClick: ((ArtItem) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ART = 0
        const val TYPE_AD_FULL = 1
        private const val TYPE_AD_ONE = 2
        const val TYPE_DATA_PLACEHOLDER = 3
        const val TYPE_PLACEHOLDER = 4
    }

    private val items = mutableListOf<ItemListAds<ArtItem>>()

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is ItemListAds.DataItemListAds<ArtItem> -> TYPE_ART
            is ItemListAds.Ad -> when (item.style) {
                AdStyle.FULL_ITEMS -> TYPE_AD_FULL
                AdStyle.FULL_ONE_ITEM -> TYPE_AD_ONE
            }

            is ItemListAds.DataItemListAdsPlaceholder -> TYPE_DATA_PLACEHOLDER
            is ItemListAds.Placeholder -> TYPE_PLACEHOLDER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ART -> ItemDataVH(ItemArtBinding.inflate(inflater, parent, false))
            TYPE_AD_FULL -> AdFullVH(ItemNativeAdsListBinding.inflate(inflater, parent, false))
            TYPE_AD_ONE -> AdOneVH(ItemNativeAdsListBinding.inflate(inflater, parent, false))
            TYPE_DATA_PLACEHOLDER,
            TYPE_PLACEHOLDER -> PlaceholderVH(
                EmptyViewBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ItemListAds.DataItemListAds -> (holder as ItemDataVH).bindDataVH(item)
            is ItemListAds.Ad -> {
                when (item.style) {
                    AdStyle.FULL_ITEMS -> (holder as AdFullVH).bindAdFullVH(item)
                    AdStyle.FULL_ONE_ITEM -> (holder as AdOneVH).bindAdOneVH(item)
                }
            }

            is ItemListAds.DataItemListAdsPlaceholder -> {
            }

            is ItemListAds.Placeholder -> {
            }
        }
    }

    fun setData(newItems: List<ItemListAds<ArtItem>>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    open inner class ItemDataVH(private val binding: ItemArtBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindDataVH(data: ItemListAds.DataItemListAds<ArtItem>) {
            binding.root.singleClick {
                onItemClick?.invoke(data.item)
            }
        }
    }

    inner class AdFullVH(private val binding: ItemNativeAdsListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var isBound = false
        fun bindAdFullVH(item: ItemListAds.Ad) {
            if (!isBound) {
                fragment.showNative(AdManager.NATIVE_VIEW_LIST, binding.nativeAd)
                isBound = true
            }
        }
    }

    inner class AdOneVH(private val binding: ItemNativeAdsListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var isBound = false
        fun bindAdOneVH(item: ItemListAds.Ad) {
            if (!isBound) {
                fragment.showNative(AdManager.NATIVE_VIEW_LIST, binding.nativeAd)
                isBound = true
            }
        }
    }

    inner class PlaceholderVH(binding: EmptyViewBinding) :
        RecyclerView.ViewHolder(binding.root)
}

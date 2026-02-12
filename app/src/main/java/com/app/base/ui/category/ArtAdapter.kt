package com.app.base.ui.category

import androidx.fragment.app.Fragment
import com.app.base.databinding.ItemArtBinding
import com.brally.mobile.base.adapter.BaseRecyclerViewAdsAdapter
import com.brally.mobile.base.databinding.ItemNativeAdsListBinding
import com.brally.mobile.data.model.ArtItem
import com.brally.mobile.service.ads.AdManager
import com.brally.mobile.service.ads.showNative
import com.brally.mobile.utils.loadImageUrl

class ArtAdapter(
    private val fragment: Fragment,
) : BaseRecyclerViewAdsAdapter<ArtItem, ItemArtBinding>() {

    override fun bindData(binding: ItemArtBinding, item: ArtItem, position: Int) {
        binding.imvArt.loadImageUrl(item.imagePath)
    }

    override fun bindAdFull(binding: ItemNativeAdsListBinding) {
        fragment.showNative(AdManager.NATIVE_VIEW_LIST, binding.nativeAd)
    }
}

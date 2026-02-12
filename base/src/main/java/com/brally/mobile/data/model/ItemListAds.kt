package com.brally.mobile.data.model

import androidx.annotation.Keep

@Keep
sealed class ItemListAds<T> {
    data class DataItemListAds<T>(val item: T) : ItemListAds<T>()
    data class Ad(val style: AdStyle) : ItemListAds<Nothing>()
    class DataItemListAdsPlaceholder : ItemListAds<Nothing>()
    class Placeholder : ItemListAds<Nothing>()

}

enum class AdStyle {
    FULL_ITEMS, // quảng cáo chiếm 2 cột
    FULL_ONE_ITEM   // quảng cáo chiếm 1 cột
}

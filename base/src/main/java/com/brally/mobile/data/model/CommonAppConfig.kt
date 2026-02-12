package com.brally.mobile.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CommonAppConfig(
    @SerializedName("time_reload_ads_1")
    val timeReloadAds1: Long = 30000L,
    @SerializedName("time_reload_ads_2")
    val timeReloadAds2: Long = 90000L,
    @SerializedName("time_reload_ads_3")
    val timeReloadAds3: Long = 120000L,
    @SerializedName("limit_reload_ads_1")
    val limitReloadAds1: Int = 5,
    @SerializedName("limit_reload_ads_2")
    val limitReloadAds2: Int = 30,
    @SerializedName("limit_reload_ads_3")
    val limitReloadAds3: Int = 10,
)

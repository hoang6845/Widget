package com.brally.mobile.service.ads

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.brally.mobile.utils.callSafeFragment
import com.bralydn.ads.NativeAdView
import com.bralydn.ads.SmallDelayCallback
import com.bralydn.ads.ads.BralyDNAdvertisement
import com.bralydn.ads.ads.BralyDNRewardManagement
import com.bralydn.ads.ads.interf.BralyDNResultConsentForm
import com.bralydn.ads.ads.interf.BralyDNRewardItem
import com.bralydn.ads.data.ConfigManager
import com.language_onboard.utils.gone

object AdManager {
    private const val ADS_CONFIG_KEY = "ads_config_unit"
    private const val PLACEMENT_KEY = "ads_config_placement"
    private const val ITEM_KEY = "ads_config_item"

    const val OPEN = "open"
    const val SPLASH = "splash"
    const val NATIVE_VIEW_LIST = "native_view_list"
    const val BANNER = "banner"

    const val FULL_GETSTARTED = "full_getstarted"
    const val FULL_HOME = "full_home"
    const val FULL_TEMPLATE = "full_template"
    const val FULL_TEMPLATE_BACK = "full_template_back"
    const val FULL_DRAW = "full_draw"
    const val FULL_BACK = "full_back"
    const val FULL_DONE = "full_done"
    const val FULL_BACKUP = "full_backup"

    const val NATIVE_ONBOARD1 = "native_onboard1"
    const val NATIVE_ONBOARD2 = "native_onboard2"
    const val NATIVE_ONBOARD3 = "native_onboard3"
    const val NATIVE_ONBOARD_FULL = "native_onboard_full"
    const val NATIVE_LANGUAGE = "native_language"
    const val NATIVE_HOME = "native_home"
    const val NATIVE_TEMPLATE = "native_template"
    const val NATIVE_BG = "native_bg"
    const val NATIVE_DRAW = "native_draw"
    const val NATIVE_RESULT = "native_result"
    const val NATIVE_ARTWORK = "native_artwork"

    private fun fetchConfig(activity: Activity, callback: Runnable) {
        BralyDNAdvertisement.getInstance(activity)
            .fetchConfig(activity, ADS_CONFIG_KEY, PLACEMENT_KEY, ITEM_KEY, callback)
    }

    fun fetchConfigAndShowAd(
        fragment: Fragment,
        bannerView: ViewGroup? = null,
        callback: Runnable
    ) {
        val activity = fragment.activity ?: return
        val application = activity.application
        fetchConfig(activity) {
            BralyDNAdvertisement.getInstance(activity)
                .initAdsAndLoadSplashHasBanner(application, fragment, bannerView, callback)
        }
    }

    fun showBanner(activity: Activity, view: ViewGroup, key: String) {
        BralyDNAdvertisement.Companion.getInstance(activity).loadAndShowBanner(activity, view, key)
    }

    fun showNative(
        activity: Activity, view: NativeAdView, placementKey: String, fragment: Fragment
    ) {
        val context = fragment.context ?: return
        val advertisement = BralyDNAdvertisement.getInstance(context)
//        view.visibility = View.GONE
        if (advertisement.isNativeLoaded(placementKey)) {
            view.visibility = View.VISIBLE
            advertisement.showNativeAndReload(fragment, placementKey, view)
        } else {
            advertisement.loadNative(context, placementKey) {
                view.visibility = View.VISIBLE
                advertisement.showNativeAndReload(fragment, placementKey, view)
            }
        }
    }

    fun showReward(
        activity: Activity, key: String, onGrantReward: Runnable, onLoadFail: Runnable
    ) {
        val management = BralyDNAdvertisement.Companion.getInstance(activity)
        val rewardListener = object : BralyDNRewardManagement.RewardListener() {
            override fun onRewardAdded(rewardItem: BralyDNRewardItem?) {
                onGrantReward.run()
            }

            override fun onRewardFail(error: String?) {
                onLoadFail.run()
            }

        }
        if (management.isRewardLoaded(key)) {

            management.showRewardAndReload(activity, key, rewardListener)
        } else {
            management.loadReward(activity, key) {
                management.showRewardAndReload(activity, key, rewardListener)
            }
        }
    }


    fun showFull(activity: Activity, placementKey: String, runnable: Runnable) {
        BralyDNAdvertisement.Companion.getInstance(activity)
            .showInterstitialAndReload(activity, placementKey, SmallDelayCallback(runnable))
    }


    fun Fragment.showFull(key: String, runnable: Runnable) {
        activity?.let {
            showFull(it, key) {
                runnable.run()
            }
        }
    }

    fun checkToRegisterOpenAdsOnMain(activity: Activity) {
        BralyDNAdvertisement.Companion.getInstance(activity).checkToRegisterOpenAdsOnMain(activity)
    }


    fun onIronSourceResume(activity: Activity) {
        BralyDNAdvertisement.Companion.getInstance(activity).onIronSourceResume(activity)
    }

    fun resumeShowAdsSplash(activity: Activity) {
        BralyDNAdvertisement.Companion.getInstance(activity).resumeShowSplashAds(activity)
    }

    fun blockShowAdsSplash(activity: Activity) {
        BralyDNAdvertisement.Companion.getInstance(activity).blockShowAdsSplash()
    }

    fun unregisterOpenAds(activity: Activity) {
        BralyDNAdvertisement.Companion.getInstance(activity).unregisterOpenAds(activity)
    }

    fun onIronSourcePause(activity: Activity) {
        BralyDNAdvertisement.Companion.getInstance(activity).onIronSourcePause(activity)
    }

    fun showConsentForm(
        activity: Activity,
        testDeviceId: List<String>? = null,
        isForceShowConsentWhenRejectBefore: Boolean,
        BralyDNResultConsentForm: BralyDNResultConsentForm
    ) {
        BralyDNAdvertisement.Companion.getInstance(activity).showConsentForm(
            activity, testDeviceId, isForceShowConsentWhenRejectBefore, BralyDNResultConsentForm
        )
    }

    fun showPrivacyOptionForm(
        activity: Activity, testDeviceId: String? = null, resultConsentForm: BralyDNResultConsentForm
    ) {
        BralyDNAdvertisement.Companion.getInstance(activity).showPrivacyOptionForm(
            activity, testDeviceId, resultConsentForm
        )
    }

    fun isAdsPlacementEnable(activity: Activity, adKey: String): Boolean {
        return ConfigManager.getInstance(activity).adManagement?.adPlacements?.getOrElse(adKey) { null }?.placementConfig?.enable
            ?: false
    }

    fun isCmpRequired(activity: Activity): Boolean {
        return BralyDNAdvertisement.Companion.getInstance(activity).canShowCMPSetting(activity)
    }
}

fun Fragment.registerOpenAd() {
    activity?.let {
        AdManager.checkToRegisterOpenAdsOnMain(it)
    }
}

fun Fragment.unregisterOpenAd() {
    activity?.let {
        AdManager.unregisterOpenAds(it)
    }
}

fun Fragment.showPrivacyOptionForm(resultConsentForm: BralyDNResultConsentForm) {
    activity?.let {
        AdManager.showPrivacyOptionForm(activity = it, resultConsentForm = resultConsentForm)
    }
}

fun Fragment.showNative(key: String, nativeAdView: NativeAdView) {
    activity?.let {
        AdManager.showNative(it, nativeAdView, key, this)
    }
}

fun Fragment.showBanner(configKey: String, viewGroup: ViewGroup) {
    activity?.let {
        AdManager.showBanner(it, viewGroup, configKey)
    }
}

fun Fragment.showFull(key: String, runnable: Runnable) {
    activity?.let {
        AdManager.showFull(it, key) {
            callSafeFragment {
                runnable.run()
            }
        }
    }
}

fun Fragment.showReward(
    adKey: String, onGrantReward: Runnable, onLoadFail: Runnable
) {
    activity?.let {
        AdManager.showReward(it, adKey, onGrantReward, onLoadFail)
    }
}

fun Fragment.showBannerOrNative(
    bannerKey: String,
    nativeKey: String,
    bannerView: ViewGroup,
    nativeView: NativeAdView,
    collapsibleIcon: View? = null
) {
    val enableBanner = AdManager.isAdsPlacementEnable(requireActivity(), bannerKey)
    val enableNative = AdManager.isAdsPlacementEnable(requireActivity(), nativeKey)
    if (enableNative) {
        showNative(nativeKey, nativeView)
        bannerView.gone()
    } else if (enableBanner) {
        showBanner(bannerKey, bannerView)
        nativeView.gone()
        collapsibleIcon?.gone()
    } else {
        nativeView.gone()
        bannerView.gone()
        collapsibleIcon?.gone()
    }
}


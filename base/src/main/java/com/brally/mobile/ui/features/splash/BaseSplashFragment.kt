package com.brally.mobile.ui.features.splash

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.brally.mobile.base.R
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.viewmodel.BaseViewModel
import com.brally.mobile.service.ads.AdManager
import com.brally.mobile.service.event.SPLASH_SHOW
import com.brally.mobile.service.event.SPLASH_SHOW_0
import com.brally.mobile.service.event.subscribeEventNetwork
import com.brally.mobile.service.session.isFirst
import com.brally.mobile.service.session.isFirstSplash
import com.brally.mobile.service.session.setFirstSplash
import com.language_onboard.data.model.Language
import com.language_onboard.data.model.OnboardingConfig
import com.language_onboard.data.model.OnboardingItem
import com.language_onboard.data.model.OnboardingType
import com.language_onboard.utils.CommonAdManager
import com.language_onboard.utils.openOnboarding
import com.language_onboard.utils.tracking

abstract class BaseSplashFragment<VB : ViewBinding, VM : BaseViewModel> :
    BaseFragment<VB, VM>() {

    override fun initView() {
        if (isFirstSplash()) {
            setFirstSplash(false)
            tracking(SPLASH_SHOW_0)
        }
        tracking(SPLASH_SHOW)
        subscribeEventNetwork { online ->
            isInternetConnected(online && isAdded)
            if (online && isAdded) {
                fetchAndInitAds()
            }
        }
    }

    override fun initListener() {
    }


    override fun initData() {

    }

    private fun openOnboardingScreen() {
        val onboardingItems = listOf(
            OnboardingItem(
                type = OnboardingType.IMAGE.type,
                title = R.string.text_ob_1,
                description = R.string.description_ob_1,
                imageRes = R.drawable.img_onboard_1,
                nativeAdsLayoutRes = R.layout.layout_native_onboard,
                nativeKey = CommonAdManager.NATIVE_OB
            ), OnboardingItem(
                type = OnboardingType.IMAGE.type,
                title = R.string.text_ob_2,
                description = R.string.description_ob_2,
                imageRes = R.drawable.img_onboard_2,
                nativeAdsLayoutRes = R.layout.layout_native_onboard,
                nativeKey = CommonAdManager.NATIVE_OB
            ), OnboardingItem(
                type = OnboardingType.IMAGE.type,
                title = R.string.text_ob_2,
                description = R.string.description_ob_2,
                imageRes = R.drawable.img_onboard_3,
                nativeAdsLayoutRes = R.layout.layout_native_onboard,
                nativeKey = CommonAdManager.NATIVE_OB
            )
        )
        val languages = Language.entries

        val onboardingConfig = OnboardingConfig(
            languages = languages,
            onboardingItems = onboardingItems,
            languageNativeRes = R.layout.layout_native_onboard,
            nativeFullRes = R.layout.layout_native_ads_onboarding_full,
            isHideStatusBar = true,
            styleOnboard = 1
        )
        openOnboard()
    }

    private fun fetchAndInitAds() {
        activity?.let {
            AdManager.fetchConfigAndShowAd(
                fragment = this,
                bannerView = bannerView(),
                callback = {
                    onFetchConfigSuccess()
                    if (isFirst()) {
                        openOnboardingScreen()
                    } else {
                        openHome()
                    }
                })
        }
    }

    private fun openOnboard(isCustom: Boolean = true, onboardingConfig: OnboardingConfig? = null) {
        if (isCustom) {
            if (!isAdded || isDetached || activity == null) return
            findNavController().navigate(R.id.base_navigation)
        } else {
            onboardingConfig?.let { openOnboarding(onboardingConfig) }
        }
    }


    abstract fun bannerView(): ViewGroup?
    abstract fun openHome()
    abstract fun onFetchConfigSuccess()
    abstract fun isInternetConnected(isInternet: Boolean = true)
}

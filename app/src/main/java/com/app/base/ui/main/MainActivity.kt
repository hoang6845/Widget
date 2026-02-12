package com.app.base.ui.main

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.app.base.R
import com.app.base.utils.ContextUtils
import com.brally.mobile.base.activity.navigate
import com.brally.mobile.base.databinding.ActivityMainBinding
import com.brally.mobile.data.local.BaseAppSharePref
import com.brally.mobile.service.ads.AdManager
import com.brally.mobile.service.event.NO_INTERNET_DIALOG_SHOW
import com.brally.mobile.service.event.subscribeEventNetwork
import com.brally.mobile.service.sound.AppMusicPlayer
import com.brally.mobile.ui.features.main.BaseMainActivity
import com.brally.mobile.utils.openSettingNetWork
import com.brally.mobile.utils.singleClick
import com.bralydn.analytics.event.BralyDNTracking
import com.language_onboard.data.local.CommonAppSharePref
import com.language_onboard.data.model.Language
import com.language_onboard.utils.FeatureConfig
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.Locale

class MainActivity : BaseMainActivity<ActivityMainBinding, MainViewModel>() {
    private val commonSharePref by inject<CommonAppSharePref>()
    private val baseAppSharePref by inject<BaseAppSharePref>()

    override val graphResId: Int
        get() = R.navigation.app_nav

    private val listDestinationNoMusic = listOf(
        R.id.splashFragment,
        com.bralydn.ads.R.id.obParentFragment,
        com.bralydn.ads.R.id.languageFragment,
        com.bralydn.ads.R.id.onboardingFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun initView() {
        super.initView()
        navController?.addOnDestinationChangedListener { _, destination, _ ->
            if (listDestinationNoMusic.contains(destination.id).not()) {
                AppMusicPlayer.checkAndPlay()
            }
        }
        if (baseAppSharePref.pendingNavigation) {
            baseAppSharePref.pendingNavigation = false
            checkToNavigate()
        }
    }

    override fun initData() {
        super.initData()
        subscribeEventNetwork { online ->
            runOnUiThread {
                binding.layoutNoInternet.root.isVisible = online.not()
            }
            if (online.not()) {
                BralyDNTracking.logEvent(this, NO_INTERNET_DIALOG_SHOW)
            }
        }
        binding.layoutNoInternet.buttonSetting.singleClick { openSettingNetWork() }

        viewModel.isLoading.observe {
            binding.loading.loadingView.isVisible = it
        }
    }

    override fun onFlowFinished() {
        AdManager.showFull(this@MainActivity, AdManager.FULL_GETSTARTED) {
            lifecycleScope.launch {
                lifecycleScope.launchWhenStarted {
                    try {
                        navigate(R.id.homeFragment, isPopAll = true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            if (navController?.currentDestination == null) return
            if (listDestinationNoMusic.contains(navController?.currentDestination?.id).not()) {
                AppMusicPlayer.checkAndPlay()
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        AppMusicPlayer.stop()
        AppMusicPlayer.stopFxMusicPlayer()
    }

    override fun onDestroy() {
        AppMusicPlayer.releaseBackgroundMusic()
        AppMusicPlayer.releaseFxMusic()
        super.onDestroy()
    }

    override fun attachBaseContext(context: Context) {
        val locale = commonSharePref.languageCode ?: Language.ENGLISH.countryCode
        val localeUpdatedContext: ContextWrapper =
            ContextUtils.updateLocale(context, Locale(locale))
        super.attachBaseContext(localeUpdatedContext)
    }

    private fun checkToNavigate() {
        val config = FeatureConfig.getConfigEnableFeatures()
        if (config.onboarding == true) {
            navController?.navigate(com.brally.mobile.base.R.id.appOnboardFragment)
        } else {
            onFlowFinished()
        }
    }
}

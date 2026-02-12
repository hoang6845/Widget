package com.brally.mobile.ui.features.main

import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding
import com.brally.mobile.base.R
import com.brally.mobile.base.activity.BaseActivity
import com.brally.mobile.base.viewmodel.BaseViewModel
import com.brally.mobile.service.event.subscribeEventNetwork
import com.brally.mobile.utils.openSettingNetWork
import com.brally.mobile.utils.singleClick
import com.bralydn.ads.ads.BralyDNAdvertisement
import com.language_onboard.intf.OnboardingHandler

abstract class BaseMainActivity<VB : ViewBinding, VM : BaseViewModel> : BaseActivity<VB, VM>(),
    OnboardingHandler {
    abstract val graphResId: Int
    protected var navController: NavController? = null

    override fun initView() {
        (supportFragmentManager.findFragmentById(R.id.navHostFragment) as? NavHostFragment)?.let { navHostFragment ->
            val graph = navHostFragment.navController.navInflater.inflate(graphResId = graphResId)
            navHostFragment.navController.graph = graph
            navController = navHostFragment.navController
        }
    }

    override fun initListener() {
        subscribeEventNetwork { online ->
            runOnUiThread {
                findViewById<View>(R.id.layoutNoInternet).isVisible = online.not()
            }
        }
        findViewById<View>(R.id.buttonSetting).singleClick { openSettingNetWork() }
    }

    override fun initData() {

    }

    override fun onResume() {
        super.onResume()
        BralyDNAdvertisement.getInstance(this).onIronSourceResume(this)
    }

    override fun onPause() {
        super.onPause()
        BralyDNAdvertisement.getInstance(this).onIronSourcePause(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        BralyDNAdvertisement.getInstance(this).unregisterOpenAds(this)
    }
}
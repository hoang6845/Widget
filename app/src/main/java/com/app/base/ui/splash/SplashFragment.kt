package com.app.base.ui.splash

import android.view.ViewGroup
import com.app.base.R
import com.app.base.databinding.FragmentSplashBinding
import com.brally.mobile.base.activity.navigate
import com.brally.mobile.service.ads.AdManager
import com.brally.mobile.service.ads.showBanner
import com.brally.mobile.service.session.isFirst
import com.brally.mobile.ui.features.splash.BaseSplashFragment
import com.language_onboard.utils.gone
import com.language_onboard.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class SplashFragment : BaseSplashFragment<FragmentSplashBinding, SplashViewModel>() {
    private var job: Job? = null
    private var isPaused = false
    private var isInternetAvailable = true

    override fun bannerView(): ViewGroup? {
        return binding.banner
    }

    override fun openHome() {
        navigate(R.id.homeFragment, isPop = true)
    }

    override fun initView() {
        super.initView()
        showBanner(AdManager.BANNER, binding.banner)
        if (isFirst()) {
            binding.tvLoading.text =
                resources.getStringArray(com.brally.mobile.base.R.array.text_first_time).random()
            setupLoading()
        } else {
            binding.tvLoading.text = getString(com.brally.mobile.base.R.string.loading_text)
            binding.tvPercentLoading.gone()
            binding.progressBar.isIndeterminate = true
        }
        checkConsentShow()
    }

    override fun isInternetConnected(isInternet: Boolean) {
        isInternetAvailable = isInternet
        isPaused = !isInternet
    }

    override fun onFetchConfigSuccess() {
        job?.cancel()
        updateUI(100)
    }

    private fun setupLoading() {
        binding.apply {
            tvPercentLoading.visible()
            progressBar.max = 100
            progressBar.progress = 0
            updateUI(0)
        }
        startLoading()
    }

    private suspend fun waitIfPaused() {
        while (isPaused) {
            delay(500)
        }
    }

    private fun startLoading() {
        job = CoroutineScope(Dispatchers.Main).launch {
            delay(1000L)
            var progress = 0

            // Phase 1: 0 -> 90%
            while (progress < 80) {
                waitIfPaused()
                progress += 1
                updateUI(progress)
                delay(Random.nextLong(50, 200))
            }

            // Phase 2: 90 -> 100%
            while (progress < 90) {
                waitIfPaused()
                progress += 1
                updateUI(progress)
                delay(Random.nextLong(500, 1200))
            }
            while (progress < 99) {
                waitIfPaused()
                progress += 1
                updateUI(progress)
                delay(Random.nextLong(1000, 1500))
            }
        }
    }

    private fun updateUI(progress: Int) {
        if (!isAdded || view == null) return
        binding.progressBar.setProgressCompat(progress, true)
        binding.tvPercentLoading.text = "$progress%"
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    private fun checkConsentShow() {
        view?.viewTreeObserver?.addOnWindowFocusChangeListener { hasFocus ->
            isPaused = if (!hasFocus) {
                true
            } else {
                !isInternetAvailable
            }
        }
    }
}

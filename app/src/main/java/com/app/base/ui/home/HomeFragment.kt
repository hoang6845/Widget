package com.app.base.ui.home

import com.app.base.R
import com.app.base.component.dialog.ExitsAppDialog
import com.app.base.databinding.FragmentHomeBinding
import com.app.base.ui.category.CategoryFragmentArgs
import com.app.base.ui.main.MainViewModel
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.navigate
import com.brally.mobile.base.activity.onBackPressed
import com.brally.mobile.service.ads.AdManager
import com.brally.mobile.service.ads.showBanner
import com.brally.mobile.service.ads.showFull
import com.brally.mobile.service.ads.showNative
import com.brally.mobile.service.event.EXIT_DIALOG_SHOW
import com.brally.mobile.service.event.HOME_CLICK_BACK
import com.brally.mobile.utils.collectLatestFlow
import com.brally.mobile.utils.singleClick
import com.bralydn.analytics.notification.UpdateAppUtils
import com.language_onboard.utils.tracking
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {
    private val mainViewModel by activityViewModel<MainViewModel>()

    override fun initView() {
        adjustInsetsForBottomNavigation(binding.imvSetting)
        checkUpdate()
//        showBanner(AdManager.BANNER, binding.banner)
        showNative(AdManager.NATIVE_HOME, binding.nativeAdsView)
    }

    override fun initListener() {
        binding.imvMenuDraw.singleClick {
           showFull(AdManager.FULL_HOME) {
               navigate(R.id.wallpaperLiveFragment)
           }
        }

        binding.imvMenuCollection.singleClick {
            showFull(AdManager.FULL_HOME) {
                //            navigate(R.id.collectionFragment)
            }
        }

        binding.imvSetting.singleClick {
            navigate(R.id.settingFragment)
        }

        onBackPressed {
            tracking(HOME_CLICK_BACK)
            tracking(EXIT_DIALOG_SHOW)
            ExitsAppDialog(requireActivity()).also { dialog ->
                dialog.show(this) {
                    requireActivity().finish()
                }
            }
        }
    }

    override fun initData() {
        collectLatestFlow(viewModel.appCommonConfig) { data ->
            mainViewModel.setCommonAppConfig(data)
        }
    }

    private fun checkUpdate() {
        activity?.let {
            UpdateAppUtils.checkUpdateApp(it)
        }
    }
}

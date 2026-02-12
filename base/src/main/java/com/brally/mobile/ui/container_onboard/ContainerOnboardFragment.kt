package com.brally.mobile.ui.container_onboard

import android.content.Context
import androidx.navigation.fragment.findNavController
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.databinding.FragmentContainerOnboardBinding
import com.brally.mobile.utils.EmptyViewModel
import com.language_onboard.intf.OnboardingHandler
import com.language_onboard.ui.fragment.Screen
import com.language_onboard.utils.FeatureConfig

class ContainerOnboardFragment : BaseFragment<FragmentContainerOnboardBinding, EmptyViewModel>() {

    private var navigationHandler: OnboardingHandler? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnboardingHandler) {
            navigationHandler = context
        }
    }

    override fun initView() {
        if (startDestination() == Screen.LANGUAGE) {
            findNavController().navigate(com.brally.mobile.base.R.id.appLanguageFragment)
        } else if (startDestination() == Screen.ONBOARDING) {
            findNavController().navigate(com.brally.mobile.base.R.id.appOnboardFragment)
        } else {
            navigationHandler?.onFlowFinished()
        }
    }

    private fun startDestination(): Screen {
        val config = FeatureConfig.getConfigEnableFeatures()
        return if (config.language == true) Screen.LANGUAGE
        else if (config.onboarding == true) Screen.ONBOARDING
        else Screen.NO_CONFIG
    }

    override fun initListener() {
    }

    override fun initData() {
    }
}

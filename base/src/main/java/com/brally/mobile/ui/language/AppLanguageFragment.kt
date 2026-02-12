package com.brally.mobile.ui.language

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.databinding.FragmentAppLanguageBinding
import com.brally.mobile.data.model.AppLanguageSelector
import com.brally.mobile.service.ads.AdManager
import com.brally.mobile.service.ads.showNative
import com.brally.mobile.utils.collectLatestFlow
import com.brally.mobile.utils.singleClick
import com.bralydn.ads.R
import com.language_onboard.data.model.CommonEnableConfig
import com.language_onboard.intf.OnboardingHandler
import com.language_onboard.utils.gone
import com.language_onboard.utils.setLocale
import com.language_onboard.utils.visible

class AppLanguageFragment : BaseFragment<FragmentAppLanguageBinding, AppLanguageViewModel>() {

    private var navigationHandler: OnboardingHandler? = null
    private var commonEnableConfig: CommonEnableConfig? = null
    private val languageAdapter by lazy { AppLanguageAdapter() }
    var languageSelected: AppLanguageSelector? = null
    var isFirstSelect = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnboardingHandler) {
            navigationHandler = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        navigationHandler = null
    }

    override fun initView() {
        adjustInsetsForBottomNavigation(binding.viewTop)

        binding.tvTitle.text = getString(R.string.text_choose_language)
        binding.rvLanguage.apply {
            adapter = languageAdapter
            itemAnimator = DefaultItemAnimator()
        }
        binding.btnSelect.apply {
            isEnabled = false
            imageTintList = ContextCompat.getColorStateList(
                requireContext(),
                com.brally.mobile.base.R.color.gray_7d7d7d
            )
        }
    }

    override fun initListener() {
        languageAdapter.setOnClickItemRecyclerView { item, position ->
            if (isFirstSelect) {
                isFirstSelect = false
                binding.commonShimmerLayout.root.visible()
                binding.nativeAdView.postDelayed({
                    if (view != null && isAdded) {
                        binding.commonShimmerLayout.root.gone()
                        showNative(AdManager.NATIVE_LANGUAGE, binding.nativeAdView)
                    }
                }, 1500)
            } else {
                showNative(AdManager.NATIVE_LANGUAGE, binding.nativeAdView)
            }
            viewModel.setSelectLanguage(item)
            binding.btnSelect.apply {
                isEnabled = true
                imageTintList = ContextCompat.getColorStateList(
                    requireContext(),
                    com.brally.mobile.base.R.color.colorBlue
                )
            }
        }
        binding.btnSelect.singleClick {
            languageSelected?.language?.countryCode?.let { languageCode ->
                requireContext().setLocale(
                    languageCode
                )
            }
            viewModel.setLanguage()
            setupLanguageInNeed()
        }
    }

    override fun initData() {
        collectLatestFlow(viewModel.featureConfig) { data ->
            commonEnableConfig = data
        }
        collectLatestFlow(viewModel.languages) { data ->
            languageSelected = data.firstOrNull { it.isCheck }
            languageAdapter.setList(data.toMutableList())
        }
    }

    private fun setupLanguageInNeed() {
        languageSelected?.language?.countryCode?.let { languageCode ->
            requireContext().setLocale(languageCode)
            recreateApp()
            return
        }
    }

    private fun recreateApp() {
        activity?.recreate()
    }

    private fun checkToNavigate() {
        commonEnableConfig?.let {
            if (it.onboarding == true) {
                navigateToOnboarding()
            } else {
                navigateToHomeFragment()
            }
        }
    }

    private fun navigateToHomeFragment() {
        navigationHandler?.onFlowFinished()
    }

    fun navigateToOnboarding() {
        findNavController().navigate(com.brally.mobile.base.R.id.appOnboardFragment)
    }
}

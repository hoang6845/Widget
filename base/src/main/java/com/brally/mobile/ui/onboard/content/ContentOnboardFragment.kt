package com.brally.mobile.ui.onboard.content

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.databinding.FragmentContentAppOnboardingBinding
import com.brally.mobile.service.ads.AdManager
import com.brally.mobile.service.ads.showNative
import com.brally.mobile.ui.onboard.AppOnboardViewModel
import com.brally.mobile.ui.onboard.OnboardData
import com.brally.mobile.utils.EmptyViewModel
import com.brally.mobile.utils.singleClick
import com.bralydn.ads.R
import com.language_onboard.data.model.OnboardingConfig
import com.language_onboard.data.model.OnboardingItem
import com.language_onboard.ui.fragment.onboarding_v3.CommonOnboardingV3Fragment
import com.language_onboard.utils.CommonAdManager
import com.language_onboard.utils.gone
import com.language_onboard.utils.loadImageDrawable

class ContentOnboardFragment : BaseFragment<FragmentContentAppOnboardingBinding, EmptyViewModel>() {

    private var isLastPage = false

    override fun initView() {
        isLastPage = arguments?.getBoolean(ARGUMENT_IS_LAST_PAGE) ?: false
        val data = arguments?.getParcelable<OnboardingItem>(ARGUMENT_ONBOARD_DATA)
        val parent = parentFragment as? CommonOnboardingV3Fragment
        parent?.let { binding.dotIndicator.attachTo(it.getViewPager()) }
        data?.let {
            setUpOnboard(it)
        }
    }

    override fun initListener() {
        binding.btnNext.singleClick {
            val result =
                Bundle().apply { putString(OnboardData.ACTION_KEY, OnboardData.ACTION_NEXT) }
            parentFragmentManager.setFragmentResult(OnboardData.ONBOARD_ACTION_REQUEST, result)
        }
    }

    override fun initData() {
    }

    fun onPageSelect(position: Int) {
        binding.dotIndicator.selectedDotColor = ContextCompat.getColor(requireContext(), R.color.color_0d99ff)
        binding.itemShimmerView.shimmerContainer.postDelayed(
            {
                if (view != null && isAdded) {
                    binding.itemShimmerView.shimmerContainer.gone()
                    showNative(CommonAdManager.NATIVE_OB, binding.nativeAdView)
                }
            },
            1000
        )
    }

    private fun setUpOnboard(item: OnboardingItem) {
        binding.apply {
            item.title?.let { tvTitle.text = getString(it) }
            item.description?.let { tvDescription.text = getString(it) }
            item.imageRes?.let { imvOnboard.loadImageDrawable(it) }
            btnNext.text = run {
                if (isLastPage) getString(R.string.title_started)
                else getString(R.string.title_next)
            }
        }
    }

    companion object {
        private const val ARGUMENT_ONBOARD_DATA = "ARGUMENT_ONBOARD_DATA"
        private const val ARGUMENT_IS_LAST_PAGE = "ARGUMENT_IS_LAST_PAGE"

        fun newInstance(item: OnboardingItem, isLastPage: Boolean): ContentOnboardFragment {
            return ContentOnboardFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARGUMENT_IS_LAST_PAGE, isLastPage)
                    putParcelable(ARGUMENT_ONBOARD_DATA, item)
                }
            }
        }
    }
}

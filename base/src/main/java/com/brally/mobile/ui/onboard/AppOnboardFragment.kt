package com.brally.mobile.ui.onboard

import android.content.Context
import androidx.viewpager2.widget.ViewPager2
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.databinding.FragmentAppOnboardingBinding
import com.brally.mobile.ui.onboard.content.ContentOnboardFragment
import com.brally.mobile.utils.collectLatestFlow
import com.language_onboard.intf.OnboardingHandler

class AppOnboardFragment : BaseFragment<FragmentAppOnboardingBinding, AppOnboardViewModel>() {

    private var navigationHandler: OnboardingHandler? = null
    private lateinit var pageAdapter: OnboardPageAdapter
    private var pageCount = 0

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
        pageAdapter = OnboardPageAdapter(this)
        binding.viewPager.adapter = pageAdapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.viewPager.post {
                    val itemId = pageAdapter.getItemId(position)
                    val fragmentTag = "f$itemId"
                    val currentFragment = childFragmentManager.findFragmentByTag(fragmentTag) as? ContentOnboardFragment

                    currentFragment?.onPageSelect(position)
                }
            }
        })
    }

    override fun initListener() {
        childFragmentManager.setFragmentResultListener(
            OnboardData.ONBOARD_ACTION_REQUEST, this
        ) { _, bundle ->
            val action = bundle.getString(OnboardData.ACTION_KEY)
            if (action == OnboardData.ACTION_NEXT) {
                val nextItem = binding.viewPager.currentItem + 1
                if (nextItem < pageAdapter.itemCount) {
                    binding.viewPager.setCurrentItem(nextItem, true)
                } else {
                    navigationHandler?.onFlowFinished()
                }
            }
        }
    }

    override fun initData() {
        pageAdapter.setData(OnboardData.onboardingItems)
    }
}

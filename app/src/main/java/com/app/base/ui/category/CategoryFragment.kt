package com.app.base.ui.category

import androidx.navigation.fragment.navArgs
import com.app.base.databinding.FragmentCategoryBinding
import com.app.base.ui.main.MainViewModel
import com.app.base.utils.setUpAdapterGridAds
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.onBackPressed
import com.brally.mobile.base.activity.popBackStack
import com.brally.mobile.service.ads.AdManager
import com.brally.mobile.service.ads.AdManager.showFull
import com.brally.mobile.service.ads.showNative
import com.brally.mobile.utils.Common
import com.brally.mobile.utils.TimerHelper
import com.brally.mobile.utils.collectLatestFlow
import com.brally.mobile.utils.singleClick
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class CategoryFragment : BaseFragment<FragmentCategoryBinding, CategoryViewModel>() {

    private val args by navArgs<CategoryFragmentArgs>()
    private val mainViewModel by activityViewModel<MainViewModel>()
    private val artAdapter by lazy { ArtAdapter(this) }
    private val categoryTabAdapter by lazy {
        CategoryTabAdapter(onItemTapped = {
            reloadAds()
            binding.rcvArts.scrollToPosition(0)
            viewModel.getArtsByCategory(viewModel.getCategoryPosition(it))
        })
    }

    private val categoryId by lazy { args.categoryId }
    private var timerHelper = TimerHelper()

    override fun initView() {
        adjustInsetsForBottomNavigation(binding.top)
        timerHelper = TimerHelper(
            mainViewModel.getCommonAppConfig().timeReloadAds2,
            mainViewModel.getCommonAppConfig().limitReloadAds2
        )

        binding.rcvCategoriesTab.adapter = categoryTabAdapter
        binding.rcvArts.setUpAdapterGridAds(artAdapter, context, spanCount = 2)
        showNative(AdManager.NATIVE_TEMPLATE, binding.nativeAdsView)
    }

    override fun initListener() {
        artAdapter.setOnClickItemRecyclerView { artItem, _ ->
            //Todo : Handle art item click
        }

        binding.btnBack.singleClick {
            handleBack()
        }

        onBackPressed {
            handleBack()
        }
    }

    private fun handleBack() {
        showFull(AdManager.FULL_TEMPLATE_BACK) {
            popBackStack()
        }
    }

    override fun initData() {
        collectLatestFlow(viewModel.listArt) { arts ->
            artAdapter.setAllData(
                Common.insertDataAds(arts, interval = 4, spanCount = 2, maxAds = 2).toMutableList()
            )
        }

        collectLatestFlow(viewModel.categories) { categories ->
            // Set up the category tabs with the first category and the rest
            if (categories.isNotEmpty()) {
                categoryTabAdapter.setCategories(categories.toMutableList())
            }
        }

        collectLatestFlow(viewModel.categorySelected) { positionSelected ->
            categoryTabAdapter.selectCategory(positionSelected)
        }
    }

    private fun reloadAds() {
        if (timerHelper?.checkTimeReached() == true) {
            showNative(AdManager.NATIVE_TEMPLATE, binding.nativeAdsView)
        }
    }
}

package com.app.base.ui.category

import androidx.lifecycle.LifecycleOwner
import com.brally.mobile.base.viewmodel.BaseViewModel
import com.brally.mobile.data.model.ArtItem
import com.brally.mobile.data.model.CategoryItem
import com.brally.mobile.service.firebase.AppRemoteConfig
import com.language_onboard.data.local.CommonAppSharePref
import com.language_onboard.data.model.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CategoryViewModel : BaseViewModel() {
    private val appSharePref: CommonAppSharePref by lazy {
        CommonAppSharePref(context)
    }

    private val _listArt = MutableStateFlow<List<ArtItem>>(emptyList())
    val listArt = _listArt.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _categorySelected = MutableStateFlow<Int>(0)
    val categorySelected = _categorySelected.asStateFlow()

    private var allArt = mutableListOf<ArtItem>()

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        getCategories(_categorySelected.value)
    }

    private fun getListArt(position: Int) {
        launchHandler {
            flowOnIO {
                AppRemoteConfig.getListArt()
            }.subscribe { data ->
                allArt = data.toMutableList()
                getArtsByCategory(position)
            }
        }
    }

    fun getCategories(position: Int) {
        launchHandler {
            flowOnIO {
                AppRemoteConfig.getListCategory(
                    appSharePref.languageCode ?: Language.ENGLISH.countryCode
                )
            }.subscribe {
                _categories.value = it
                getListArt(position)
            }
        }
    }

    fun getArtsByCategory(position: Int) {
        val category = _categories.value[position]
        _categorySelected.value = _categories.value.indexOf(category)

        if (category.type.lowercase() == "all".lowercase()) {
            _listArt.value = allArt
        } else {
            val filteredTemplates =
                allArt.filter { it.category.lowercase() == category.type.lowercase() }
            _listArt.value = filteredTemplates.sortedBy { it.order }
        }
    }

    fun getCategoryPosition(categoryItem: CategoryItem): Int {
        return _categories.value.indexOf(categoryItem)
    }
}

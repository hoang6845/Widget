package com.brally.mobile.ui.language

import com.brally.mobile.base.viewmodel.BaseViewModel
import com.brally.mobile.data.local.BaseAppSharePref
import com.brally.mobile.data.model.AppLanguage
import com.brally.mobile.data.model.AppLanguageSelector
import com.brally.mobile.data.model.ArtItem
import com.brally.mobile.service.firebase.AppRemoteConfig
import com.bralydn.ads.ads.utility.CommonSharedPrefManager
import com.language_onboard.data.local.CommonAppSharePref
import com.language_onboard.data.model.CommonEnableConfig
import com.language_onboard.data.model.Language
import com.language_onboard.data.model.LanguageSelector
import com.language_onboard.utils.FeatureConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppLanguageViewModel: BaseViewModel() {

    private val appSharePref: CommonAppSharePref by lazy {
        CommonAppSharePref(context)
    }

    private val baseAppSharePref: BaseAppSharePref by lazy {
        BaseAppSharePref(context)
    }

    private val _languages = MutableStateFlow<List<AppLanguageSelector>>(emptyList())
    val languages = _languages.asStateFlow()

    private val _featureConfig = MutableStateFlow<CommonEnableConfig>(CommonEnableConfig())
    val featureConfig = _featureConfig.asStateFlow()


    init {
        fetchFeatureConfig()
        getLanguage()
    }
    fun fetchFeatureConfig() {
        launchHandler {
            flowOnIO {
                FeatureConfig.getConfigEnableFeatures()
            }.subscribe { data ->
                _featureConfig.value = data
            }
        }
    }
    fun getLanguage() {
        val languages = AppLanguage.getEntries().map { language ->
            AppLanguageSelector(language, isCheck = false)
        }
        _languages.value = languages
    }

    fun setSelectLanguage(item: AppLanguageSelector) {
        val newData = _languages.value.toMutableList().map {
            it.copy(isCheck =  it.language.countryCode == item.language.countryCode)
        }
        _languages.value = newData.toMutableList()
    }

    fun setLanguage() {
        val languageSelected =  _languages.value.firstOrNull { it.isCheck }
        appSharePref.languageCode = languageSelected?.language?.countryCode
        appSharePref.applyLanguage(languageSelected?.language?.countryCode ?: Language.ENGLISH.countryCode)
        baseAppSharePref.pendingNavigation = true
    }
}

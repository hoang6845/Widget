package com.app.base.ui.language

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.brally.mobile.base.viewmodel.BaseViewModel
import com.brally.mobile.data.model.LanguageApp
import com.brally.mobile.data.model.LanguageSelector
import com.language_onboard.data.local.CommonAppSharePref
import com.language_onboard.data.model.Language

class LanguageViewModel : BaseViewModel() {

    private val appSharePref: CommonAppSharePref by lazy {
        CommonAppSharePref(context)
    }

    val languageLiveData by lazy { MutableLiveData<List<LanguageSelector>>() }
    var mLanguageSelector: LanguageSelector? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        getLang()
    }

    private fun getLang() {
        launchHandler {
            flowOnIO {
                val languageCode = appSharePref.languageCode ?: Language.SPANISH.countryCode
                LanguageApp.entries.flatMapIndexed { _: Int, language: LanguageApp ->
                    arrayListOf<LanguageSelector>().also { list ->
                        val isCheck = languageCode == language.languageCode
                        val item = LanguageSelector(language, isCheck = isCheck)
                        list.add(item)
                        if (item.isCheck) {
                            mLanguageSelector = item
                        }
                    }
                }
            }.subscribe {
                languageLiveData.value = it
            }
        }
    }

    fun saveLang(onDone: () -> Unit) {
        launchHandler {
            flowOnIO {
                val languageCode =
                    mLanguageSelector?.language?.languageCode ?: Language.ENGLISH.countryCode
                appSharePref.languageCode = languageCode
                appSharePref.applyLanguage(languageCode)
            }.subscribe {
                onDone.invoke()
            }
        }
    }
}

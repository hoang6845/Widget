package com.app.base.ui.home

import androidx.lifecycle.viewModelScope
import com.brally.mobile.base.viewmodel.BaseViewModel
import com.brally.mobile.data.model.CommonAppConfig
import com.brally.mobile.service.firebase.AppRemoteConfig
import com.brally.mobile.service.firebase.SeverRemoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : BaseViewModel() {

    private val _appCommonConfig = MutableStateFlow<CommonAppConfig>(CommonAppConfig())
    val appCommonConfig = _appCommonConfig.asStateFlow()

    init {
        testToken()
        getAppCommonConfig()
    }

    fun testToken() {
        viewModelScope.launch {
            SeverRemoteConfig.testReadmeToken()
        }
    }

    fun getAppCommonConfig() {
        launchHandler {
            flowOnIO {
                AppRemoteConfig.getCommonAppConfig()
            }.subscribe { data ->
                _appCommonConfig.value = data
            }
        }
    }
}

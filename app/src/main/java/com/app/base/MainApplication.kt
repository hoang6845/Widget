package com.app.base

import androidx.core.content.ContextCompat
import com.app.base.ui.home.HomeFragment
import com.app.base.ui.splash.SplashFragment
import com.brally.mobile.base.application.BaseApplication
import com.brally.mobile.data.local.BaseAppSharePref
import com.brally.mobile.data.model.AppInfo
import com.brally.mobile.service.firebase.AppRemoteConfig
import com.brally.mobile.service.firebase.SeverRemoteConfig
import com.brally.mobile.utils.Constant
import com.bralydn.ads.ads.BralyDNMobileAds
import com.bralydn.ads.data.Configuration
import com.google.android.gms.ads.AdRequest
import com.language_onboard.data.local.CommonAppSharePref
import com.language_onboard.di.commonViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

class MainApplication : BaseApplication() {
    override val appInfo: AppInfo by lazy {
        AppInfo(
            appId = BuildConfig.APPLICATION_ID,
            icon = R.mipmap.ic_launcher,
            appName = ContextCompat.getContextForLanguage(this).getString(R.string.app_name),
            versionCode = BuildConfig.VERSION_CODE,
            versionName = BuildConfig.VERSION_NAME,
            isDebug = BuildConfig.DEBUG,
            privacy = Constant.POLICY,
            term = Constant.TERM,
            emailFeedback = Constant.EMAIL_FEEDBACK,
            appFlyer = Constant.KEY_APPSFLYER,
            rawGit = SeverRemoteConfig.DATA_BASE_URL_GITHUB,
            splashClass = SplashFragment::class.java,
            homeClass = HomeFragment::class.java,
            soundBGClass = arrayListOf(),
            appmetrica = null
        )
    }

    override fun onCreate() {
        super.onCreate()
        BralyDNMobileAds.apply {
            initConfig(
                this@MainApplication,
                Configuration(
                    defaultAds = Constant.DEFAULT_CONFIG_ADS,
                    isDebug = appInfo.isDebug,
                    testDeviceIds = listOf(),
                    appsflyerKey = Constant.KEY_APPSFLYER,
                    appmetricaNetwork = appInfo.appmetrica
                ).useDebugConfig(useDebug = isDebuggable())
            )
        }

        initKoin()
    }

    private fun isDebuggable() = BuildConfig.DEBUG

    private fun initKoin() {
        startKoin {
            androidLogger(if (appInfo.isDebug) Level.ERROR else Level.NONE)
            androidContext(this@MainApplication)
            modules(
                commonViewModelModule,
                module {
                    single { CommonAppSharePref(get()) }
                    single { BaseAppSharePref(get()) }
                }
            )
        }
    }
}

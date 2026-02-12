package com.brally.mobile.base.application

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.brally.mobile.data.model.AppInfo
import com.brally.mobile.utils.pref.LocalCache

abstract class BaseApplication : Application(), DefaultLifecycleObserver {

    abstract val appInfo: AppInfo

    companion object {
        lateinit var INSTANCE: BaseApplication
    }

    override fun onCreate() {
        super<Application>.onCreate()
        INSTANCE = this
        LocalCache.getInstance().initContext(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
}

fun getBaseApplication(): BaseApplication {
    return BaseApplication.INSTANCE
}

fun appInfo(): AppInfo {
    return getBaseApplication().appInfo
}

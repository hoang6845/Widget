package com.brally.mobile.data.local

import android.content.Context
import androidx.core.content.edit

class BaseAppSharePref(private val context: Context) {
    companion object {
        private const val PREF_PENDING_NAVIGATION = "pref_pending_navigation"
    }

    private val sharePref by lazy {
        context.getSharedPreferences("BaseAppSharedPref", Context.MODE_PRIVATE)
    }

    var pendingNavigation: Boolean
        get() = sharePref.getBoolean(PREF_PENDING_NAVIGATION, false)
        set(value) {
            sharePref.edit { putBoolean(PREF_PENDING_NAVIGATION, value) }
        }
}

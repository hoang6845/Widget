package com.app.base.local

import android.content.Context
import androidx.core.content.edit

class AppSharePref(private val context: Context) {
    companion object {
        private const val PREF_IS_RATE_APP = "isRateApp"
        private const val PREF_SESSION_SHOW_RATE_APP = "sessionShowRateApp"
        private const val PREF_SESSION_VALID_SHOW_RATE_APP = "sessionValidShowRateApp"
        private const val PREF_PENDING_NAVIGATION = "pref_pending_navigation"
    }

    private val sharePref by lazy {
        context.getSharedPreferences("TrackingSharePref", Context.MODE_PRIVATE)
    }

    var isRateApp: Boolean
        get() = sharePref.getBoolean(PREF_IS_RATE_APP, false)
        set(value) {
            sharePref.edit { putBoolean(PREF_IS_RATE_APP, value)}
        }

    var sessionShowRateApp: Int
        get() = sharePref.getInt(PREF_SESSION_SHOW_RATE_APP, -1)
        set(value) {
            sharePref.edit { putInt(PREF_SESSION_SHOW_RATE_APP, value)}
        }

    var sessionValidShowRateApp: Int
        get() = sharePref.getInt(PREF_SESSION_VALID_SHOW_RATE_APP, -1)
        set(value) {
            sharePref.edit { putInt(PREF_SESSION_VALID_SHOW_RATE_APP, value)}
        }

    var pendingNavigation: Boolean
        get() = sharePref.getBoolean(PREF_PENDING_NAVIGATION, false)
        set(value) {
            sharePref.edit { putBoolean(PREF_PENDING_NAVIGATION, value)}
        }
}

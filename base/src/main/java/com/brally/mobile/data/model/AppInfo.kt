package com.brally.mobile.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class AppInfo(
    @SerializedName("id")
    val appId: String,
    @SerializedName("app_name")
    val appName: String,
    @SerializedName("icon")
    val icon: Int,
    @SerializedName("version_code")
    val versionCode: Int,
    @SerializedName("version_name")
    val versionName: String,
    @SerializedName("is_debug")
    val isDebug: Boolean,
    @SerializedName("privacy")
    val privacy: String,
    @SerializedName("term")
    val term: String,
    @SerializedName("email_feedback")
    val emailFeedback: String,
    @SerializedName("appflyer")
    val appFlyer: String,
    @SerializedName("raw_git")
    val rawGit: String,
    @SerializedName("splash_class")
    val splashClass: Class<out Fragment>,
    @SerializedName("home_class")
    val homeClass: Class<out Fragment>,
    @SerializedName("sound_background_class")
    val soundBGClass: List<Class<out Fragment>> = arrayListOf(),
    @SerializedName("appmetrica")
    val appmetrica: String?,
) : Parcelable {

    fun isSplashClass(keyClass: String): Boolean {
        return keyClass == splashClass.name
    }

    fun isHomeClass(keyClass: String): Boolean {
        return keyClass == homeClass.name
    }
}

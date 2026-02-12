package com.brally.mobile.service.event

import android.content.Context
import androidx.fragment.app.Fragment
//import com.bralydn.analytics.event.BralyTracking

const val IMAGE_LOADING_FAIL = "image_loading_fail"
const val IMAGE_LOADING_SUCCESS = "image_loading_success"

//Rate
const val RATE_APP_SHOW = "rate_app_show"
const val RATE_APP_CLICK_LATER = "rate_app_click_later"
const val RATE_APP_CLICK_RATE = "rate_app_click_rate - "

const val SPLASH_SHOW = "splash_show"
const val SPLASH_SHOW_0 = "splash_show_0"

// Language
const val LANGUAGE_MODULE_SHOW = "language_module_show"
const val SELECTED_LANGUAGE_MD = "selected_language_md"

// Onboarding
const val SHOW_ONBOARDING_1_MD = "show_onboarding_1_md"
const val ONBOARD_CLICK_NEXT_MD_1 = "onboard_click_next_md_1"
const val SHOW_ONBOARDING_2_MD = "show_onboarding_2_md"
const val ONBOARDING_CLICK_CONTINUE_2 = "onboarding_click_continue_2"
const val SHOW_ONBOARDING_3_MD = "show_onboarding_3_md"
const val ONBOARD_CLICK_GET_STARTED_MD = "onboard_click_get_started_md"

// Home
const val HOME_SHOW = "home_show"
const val HOME_SHOW_0 = "home_show_0"
const val HOME_CLICK_BACK = "home_click_back"
const val EXIT_DIALOG_SHOW = "exit_dialog_show"
const val NO_INTERNET_DIALOG_SHOW = "no_internet_dialog_show"

// Setting
const val SETTING_SHOW = "setting_show"
const val SETTING_CLICK_BACK = "setting_click_back"
const val SETTING_CLICK_MUSIC_ON = "setting_click_music_on"
const val SETTING_CLICK_MUSIC_OFF = "setting_click_music_off"
const val SETTING_CLICK_SOUND_ON = "setting_click_sound_on"
const val SETTING_CLICK_SOUND_OFF = "setting_click_sound_off"

const val SETTING_CLICK_LANGUAGE = "setting_click_language"
const val SETTING_CLICK_SHARE = "setting_click_share"
const val SETTING_CLICK_RATE = "setting_click_rate"
const val SETTING_CLICK_FEEDBACK = "setting_click_feedback"
const val SETTING_CLICK_POLICY = "setting_click_policy"
const val SETTING_CLICK_CHECK_FOR_UPDATE = "setting_click_check_for_update"

// CMP
const val SETTING_CLICK_CMP = "setting_click_cmp"
const val SETTING_CLICK_TERM = "setting_click_term"
const val CMP_MESSAGE_SHOW = "cmp_message_show"
const val CONSENT_ALL_SUCCESS = "consent_all_success"
const val CONSENT_SOME_OPTIONS = "consent_some_options"
const val CONSENT_ALL_FAILED = "consent_all_failed"
const val PURPOSE_AMOUNT = "purpose_amout"
const val VENDORS_AMOUNT = "vendors_amout"


fun Fragment.tracking(
    eventName: String,
    params: HashMap<String, String>? = null,
    useAppMetrica: Boolean = false
) {
//    context?.let {
//        BralyTracking.logEvent(it, eventName, params, useAppMetrica)
//    }
}

fun tracking(
    context: Context,
    eventName: String,
    params: HashMap<String, String>? = null,
    useAppMetrica: Boolean = false
) {
    //BralyTracking.logEvent(context, eventName, params, useAppMetrica)
}

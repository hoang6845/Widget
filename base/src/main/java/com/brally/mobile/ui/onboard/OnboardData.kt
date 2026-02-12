package com.brally.mobile.ui.onboard

import com.brally.mobile.base.R
import com.language_onboard.data.model.OnboardingItem
import com.language_onboard.data.model.OnboardingType
import com.language_onboard.utils.CommonAdManager

object OnboardData {
    const val ONBOARD_ACTION_REQUEST = "onboard_action_request"
    const val ACTION_KEY = "action_key"
    const val ACTION_NEXT = "action_next"

    val onboardingItems = listOf(
        OnboardingItem(
            type = OnboardingType.IMAGE.type,
            title = R.string.text_ob_1,
            description = R.string.description_ob_1,
            imageRes = R.drawable.img_onboard_1,
            nativeKey = CommonAdManager.NATIVE_OB
        ), OnboardingItem(
            type = OnboardingType.IMAGE.type,
            title = R.string.text_ob_2,
            description = R.string.description_ob_2,
            imageRes = R.drawable.img_onboard_2,
            nativeKey = CommonAdManager.NATIVE_OB
        ), OnboardingItem(
            type = OnboardingType.IMAGE.type,
            title = R.string.text_ob_2,
            description = R.string.description_ob_2,
            imageRes = R.drawable.img_onboard_3,
            nativeKey = CommonAdManager.NATIVE_OB
        )
    )
}

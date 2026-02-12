package com.app.base.ui.setting

import androidx.core.view.isVisible
import com.app.base.R
import com.app.base.component.dialog.RateAppBottomDialog
import com.app.base.databinding.FragmentSettingBinding
import com.bg.common.Common
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.isCmpRequired
import com.brally.mobile.base.activity.navigate
import com.brally.mobile.base.activity.popBackStack
import com.brally.mobile.base.activity.showDialog
import com.brally.mobile.base.activity.showPrivacyOptionForm
import com.brally.mobile.base.application.appInfo
import com.brally.mobile.service.event.CMP_MESSAGE_SHOW
import com.brally.mobile.service.event.CONSENT_ALL_FAILED
import com.brally.mobile.service.event.CONSENT_ALL_SUCCESS
import com.brally.mobile.service.event.CONSENT_SOME_OPTIONS
import com.brally.mobile.service.event.PURPOSE_AMOUNT
import com.brally.mobile.service.event.SETTING_CLICK_BACK
import com.brally.mobile.service.event.SETTING_CLICK_CMP
import com.brally.mobile.service.event.SETTING_CLICK_FEEDBACK
import com.brally.mobile.service.event.SETTING_CLICK_LANGUAGE
import com.brally.mobile.service.event.SETTING_CLICK_MUSIC_OFF
import com.brally.mobile.service.event.SETTING_CLICK_MUSIC_ON
import com.brally.mobile.service.event.SETTING_CLICK_POLICY
import com.brally.mobile.service.event.SETTING_CLICK_RATE
import com.brally.mobile.service.event.SETTING_CLICK_SHARE
import com.brally.mobile.service.event.SETTING_CLICK_SOUND_OFF
import com.brally.mobile.service.event.SETTING_CLICK_SOUND_ON
import com.brally.mobile.service.event.SETTING_CLICK_TERM
import com.brally.mobile.service.event.SETTING_SHOW
import com.brally.mobile.service.event.VENDORS_AMOUNT
import com.brally.mobile.service.firebase.AppRemoteConfig
import com.brally.mobile.service.session.isMusic
import com.brally.mobile.service.session.isSound
import com.brally.mobile.service.session.isVibrate
import com.brally.mobile.service.session.saveMusic
import com.brally.mobile.service.session.saveSound
import com.brally.mobile.service.session.saveVibrate
import com.brally.mobile.service.sound.AppMusicPlayer
import com.brally.mobile.utils.singleClick
import com.bralydn.ads.ads.interf.BralyDNResultConsentForm
import com.google.android.ump.FormError
import com.language_onboard.utils.tracking

class SettingFragment : BaseFragment<FragmentSettingBinding, SettingViewModel>(),
    BralyDNResultConsentForm {

    override fun initView() {
        tracking(SETTING_SHOW)
        adjustInsetsForBottomNavigation(binding.clToolbar)
    }

    override fun initListener() {
        binding.imvBack.singleClick {
            tracking(SETTING_CLICK_BACK)
            popBackStack()
        }
        binding.llMusic.singleClick {
            saveMusic(!isMusic())
            val tag = if (isMusic()) SETTING_CLICK_MUSIC_ON else SETTING_CLICK_MUSIC_OFF
            tracking(tag)
            checkPlaySound()
            updateView()
        }
        binding.llSoundFx.singleClick {
            saveSound(!isSound())
            val tag = if (isSound()) SETTING_CLICK_SOUND_ON else SETTING_CLICK_SOUND_OFF
            tracking(tag)
            updateView()
        }
        binding.llVibrate.singleClick {
            saveVibrate(!isVibrate())
            updateView()
        }
        binding.llLanguage.singleClick {
            tracking(SETTING_CLICK_LANGUAGE)
            navigate(R.id.languageAppFragment)
        }
        binding.llShare.singleClick {
            tracking(SETTING_CLICK_SHARE)
            activity?.let {
                Common.shareApp(it, getString(R.string.app_name))
            }
        }
        binding.llRate.singleClick {
            tracking(SETTING_CLICK_RATE)
            showDialog(RateAppBottomDialog.getInstance(), RateAppBottomDialog::class.java.name)
        }
        binding.llFeedback.singleClick {
            tracking(SETTING_CLICK_FEEDBACK)
            activity?.let {
                Common.sendFeedback(it, getString(R.string.app_name), appInfo().emailFeedback, null)
            }
        }
        binding.llPolicy.singleClick {
            tracking(SETTING_CLICK_POLICY)
            activity?.let {
                Common.openWebView(it, appInfo().privacy)
            }
        }

        binding.llTerm.singleClick {
            tracking(SETTING_CLICK_TERM)
            activity?.let {
                Common.openWebView(it, appInfo().term)
            }
        }

        binding.llCmpSetting.isVisible = isCmpRequired() && AppRemoteConfig.getShowCmp()
        binding.llCmpSetting.singleClick {
            tracking(SETTING_CLICK_CMP)
            showPrivacyOptionForm(this)
        }
        updateView()
    }

    override fun initData() {
    }

    private fun checkPlaySound() {
        if (isMusic()) {
            AppMusicPlayer.playBackgroundMusic()
        } else {
            AppMusicPlayer.stop()
            AppMusicPlayer.releaseBackgroundMusic()
        }
    }

    private fun updateView() {
        binding.switchMusic.setImageResource(if (isMusic()) R.drawable.ic_switch_on else R.drawable.ic_switch_off)
        binding.switchSoundFx.setImageResource(if (isSound()) R.drawable.ic_switch_on else R.drawable.ic_switch_off)
        binding.switchVibrate.setImageResource(if (isVibrate()) R.drawable.ic_switch_on else R.drawable.ic_switch_off)
    }

    override fun onConsentCustom(consentPurpose: Int, consentVendor: Int) {
        tracking(
            CONSENT_SOME_OPTIONS,
            hashMapOf(
                PURPOSE_AMOUNT to consentPurpose.toString(),
                VENDORS_AMOUNT to consentVendor.toString()
            )
        )
        tracking(CONSENT_SOME_OPTIONS)
    }

    override fun onConsentFull(isConsentFullBefore: Boolean) {
        tracking(CONSENT_ALL_SUCCESS)
    }

    override fun onConsentReject() {
        tracking(CONSENT_ALL_FAILED)
    }

    override fun onConsentSkip() {
    }

    override fun onError(error: FormError) {
    }

    override fun onShowConsentForm() {
        tracking(CMP_MESSAGE_SHOW)
    }
}

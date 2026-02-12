package com.app.base.component.dialog

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.bg.common.Common
import com.brally.mobile.base.activity.BaseBottomSheetFragment
import com.brally.mobile.service.event.RATE_APP_CLICK_LATER
import com.brally.mobile.service.event.RATE_APP_CLICK_RATE
import com.brally.mobile.service.event.RATE_APP_SHOW
import com.brally.mobile.service.event.tracking
import com.brally.mobile.utils.singleClick
import com.language_onboard.utils.gone
import com.app.base.R
import com.app.base.databinding.DialogRateAppBinding
import com.app.base.local.AppSharePref
import kotlin.math.roundToInt

class RateAppBottomDialog : BaseBottomSheetFragment<DialogRateAppBinding>() {

    private val appSharePref by lazy {
        AppSharePref(requireContext())
    }

    override fun getVB(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogRateAppBinding {
        return DialogRateAppBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        tracking(RATE_APP_SHOW)
        binding.ratingBar.onRateListener = { star ->
            binding.buttonSubmit.apply {
                text = if (star >= 4) {
                    binding.etFeedbackLayout.gone()
                    binding.tvFeedbackSize.gone()
                    getString(R.string.rate_us)
                } else {
                    binding.etFeedbackLayout.isVisible = true
                    binding.tvFeedbackSize.apply {
                        isVisible = true
                        text = String.format(getString(R.string.max_size_feedback_text), 0)
                    }
                    getString(R.string.feed_back)
                }
                this.isVisible = true
                this.isEnabled = true
            }
        }

        binding.buttonSubmit.singleClick {
            appSharePref.isRateApp = true
            tracking(RATE_APP_CLICK_RATE + binding.ratingBar.rating.roundToInt(),)
            rateApp()
        }

        binding.etFeedback.doAfterTextChanged {
            binding.tvFeedbackSize.apply {
                isVisible = true
                text =
                    String.format(getString(R.string.max_size_feedback_text), it.toString().length)
            }
        }
    }

    private fun rateApp() {
        val star = binding.ratingBar.rating.toInt()
        if (star > 4) {
            activity?.let {
                Common.openAppInStore(it, it.packageName)
            }
        } else {
//            FeedbackSdk.sendFeedback(binding.etFeedback.text.toString())
        }
        dialog?.dismiss()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        appSharePref.sessionShowRateApp = 0
        tracking(RATE_APP_CLICK_LATER)
    }

    companion object {
        const val TAG = "RateAppBottomSheet"
        fun getInstance(): RateAppBottomDialog {
            val bottomSheet = RateAppBottomDialog()
            return bottomSheet
        }
    }
}

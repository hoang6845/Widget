package com.brally.mobile.utils

import android.app.Service
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.brally.mobile.base.application.getBaseApplication
import com.brally.mobile.service.session.isVibrate

object VibrateHelper {

    private const val DEFAULT_DURATION = 500L

    fun startVibrate() {
        if (isVibrate().not()) return
        val vibratorManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getBaseApplication().getSystemService(Service.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            getBaseApplication().getSystemService(Service.VIBRATOR_SERVICE) as? Vibrator
        }
        vibratorManager?.let { vibrator ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(DEFAULT_DURATION, 255))
            } else {
                vibrator.vibrate(DEFAULT_DURATION)
            }
        }
    }
}
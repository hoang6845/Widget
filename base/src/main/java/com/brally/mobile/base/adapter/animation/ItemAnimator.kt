package com.brally.mobile.base.adapter.animation

import android.animation.Animator
import android.view.View

interface ItemAnimator {
    fun animator(view: View): Animator
}

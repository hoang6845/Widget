package com.app.base.component.widget

import android.view.View
import android.view.ViewGroup
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import com.app.base.R

class RatingStar(viewGroup: ViewGroup) : View.OnClickListener {
    private val rateBars: MutableList<View> = mutableListOf()
    private var onRateListener: OnRateListener? = null
    private var rateIndex = -1

    private fun startAnimation(view: View, i: Int, i2: Int) {
        view.postDelayed({
            val scaleAnimation = ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f, 1, 0.5f, 1, 0.5f)
//            scaleAnimation.interpolator = CustomInterpolator()
            scaleAnimation.duration = i.toLong()
//            scaleAnimation.setAnimationListener(object : SimpleAnimationListener() {
//                override fun onAnimationEnd(animation: Animation) {
//                }
//
//                override fun onAnimationStart(animation: Animation) {
//                    view.isSelected = true
//                }
//            })
//            view.startAnimation(scaleAnimation)
        }, i2.toLong())
    }

    override fun onClick(view: View) {
        val id = view.id
        var i = 0
        while (i < rateBars.size) {
            rateBars[i].clearAnimation()
            rateBars[i].isSelected = i <= id
            i++
        }
        if (onRateListener != null) {
            onRateListener!!.onRate(id + 1)
        }
        rateIndex = id
    }

    fun setOnRateListener(onRateListenerVar: OnRateListener?) {
        onRateListener = onRateListenerVar
    }

    interface OnRateListener {
        fun onRate(star: Int)
    }

    init {
        var i = 0
        for (i2 in 0 until viewGroup.childCount) {
            val childAt = viewGroup.getChildAt(i2)
            if ("rate" == childAt.tag) {
                if (childAt is ImageView) {
                    childAt.setImageResource(R.drawable.bg_star_rate_app)
                }
                childAt.id = rateBars.size
                childAt.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                childAt.isSelected = false
                childAt.setOnClickListener(this)
                rateBars.add(childAt)
                startAnimation(childAt, 500, i)
                i += 100
            }
        }
    }
}

package com.app.base.component.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.app.base.R

class ApplicationRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes), RatingStar.OnRateListener {

    var onRateListener: ((Int) -> Unit)? = null

    var rating: Float = 5f

    init {
        inflate(context, R.layout.star_group, this)
        val ratingStar = RatingStar(findViewById(R.id.llCustomRating))
        ratingStar.setOnRateListener(this)
    }

    override fun onRate(star: Int) {
        rating = star.toFloat()
        onRateListener?.invoke(star)
    }
}

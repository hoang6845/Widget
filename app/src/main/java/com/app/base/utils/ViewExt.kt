package com.app.base.utils

import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brally.mobile.base.R
import com.brally.mobile.base.application.getBaseApplication
import com.brally.mobile.utils.RecyclerViewType

fun AppCompatTextView.setGradient(
    listColor: IntArray = intArrayOf(
        R.color.pink_fd8aff, R.color.blue_00b2ff
    )
) {
    post {
        try {
            this.text = this.text
            val width = paint.measureText(this.text.toString())
            val listParseColor = listColor.map {
                ContextCompat.getColor(getBaseApplication(), it)
            }.toIntArray()
            val shader = LinearGradient(
                0f, 0f, width, this.textSize, listParseColor, null, Shader.TileMode.CLAMP
            )

            this.paint.shader = shader
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}

fun RecyclerView.setUpAdapterGridAds(
    adapter: RecyclerView.Adapter<*>, context: Context?, spanCount: Int
) {
    val layoutManager = GridLayoutManager(context, spanCount).apply {
        spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    RecyclerViewType.TYPE_AD_FULL.value -> 2 // FULL_ITEMS
                    else -> 1 // Data item & FULL_ONE_ITEM & Placeholder
                }
            }
        }
    }

    this.layoutManager = layoutManager
    this.adapter = adapter

    setRecycledViewPool(RecyclerView.RecycledViewPool().apply {
        setMaxRecycledViews(RecyclerViewType.TYPE_AD_FULL.value, 2)
        setMaxRecycledViews(RecyclerViewType.TYPE_AD_ONE.value, 4)
    })
}

fun RecyclerView.setUpAdapterLinearAds(
    adapter: RecyclerView.Adapter<*>, context: Context?, orientation: Int = RecyclerView.VERTICAL
) {
    val layoutManager = LinearLayoutManager(context, orientation, false)

    this.layoutManager = layoutManager
    this.adapter = adapter

    setRecycledViewPool(RecyclerView.RecycledViewPool().apply {
        setMaxRecycledViews(RecyclerViewType.TYPE_AD_FULL.value, 2)
        setMaxRecycledViews(RecyclerViewType.TYPE_AD_ONE.value, 4)
    })
}

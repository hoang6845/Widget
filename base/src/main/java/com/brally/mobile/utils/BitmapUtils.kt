package com.brally.mobile.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import java.io.File

object BitmapUtils {

    fun getBitmapFromView(view: View): Bitmap? {
        try {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY)
            )
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            showHiddenViews(view)
            view.draw(canvas)
            restoreHiddenViews(view)
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun showHiddenViews(view: View) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                if (child.visibility == View.INVISIBLE) {
                    child.tag = true // Đánh dấu trạng thái ban đầu
                    child.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun restoreHiddenViews(view: View) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                if (child.tag == true) {
                    child.visibility = View.INVISIBLE
                    child.tag = null
                }
            }
        }
    }



    fun viewToImage(view: View): Bitmap? {
        try {
            val returnedBitmap =
                Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(returnedBitmap)
            view.layout(view.left, view.top, view.right, view.bottom)
            view.draw(canvas)
            return returnedBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getBitmapFromFile(file: File): Bitmap? {
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


}
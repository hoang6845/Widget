@file:Suppress("DEPRECATION")

package com.brally.mobile.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Insets
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.brally.mobile.base.application.appInfo
import com.brally.mobile.base.application.getBaseApplication
import com.brally.mobile.data.model.AdStyle
import com.brally.mobile.data.model.ItemListAds
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import java.io.File

enum class RecyclerViewType(val value: Int) {
    TYPE_DATA(0), TYPE_AD_FULL(1), TYPE_AD_ONE(2), TYPE_PLACEHOLDER(3);
}

fun MutableList<ItemListAds<Object>>.insertAdsWithRowAlign(
    original: List<ItemListAds<Object>>, interval: Int
): MutableList<ItemListAds<Object>> {
    val result = mutableListOf<ItemListAds<Object>>()
    var columnIndex = 0
    var count = 0

    for (item in original) {
        result.add(item)
        columnIndex = (columnIndex + 1) % 2
        count++

        if (count % interval == 0) {
            if (columnIndex != 0) {
//                    result.add(ItemListAds.Placeholder)
                columnIndex = 0
            }
            (ItemListAds.Ad(AdStyle.FULL_ITEMS) as? ItemListAds<Object>)?.let { result.add(it) }
            columnIndex = 0
        }
    }
    return result
}


object Common {
    fun dipToPx(context: Context, dipValue: Float): Float {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics)
    }

    fun pxToDip(context: Context, dipValue: Float): Float {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dipValue, metrics)
    }

    fun getScreenWidth(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets =
                windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    fun getImageFolderInAppDir(context: Context): String {
        val directory = File(context.filesDir.absolutePath + "/${appInfo().appName}/Pictures")
        if (!directory.exists()) directory.mkdirs()
        return directory.absolutePath
    }

    fun getItemMakeOverImageFile(context: Context, name: String): File {
        val dir = getImageFolderInAppDir(context)
        return File("$dir/${name}.png")
    }

    fun getMakeOverPeopleFolderInAppDir(context: Context): String {
        val directory = File(context.filesDir.absolutePath + "/${appInfo().appName}/MakePeople")
        if (!directory.exists()) directory.mkdirs()
        return directory.absolutePath
    }

    fun File.saveItemIntoGallery() {
        val dirPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path
        val destFile = File(dirPath, path.substringAfterLast("/"))
        inputStream().use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        scanFile(destFile)
    }

    private fun scanFile(file: File) {
        MediaScannerConnection.scanFile(
            getBaseApplication(), arrayOf(file.toString()), null, null
        )
    }

    fun saveBitmapToCache(context: Context, fileName: String, bitmap: Bitmap): String {
        val cachePath = context.cacheDir.absolutePath
        return saveBitmapToCache(fileName, bitmap, cachePath)
    }

    private fun saveBitmapToCache(fileName: String, bitmap: Bitmap, dirPath: String): String {
        val stickerFile = File(dirPath, fileName)
        if (stickerFile.exists()) return stickerFile.path
        stickerFile.writeBitmap(bitmap)
        scanFile(stickerFile)
        return stickerFile.path
    }

    private fun File.writeBitmap(bitmap: Bitmap) {
        parentFile?.mkdirs()
        createNewFile()
        outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            it.flush()
        }
    }

    private fun getRawUri(filename: String, activity: Activity): Uri {
        return Uri.parse((ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + activity.packageName + "/raw/" + filename))
    }

    fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var image = image
        return if (maxHeight > 0 && maxWidth > 0) {
            val width = image.getWidth()
            val height = image.getHeight()
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
            image
        } else {
            image
        }
    }

    fun getImageFromSDCard(filePath: String): Bitmap? {
        val file = File(filePath)
        if (file.exists()) {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, options)

            val scale = Math.max(options.outWidth / 512, options.outHeight / 512)
            options.inSampleSize = scale
            options.inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeFile(filePath, options)
            if (bitmap != null) {
                return getSquareBitmap(bitmap, 512)
            }
        }
        return null
    }

    fun getSquareBitmap(bitmap: Bitmap, targetSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val squareSize = Math.min(width, height)

        val xOffset = (width - squareSize) / 2
        val yOffset = (height - squareSize) / 2

        val croppedBitmap = Bitmap.createBitmap(bitmap, xOffset, yOffset, squareSize, squareSize)
        return Bitmap.createScaledBitmap(croppedBitmap, targetSize, targetSize, false)
    }

    fun Context.preloadSketchGlide(
        path: String, onResourceReady: ((Bitmap?) -> Unit)? = null
    ) {
        Glide.with(this).asBitmap().load(generateGlideUrl(path))
            .apply(RequestOptions.overrideOf(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    onResourceReady?.invoke(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    onResourceReady?.invoke(null)
                }
            })
    }

    fun EditText.showKeyboard() {
        try {
            this.context?.let {
                val manager =
                    it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                manager?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun Fragment.closeKeyboard() {
        try {
            this.context?.let {
                val manager =
                    it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(view?.windowToken, 0)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun <T>insertDataAds(
        original: List<T>, interval: Int, spanCount: Int = 2, maxAds: Int = 2
    ): List<ItemListAds<T>> {
        val result = mutableListOf<ItemListAds<T>>()
        var columnIndex = 0
        var count = 0
        var adInsertedCount = 0

        var data = original.map {
            ItemListAds.DataItemListAds(it)
        }

        for (item in data) {
            result.add(item)
            columnIndex = (columnIndex + (spanCount - 1)) % spanCount
            count++

            if (count % interval == 0 && adInsertedCount < maxAds) {
                if (columnIndex != 0) {
                    columnIndex = 0
                }
                result.add(ItemListAds.Ad(AdStyle.FULL_ITEMS) as ItemListAds<T>)
                adInsertedCount++
                columnIndex = 0
            }
        }
        return result
    }
}

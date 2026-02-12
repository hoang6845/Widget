package com.app.base.ui.main

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.brally.mobile.base.application.getBaseApplication
import com.brally.mobile.base.viewmodel.BaseViewModel
import com.brally.mobile.data.model.DrawResult
import com.brally.mobile.service.session.saveDrawCollection
import com.app.base.R
import com.brally.mobile.data.model.CommonAppConfig
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainViewModel : BaseViewModel() {

    private var commonAppConfig = CommonAppConfig()

    fun setCommonAppConfig(config: CommonAppConfig) {
        commonAppConfig = config
    }

    fun getCommonAppConfig(): CommonAppConfig {
        return commonAppConfig
    }

    fun saveBitmapToCached(bitmap: Bitmap): File? {
        try {
            showLoading()
            val imageFile =
                File(getBaseApplication().cacheDir, "draw_${System.currentTimeMillis()}.png")
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
            hideLoading(isNow = true)
            return imageFile
        } catch (e: IOException) {
            e.printStackTrace()
            hideLoading(isNow = true)
            return null
        }
    }

    fun getFileToShare(uri: Uri, onDone: (File) -> Unit) {
        try {
            val file = getFileWithUri(uri = uri)
            onDone.invoke(file)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun getFileWithUri(uri: Uri): File {
        val tempFile = File.createTempFile("tmp_", ".png", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return tempFile
    }

    fun handleFileImageUri(uri: Uri, onDone: (String?) -> Unit) {
        val currentFile = getFileWithUri(uri = uri)
        handleFileImage(currentFile, onDone)
    }

    fun handleFileImage(file: File, onDone: (String?) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ (API 29+) - Use MediaStore with RELATIVE_PATH
            launchHandler {
                val fileName = file.name
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val videoUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                var savedFilePath: String? = null
                videoUri?.let { uri ->
                    resolver.openOutputStream(uri).use { outputStream ->
                        file.inputStream().use { inputStream ->
                            outputStream?.let { inputStream.copyTo(it) }
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    savedFilePath = getFilePathFromUri(uri)
                    activityTopOrNull?.runOnUiThread {
                        onDone.invoke(savedFilePath)
                    }
                } ?: run {
                    Toast.makeText(
                        context,
                        context.getString(R.string.failed_to_save_image_to_gallery),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else
            // Special handling for Android 9 (API 28)
            launchHandler {
                val fileName = file.name
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    // DATA field is used for Android 9 instead of RELATIVE_PATH
                    put(MediaStore.Images.Media.DATA,
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + fileName)
                }

                val resolver = context.contentResolver
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                var savedFilePath: String? = null
                imageUri?.let { uri ->
                    resolver.openOutputStream(uri).use { outputStream ->
                        file.inputStream().use { inputStream ->
                            outputStream?.let { inputStream.copyTo(it) }
                        }
                    }
                    savedFilePath = getFilePathFromUri(uri)
                    activityTopOrNull?.runOnUiThread {
                        onDone.invoke(savedFilePath)
                    }
                    // Notify the system about the new image
                    notifyFile(File(savedFilePath ?: ""))
                } ?: run {
                    Toast.makeText(
                        context,
                        context.getString(R.string.failed_to_save_image_to_gallery),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    return cursor.getString(columnIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun notifyFile(file: File) {
        try {
            val context = getBaseApplication()
            @Suppress("DEPRECATION")
            context.sendBroadcast(
                Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    .setData(Uri.fromFile(file))
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveDrawResult(drawResult: DrawResult) {
        launchHandler {
            flowOnIO {
                saveDrawCollection(drawResult.copy(id = System.currentTimeMillis().toInt()))
            }.subscribe(onLoading = false) {}
        }
    }
}

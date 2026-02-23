package com.app.base.utils

import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder

/**
 * SIMPLE & SMOOTH Live Wallpaper Service
 *
 * Chỉ phát video đã render sẵn (video + animation đã được compose)
 * → Không cần render realtime → Mượt mà, tiết kiệm pin
 */
class LiveWallpaperService : WallpaperService() {

    companion object {
        private const val TAG = "LiveWallpaper"
    }

    override fun onCreateEngine(): Engine {
        Log.d(TAG, "Creating engine")
        return WallpaperEngine()
    }

    inner class WallpaperEngine : Engine() {

        private var mediaPlayer: MediaPlayer? = null

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            Log.d(TAG, "Engine created")
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            Log.d(TAG, "Surface created")
            initMediaPlayer(holder)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            Log.d(TAG, "Surface changed: ${width}x${height}")
        }

        private fun initMediaPlayer(holder: SurfaceHolder) {
            val videoUri = WallpaperDataStore.videoUri
            if (videoUri == null) {
                Log.e(TAG, "❌ Video URI is null!")
                return
            }

            Log.d(TAG, "Loading video: $videoUri")

            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(applicationContext, videoUri)
                    setSurface(holder.surface)
                    isLooping = true
                    setVolume(0f, 0f)

                    setOnPreparedListener { mp ->
                        Log.d(TAG, "✅ Video prepared (${mp.duration}ms)")
                        mp.start()
                        Log.d(TAG, "✅ Video started")
                    }

                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "❌ MediaPlayer error: what=$what, extra=$extra")
                        false
                    }

                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to init MediaPlayer", e)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            Log.d(TAG, "Surface destroyed")
            mediaPlayer?.release()
            mediaPlayer = null
            super.onSurfaceDestroyed(holder)
        }

        override fun onDestroy() {
            Log.d(TAG, "Engine destroyed")
            mediaPlayer?.release()
            mediaPlayer = null
            super.onDestroy()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            Log.d(TAG, if (visible) "Visible" else "Hidden")
            if (visible) {
                mediaPlayer?.start()
            } else {
                mediaPlayer?.pause()
            }
        }
    }
}

/**
 * Singleton để truyền video URI từ Activity
 */
object WallpaperDataStore {
    var videoUri: Uri? = null
}
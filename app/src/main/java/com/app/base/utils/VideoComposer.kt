package com.app.base.utils

import android.content.Context
import android.graphics.*
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import com.app.base.model.AnimationData
import com.app.base.model.PhotoSlot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.min

/**
 * VideoComposer - Render video + animation thành video mới
 * 
 * Cách dùng:
 * 1. User xong preview, bấm save
 * 2. Gọi VideoComposer.compose() → Tạo video mới
 * 3. LiveWallpaper chỉ cần phát video mới (đơn giản, mượt mà)
 */
object VideoComposer {

    private const val TAG = "VideoComposer"
    
    data class ComposeResult(
        val outputPath: String,
        val success: Boolean,
        val error: String? = null
    )

    /**
     * Compose video + animation thành video mới
     * 
     * @param context Android context
     * @param sourceVideoUri URI video gốc
     * @param animData Animation data
     * @param photoSlots Danh sách ảnh user đã import
     * @param outputFile File output (.mp4)
     * @param onProgress Callback tiến độ (0-100)
     */
    suspend fun compose(
        context: Context,
        sourceVideoUri: Uri,
        animData: AnimationData,
        photoSlots: List<PhotoSlot>,
        outputFile: File,
        onProgress: ((Int) -> Unit)? = null
    ): ComposeResult = withContext(Dispatchers.IO) {
        
        Log.d(TAG, "🎬 Starting video composition...")
        Log.d(TAG, "   Source: $sourceVideoUri")
        Log.d(TAG, "   Output: ${outputFile.absolutePath}")
        
        var retriever: MediaMetadataRetriever? = null
        var encoder: MediaCodec? = null
        var muxer: MediaMuxer? = null
        
        try {
            // 1. Setup video retriever
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, sourceVideoUri)
            
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val videoDuration = durationStr?.toLongOrNull() ?: 3000L
            
            val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val width = widthStr?.toIntOrNull() ?: 1080
            val height = heightStr?.toIntOrNull() ?: 1920
            
            Log.d(TAG, "   Video: ${width}x${height}, ${videoDuration}ms")
            
            // 2. Cache cropped bitmaps
            val croppedCache = buildCroppedCache(animData, photoSlots)
            Log.d(TAG, "   Cached ${croppedCache.size} images")
            
            // 3. Setup encoder
            val mime = MediaFormat.MIMETYPE_VIDEO_AVC
            val format = MediaFormat.createVideoFormat(mime, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, 4_000_000) // 4 Mbps
                setInteger(MediaFormat.KEY_FRAME_RATE, 30)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }
            
            encoder = MediaCodec.createEncoderByType(mime)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = encoder.createInputSurface()
            encoder.start()
            
            // 4. Setup muxer
            if (outputFile.exists()) outputFile.delete()
            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            
            // 5. Render từng frame
            val fps = 30
            val frameDurationMs = 1000L / fps
            val totalFrames = (videoDuration / frameDurationMs).toInt()
            
            Log.d(TAG, "   Rendering $totalFrames frames...")
            
            var trackIndex = -1
            val bufferInfo = MediaCodec.BufferInfo()
            
            for (frameIdx in 0 until totalFrames) {
                val timeMs = frameIdx * frameDurationMs
                val canvas = inputSurface.lockCanvas(null)

                // 5a. Render frame
                renderCompositeFrame(
                    canvas = canvas,
                    retriever = retriever,
                    animData = animData,
                    photoSlots = photoSlots,
                    croppedCache = croppedCache,
                    timeMs = timeMs,
                    width = width,
                    height = height
                )
                
                inputSurface.unlockCanvasAndPost(canvas)
                
                // 5b. Drain encoder
                var outputDone = false
                while (!outputDone) {
                    val outputBufferId = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
                    when {
                        outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            val outputFormat = encoder.outputFormat
                            trackIndex = muxer.addTrack(outputFormat)
                            muxer.start()
                            Log.d(TAG, "   Muxer started, track=$trackIndex")
                        }
                        outputBufferId >= 0 -> {
                            val outputBuffer = encoder.getOutputBuffer(outputBufferId)
                            if (outputBuffer != null && bufferInfo.size > 0) {
                                muxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                            }
                            encoder.releaseOutputBuffer(outputBufferId, false)
                            outputDone = (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0
                        }
                        else -> outputDone = true
                    }
                }
                
                // 5c. Progress callback
                val progress = ((frameIdx + 1) * 100 / totalFrames)
                onProgress?.invoke(progress)
                
                if (frameIdx % 30 == 0) {
                    Log.d(TAG, "   Progress: $progress% (frame $frameIdx/$totalFrames)")
                }
            }
            
            // 6. Signal end of stream
            encoder.signalEndOfInputStream()
            
            // 7. Cleanup
            encoder.stop()
            encoder.release()
            muxer.stop()
            muxer.release()
            retriever.release()
            inputSurface.release()
            
            Log.d(TAG, "✅ Composition complete: ${outputFile.absolutePath}")
            
            ComposeResult(
                outputPath = outputFile.absolutePath,
                success = true
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Composition failed", e)
            
            encoder?.stop()
            encoder?.release()
            muxer?.stop()
            muxer?.release()
            retriever?.release()
            
            ComposeResult(
                outputPath = outputFile.absolutePath,
                success = false,
                error = e.message
            )
        }
    }
    
    /**
     * Build cropped bitmap cache (giống code cũ)
     */
    private fun buildCroppedCache(
        animData: AnimationData,
        photoSlots: List<PhotoSlot>
    ): Map<String, Bitmap> {
        val designW = animData.design.width.toFloat()
        val designH = animData.design.height.toFloat()
        val cache = mutableMapOf<String, Bitmap>()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        
        for (slot in photoSlots) {
            val bmp = slot.bitmap ?: continue
            val photo = animData.photos[slot.slotKey] ?: continue
            val rect = photo.rect
            val fw = (rect.w * designW).toInt()
            val fh = (rect.h * designH).toInt()
            if (fw <= 0 || fh <= 0) continue
            
            val cropped = Bitmap.createBitmap(fw, fh, Bitmap.Config.ARGB_8888)
            val c = Canvas(cropped)
            val m = Matrix()
            m.postTranslate(-bmp.width / 2f, -bmp.height / 2f)
            m.postScale(slot.userScale, slot.userScale)
            m.postTranslate(fw / 2f + slot.userOffsetX, fh / 2f + slot.userOffsetY)
            c.drawBitmap(bmp, m, paint)
            cache[slot.slotKey] = cropped
        }
        
        return cache
    }
    
    /**
     * Render một frame: video background + animation overlay
     */
    private fun renderCompositeFrame(
        canvas: Canvas,
        retriever: MediaMetadataRetriever,
        animData: AnimationData,
        photoSlots: List<PhotoSlot>,
        croppedCache: Map<String, Bitmap>,
        timeMs: Long,
        width: Int,
        height: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        
        // 1. Vẽ video frame
        val videoFrame = retriever.getFrameAtTime(
            timeMs * 1000,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
        )
        
        if (videoFrame != null) {
            val srcRect = Rect(0, 0, videoFrame.width, videoFrame.height)
            val dstRect = Rect(0, 0, width, height)
            canvas.drawBitmap(videoFrame, srcRect, dstRect, paint)
            videoFrame.recycle()
        } else {
            canvas.drawColor(Color.BLACK)
        }
        
        // 2. Vẽ animation overlay
        val designW = animData.design.width.toFloat()
        val designH = animData.design.height.toFloat()
        
        val scale = min(width.toFloat() / designW, height.toFloat() / designH)
        val offsetX = (width - designW * scale) * 0.5f
        val offsetY = (height - designH * scale) * 0.5f
        
        for (slot in photoSlots) {
            val photo = animData.photos[slot.slotKey] ?: continue
            val croppedBmp = croppedCache[slot.slotKey] ?: continue
            
            val state = KeyFrameInterpolator.interpolate(photo.keys, timeMs)
            
            val rect = photo.rect
            val frameW = (rect.w * designW).toFloat()
            val frameH = (rect.h * designH).toFloat()
            
            val animCenterX = (state.x * designW).toFloat()
            val animCenterY = (state.y * designH).toFloat()
            
            val viewCenterX = offsetX + animCenterX * scale
            val viewCenterY = offsetY + animCenterY * scale
            
            val viewFrameW = frameW * scale * state.scale.toFloat()
            val viewFrameH = frameH * scale * state.scale.toFloat()
            
            canvas.save()
            canvas.translate(viewCenterX, viewCenterY)
            canvas.rotate(state.rotation.toFloat())
            
            val left = -viewFrameW / 2f
            val top = -viewFrameH / 2f
            val right = viewFrameW / 2f
            val bottom = viewFrameH / 2f
            
            canvas.clipRect(left, top, right, bottom)
            canvas.drawBitmap(croppedBmp, null, RectF(left, top, right, bottom), paint)
            canvas.restore()
        }
    }
}
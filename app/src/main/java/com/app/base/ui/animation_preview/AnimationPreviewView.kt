package com.app.base.ui.animation_preview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.app.base.model.AnimationData
import com.app.base.model.Photo
import com.app.base.model.PhotoSlot
import com.app.base.utils.KeyFrameInterpolator
import kotlin.math.min

/**
 * View overlay vẽ các ảnh user đã import, chuyển động theo keyframe
 * đồng bộ với thời gian của video.
 *
 * Cách dùng:
 *   1. Gọi setAnimationData() để cấu hình dữ liệu
 *   2. Gọi setPhotoSlots() để cập nhật ảnh user
 *   3. Gọi updateTime(ms) mỗi frame (từ VideoPlayer onProgress)
 */
class AnimationPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var animData: AnimationData? = null
    private var slots: List<PhotoSlot> = emptyList()
    private var currentTimeMs: Long = 0L

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val debugPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    fun setAnimationData(data: AnimationData) {
        animData = data
        invalidate()
    }

    fun setPhotoSlots(photoSlots: List<PhotoSlot>) {
        slots = photoSlots
        invalidate()
    }

    /**
     * Gọi từ VideoPlayer listener để đồng bộ animation với video
     */
    fun updateTime(timeMs: Long) {
        currentTimeMs = timeMs
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val data = animData ?: return

        val vw = width.toFloat()
        val vh = height.toFloat()
        val designW = data.design.width.toFloat()
        val designH = data.design.height.toFloat()

        val scale = min(vw / designW, vh / designH)
        val offsetX = (vw - designW * scale) * 0.5f
        val offsetY = (vh - designH * scale) * 0.5f

        for (slot in slots) {
            val photo = data.photos[slot.slotKey] ?: continue
            val bmp = slot.bitmap ?: continue
            val croppedBmp = getCroppedBitmap(bmp, photo, slot, designW, designH) ?: continue

            // Tính frame state tại currentTimeMs
            val state = KeyFrameInterpolator.interpolate(photo.keys, currentTimeMs)

            // Rect của slot trong design space
            val rect = photo.rect
            val frameW = (rect.w * designW).toFloat()
            val frameH = (rect.h * designH).toFloat()

            // Vị trí mới theo keyframe (x,y là tỉ lệ center mới)
            val animCenterX = (state.x * designW).toFloat()
            val animCenterY = (state.y * designH).toFloat()

            val viewCenterX = offsetX + animCenterX * scale
            val viewCenterY = offsetY + animCenterY * scale

            val viewFrameW = frameW * scale * state.scale.toFloat()
            val viewFrameH = frameH * scale * state.scale.toFloat()

            canvas.save()

            // Dịch chuyển đến tâm, xoay, rồi vẽ
            canvas.translate(viewCenterX, viewCenterY)
            canvas.rotate(state.rotation.toFloat())

            val left = -viewFrameW / 2f
            val top = -viewFrameH / 2f
            val right = viewFrameW / 2f
            val bottom = viewFrameH / 2f

            // Clip hình chữ nhật khung
            canvas.clipRect(left, top, right, bottom)
            canvas.drawBitmap(
                croppedBmp,
                null,
                RectF(left, top, right, bottom),
                bitmapPaint
            )

            canvas.restore()
        }
    }

    /**
     * Tạo bitmap đã crop & fit theo cách user đã chỉnh (offset + scale)
     * được cached trên slot để tránh tạo lại mỗi frame
     */
    private fun getCroppedBitmap(
        srcBmp: Bitmap,
        photo: Photo,
        slot: PhotoSlot,
        designW: Float,
        designH: Float
    ): Bitmap? {
        val rect = photo.rect
        val fw = (rect.w * designW).toInt()
        val fh = (rect.h * designH).toInt()
        if (fw <= 0 || fh <= 0) return null

        val result = Bitmap.createBitmap(fw, fh, Bitmap.Config.ARGB_8888)
        val c = Canvas(result)

        val m = Matrix()
        m.postTranslate(-srcBmp.width / 2f, -srcBmp.height / 2f)
        m.postScale(slot.userScale, slot.userScale)
        m.postTranslate(fw / 2f + slot.userOffsetX, fh / 2f + slot.userOffsetY)

        c.drawBitmap(srcBmp, m, bitmapPaint)
        return result
    }
}
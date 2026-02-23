package com.app.base.ui.photo_frame_edit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.app.base.model.Design
import com.app.base.model.KeyFrame
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import com.app.base.model.Rect as AnimRect

/**
 * Custom view hiển thị khung ảnh (từ AnimationData) và cho phép user
 * kéo thả + pinch-to-zoom để fit ảnh vào đúng khung.
 * Phần ảnh nằm ngoài khung sẽ bị clip (cắt).
 */
class PhotoFrameEditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // ===== Data =====
    private var animRect: AnimRect? = null
    private var keyFrame: KeyFrame? = null
    private var bitmap: Bitmap? = null

    // Transform ảnh (center-based)
    var designWidth: Int = 0
    var designHeight: Int = 0
    var imgOffsetX: Float = 0f
    var imgOffsetY: Float = 0f
    var imgScale: Float = 1f

    // Callbacks
    var onPhotoTransformChanged: ((offsetX: Float, offsetY: Float, scale: Float) -> Unit)? = null

    // ===== Paint =====
    private val framePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    private val dimPaint = Paint().apply {
        color = Color.parseColor("#99000000")
        style = Paint.Style.FILL
    }
    private val plusPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    // ===== Gesture =====
    private var mode = TouchMode.NONE
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var lastSpan = 0f
    private var lastMidX = 0f
    private var lastMidY = 0f

    private enum class TouchMode { NONE, DRAG, ZOOM }

    // ===== Computed frame bounds =====
    private val frameBounds = RectF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateFrameBounds()
    }

    private fun updateFrameBounds() {
        val r = animRect ?: return

        val dw = designWidth.toFloat()
        val dh = designHeight.toFloat()
        val vw = width.toFloat()
        val vh = height.toFloat()

        val designScale = min(vw / dw, vh / dh)

        val designOffsetX = (vw - dw * designScale) * 0.5f
        val designOffsetY = (vh - dh * designScale) * 0.5f

        val cx = designOffsetX + r.x * dw * designScale
        val cy = designOffsetY + r.y * dh * designScale

        val fw = r.w * dw * designScale
        val fh = r.h * dh * designScale

        frameBounds.set(
            (cx - fw / 2f).toFloat(),
            (cy - fh / 2f).toFloat(),
            (cx + fw / 2f).toFloat(),
            (cy + fh / 2f).toFloat()
        )

        if (bitmap != null && imgScale == 1f && imgOffsetX == 0f && imgOffsetY == 0f) {
            autoFitBitmap()
        }
    }

    fun setDesign(design: Design) {
        designWidth = design.width
        designHeight = design.height

    }

    fun setPhotoFrame(rect: AnimRect, keyFrame: KeyFrame) {
        animRect = rect
        this.keyFrame = keyFrame
        updateFrameBounds()
        invalidate()
    }

    fun setBitmap(bmp: Bitmap?) {
        bitmap = bmp
        imgOffsetX = 0f
        imgOffsetY = 0f
        imgScale = 1f
        if (bmp != null) autoFitBitmap()
        invalidate()
    }

    fun restoreTransform(offsetX: Float, offsetY: Float, scale: Float) {
        imgOffsetX = offsetX
        imgOffsetY = offsetY
        imgScale = scale
        invalidate()
    }

    /**
     * Tự scale ảnh để cover đủ khung (cover mode, không letterbox)
     */
    private fun autoFitBitmap() {
        val bmp = bitmap ?: return
        val fw = frameBounds.width()
        val fh = frameBounds.height()
        if (fw <= 0 || fh <= 0) return
        val scaleX = fw / bmp.width
        val scaleY = fh / bmp.height
        imgScale = max(scaleX, scaleY)
        imgOffsetX = 0f
        imgOffsetY = 0f
    }

    /**
     * Trả về Bitmap đã crop theo khung, dùng khi save
     */
    fun getCroppedBitmap(): Bitmap? {
        val bmp = bitmap ?: return null
        if (frameBounds.isEmpty) return null

        val fw = frameBounds.width().toInt()
        val fh = frameBounds.height().toInt()
        if (fw <= 0 || fh <= 0) return null

        val result = Bitmap.createBitmap(fw, fh, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val matrix = buildBitmapMatrix(
            offsetX = imgOffsetX,
            offsetY = imgOffsetY,
            scale = imgScale,
            pivotX = frameBounds.width() / 2f,
            pivotY = frameBounds.height() / 2f
        )
        canvas.drawBitmap(bmp, matrix, bitmapPaint)
        return result
    }

    // ===== Drawing =====
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rotation = keyFrame?.rotation?.toFloat() ?: 0f
        val vw = width.toFloat()
        val vh = height.toFloat()
        val cx = frameBounds.centerX()
        val cy = frameBounds.centerY()
        val bmp = bitmap

        canvas.save()

        val framePath = Path().apply { addRect(frameBounds, Path.Direction.CW) }
        val fullPath = Path().apply { addRect(RectF(0f, 0f, vw, vh), Path.Direction.CW) }
        val m = Matrix()
        m.postRotate(
            rotation,
            frameBounds.centerX(),
            frameBounds.centerY()
        )
        framePath.transform(m)
        fullPath.op(framePath, Path.Op.DIFFERENCE)
        canvas.drawPath(fullPath, dimPaint)

        canvas.rotate(rotation, cx, cy)

        if (bmp == null) {
            drawPlusIcon(canvas)
        }

        canvas.save()
        canvas.clipRect(frameBounds)
        if (bmp != null) {
            val matrix = buildBitmapMatrix(
                offsetX = imgOffsetX + frameBounds.centerX(),
                offsetY = imgOffsetY + frameBounds.centerY(),
                scale = imgScale,
                pivotX = 0f,
                pivotY = 0f
            )
            val cx = frameBounds.centerX() + imgOffsetX
            val cy = frameBounds.centerY() + imgOffsetY
            // Translate sang trục toàn view
            val fullMatrix = Matrix()
            fullMatrix.postTranslate(-bmp.width / 2f, -bmp.height / 2f)
            fullMatrix.postScale(imgScale, imgScale)
//            fullMatrix.postRotate(rotation)
            fullMatrix.postTranslate(
                frameBounds.centerX() + imgOffsetX,
                frameBounds.centerY() + imgOffsetY
            )
            canvas.drawBitmap(bmp, fullMatrix, bitmapPaint)
        }
        canvas.restore()

        canvas.drawRect(frameBounds, framePaint)

        canvas.restore()
    }

    private fun buildBitmapMatrix(
        offsetX: Float, offsetY: Float, scale: Float,
        pivotX: Float, pivotY: Float
    ): Matrix {
        val m = Matrix()
        m.postTranslate(-pivotX, -pivotY)
        m.postScale(scale, scale)
        m.postTranslate(offsetX, offsetY)
        return m
    }

    private fun drawPlusIcon(canvas: Canvas) {
        val cx = frameBounds.centerX()
        val cy = frameBounds.centerY()
        val arm = min(frameBounds.width(), frameBounds.height()) * 0.2f
        canvas.drawLine(cx - arm, cy, cx + arm, cy, plusPaint)
        canvas.drawLine(cx, cy - arm, cx, cy + arm, plusPaint)

        // Vẽ hình tròn quanh dấu +
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawCircle(cx, cy, arm * 1.5f, circlePaint)
    }

    // ===== Touch Handling =====
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (bitmap == null) return false // Không cho kéo khi chưa có ảnh

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (!frameBounds.contains(event.x, event.y)) return false
                mode = TouchMode.DRAG
                lastTouchX = event.x
                lastTouchY = event.y
                parent?.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    mode = TouchMode.ZOOM
                    lastSpan = getSpan(event)
                    lastMidX = (event.getX(0) + event.getX(1)) / 2f
                    lastMidY = (event.getY(0) + event.getY(1)) / 2f
                }
            }

            MotionEvent.ACTION_MOVE -> {
                when (mode) {
                    TouchMode.DRAG -> {
                        val dx = event.x - lastTouchX
                        val dy = event.y - lastTouchY
                        imgOffsetX += dx
                        imgOffsetY += dy
                        lastTouchX = event.x
                        lastTouchY = event.y
                        clampTransform()
                        invalidate()
                        notifyTransformChanged()
                    }

                    TouchMode.ZOOM -> {
                        if (event.pointerCount >= 2) {
                            val newSpan = getSpan(event)
                            if (lastSpan > 0) {
                                val factor = newSpan / lastSpan
                                imgScale = (imgScale * factor).coerceIn(0.5f, 8f)
                            }
                            lastSpan = newSpan
                            clampTransform()
                            invalidate()
                            notifyTransformChanged()
                        }
                    }

                    else -> {}
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                mode = TouchMode.DRAG
                val remaining = if (event.actionIndex == 0) 1 else 0
                lastTouchX = event.getX(remaining)
                lastTouchY = event.getY(remaining)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mode = TouchMode.NONE
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    /**
     * Giới hạn offset để ảnh không bị kéo ra xa khung quá mức
     */
    private fun clampTransform() {
        val bmp = bitmap ?: return
        val scaledW = bmp.width * imgScale
        val scaledH = bmp.height * imgScale
        val fw = frameBounds.width()
        val fh = frameBounds.height()

        val maxOffsetX = max(0f, (scaledW - fw) / 2f)
        val maxOffsetY = max(0f, (scaledH - fh) / 2f)

        imgOffsetX = imgOffsetX.coerceIn(-maxOffsetX, maxOffsetX)
        imgOffsetY = imgOffsetY.coerceIn(-maxOffsetY, maxOffsetY)
    }

    private fun notifyTransformChanged() {
        onPhotoTransformChanged?.invoke(imgOffsetX, imgOffsetY, imgScale)
    }

    private fun getSpan(event: MotionEvent): Float {
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        return hypot(dx, dy)
    }
}
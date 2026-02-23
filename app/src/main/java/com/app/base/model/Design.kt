package com.app.base.model

import android.graphics.Bitmap

// Kích thước khung thiết kế (canvas)
data class Design(
    val width: Int,   // chiều rộng canvas (px)
    val height: Int   // chiều cao canvas (px)
)

// Hình chữ nhật mô tả vị trí & kích thước ảnh (theo tỉ lệ 0..1)
data class Rect(
    val x: Double,    // vị trí ngang (0 = trái, 1 = phải)
    val y: Double,    // vị trí dọc (0 = trên, 1 = dưới)
    val w: Double,    // chiều rộng (tỉ lệ so với canvas)
    val h: Double     // chiều cao (tỉ lệ so với canvas)
)

// Keyframe animation của ảnh
data class KeyFrame(
    val time: Long,       // thời gian (ms) kể từ lúc bắt đầu
    val x: Double,        // vị trí ngang tại thời điểm này
    val y: Double,        // vị trí dọc tại thời điểm này
    val scale: Double,    // tỉ lệ zoom (1 = gốc)
    val rotation: Double  // góc xoay (độ, âm = xoay trái)
)

// Thông tin của một photo
data class Photo(
    val rect: Rect,                 // trạng thái ban đầu
    val keys: List<KeyFrame>        // danh sách keyframe animation
)

// Root object
data class AnimationData(
    val design: Design,                     // cấu hình canvas
    val photos: Map<String, Photo>          // PHOTO_1, PHOTO_2, PHOTO_3...
)

// Trạng thái interpolated tại thời điểm bất kỳ
data class FrameState(
    val x: Double,
    val y: Double,
    val scale: Double,
    val rotation: Double
)

// Ảnh đã được user import vào một slot
data class PhotoSlot(
    val slotKey: String,            // "PHOTO_1", "PHOTO_2", ...
    val photo: Photo,               // thông tin animation
    val bitmap: Bitmap? = null,     // ảnh gốc user chọn
    // Offset & scale để user fit ảnh vào khung (trong edit mode)
    val userOffsetX: Float = 0f,
    val userOffsetY: Float = 0f,
    val userScale: Float = 1f
)
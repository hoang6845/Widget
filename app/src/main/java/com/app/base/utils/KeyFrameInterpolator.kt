package com.app.base.utils

import com.app.base.model.FrameState
import com.app.base.model.KeyFrame

object KeyFrameInterpolator {

    /**
     * Tính toán trạng thái tại thời điểm [timeMs] dựa trên danh sách keyframes.
     * Dùng linear interpolation giữa hai keyframe gần nhất.
     */
    fun interpolate(keys: List<KeyFrame>, timeMs: Long): FrameState {
        if (keys.isEmpty()) return FrameState(0.5, 0.5, 1.0, 0.0)
        if (keys.size == 1) {
            val k = keys[0]
            return FrameState(k.x, k.y, k.scale, k.rotation)
        }

        // Clamp về đầu / cuối nếu ra ngoài range
        if (timeMs <= keys.first().time) {
            val k = keys.first()
            return FrameState(k.x, k.y, k.scale, k.rotation)
        }
        if (timeMs >= keys.last().time) {
            val k = keys.last()
            return FrameState(k.x, k.y, k.scale, k.rotation)
        }

        // Tìm hai keyframe bao quanh timeMs
        var prev = keys.first()
        var next = keys.last()
        for (i in 0 until keys.size - 1) {
            if (timeMs >= keys[i].time && timeMs <= keys[i + 1].time) {
                prev = keys[i]
                next = keys[i + 1]
                break
            }
        }

        val duration = next.time - prev.time
        val t = if (duration == 0L) 0f else (timeMs - prev.time).toFloat() / duration.toFloat()
        val eased = easeInOut(t)

        return FrameState(
            x = lerp(prev.x, next.x, eased),
            y = lerp(prev.y, next.y, eased),
            scale = lerp(prev.scale, next.scale, eased),
            rotation = lerpAngle(prev.rotation, next.rotation, eased)
        )
    }

    /** Ease In-Out (smooth cubic) */
    private fun easeInOut(t: Float): Float {
        return t * t * (3f - 2f * t)
    }

    private fun lerp(a: Double, b: Double, t: Float): Double {
        return a + (b - a) * t
    }

    /** Nội suy góc xoay theo đường ngắn nhất */
    private fun lerpAngle(a: Double, b: Double, t: Float): Double {
        var diff = b - a
        // Wrap về khoảng [-180, 180]
        while (diff > 180) diff -= 360
        while (diff < -180) diff += 360
        return a + diff * t
    }
}
package com.app.base.ui.wallpaper_live

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.app.base.model.AnimationData
import com.app.base.model.PhotoSlot
import com.brally.mobile.base.viewmodel.BaseViewModel

class WallPaperLiveViewModel: BaseViewModel() {
    val animationData = MutableLiveData<AnimationData>()
    val photoSlots = MutableLiveData<List<PhotoSlot>>(emptyList())
    val currentEditingSlot = MutableLiveData<String?>()  // slotKey đang chỉnh sửa
    val isSaving = MutableLiveData(false)
    val errorMessage = MutableLiveData<String?>()

    /**
     * Khởi tạo dữ liệu animation (từ JSON/assets)
     */
    fun loadAnimationData(data: AnimationData) {
        animationData.value = data
        // Tạo danh sách slot rỗng cho mỗi photo trong data
        val emptySlots = data.photos.map { (key, photo) ->
            PhotoSlot(slotKey = key, photo = photo)
        }
        photoSlots.value = emptySlots
    }

    /**
     * Gắn bitmap đã import cho một slot
     */
    fun setPhotoForSlot(slotKey: String, bitmap: Bitmap) {
        val current = photoSlots.value?.toMutableList() ?: return
        val idx = current.indexOfFirst { it.slotKey == slotKey }
        if (idx >= 0) {
            current[idx] = current[idx].copy(bitmap = bitmap)
            photoSlots.value = current
        }
    }

    /**
     * Lưu lại transform sau khi user chỉnh xong
     */
    fun saveTransformForSlot(slotKey: String, offsetX: Float, offsetY: Float, scale: Float) {
        val current = photoSlots.value?.toMutableList() ?: return
        val idx = current.indexOfFirst { it.slotKey == slotKey }
        if (idx >= 0) {
            current[idx] = current[idx].copy(
                userOffsetX = offsetX,
                userOffsetY = offsetY,
                userScale = scale
            )
            photoSlots.value = current
        }
    }

    fun setEditingSlot(slotKey: String?) {
        currentEditingSlot.value = slotKey
    }

    /**
     * Kiểm tra tất cả slot đã có ảnh chưa
     */
    fun allSlotsHavePhotos(): Boolean {
        return photoSlots.value?.all { it.bitmap != null } == true
    }

    fun getSlot(slotKey: String): PhotoSlot? {
        return photoSlots.value?.firstOrNull { it.slotKey == slotKey }
    }
}
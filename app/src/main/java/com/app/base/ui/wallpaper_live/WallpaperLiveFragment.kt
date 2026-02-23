package com.app.base.ui.wallpaper_live

import android.app.AlertDialog
import android.app.ProgressDialog
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.app.base.R
import com.app.base.databinding.FragmentWallpaperLiveBinding
import com.app.base.model.AnimationData
import com.app.base.model.Design
import com.app.base.model.KeyFrame
import com.app.base.model.Photo
import com.app.base.model.Rect
import com.app.base.utils.LiveWallpaperService
import com.app.base.utils.VideoComposer
import com.app.base.utils.WallpaperDataStore
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.service.firebase.AppRemoteConfig
import com.ironsource.ph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class WallpaperLiveFragment : BaseFragment<FragmentWallpaperLiveBinding, WallPaperLiveViewModel>() {
    private var currentSlotIndex = 0
    private var videoUri: Uri? = null
    private var composedVideoUri: Uri? = null
    private val animData: AnimationData? by lazy {
        AppRemoteConfig.getData("data", AnimationData::class.java)
    }

    // Launcher pick image
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { loadImageForCurrentSlot(it) }
    }

    override fun initView() {
        setupViewModel()
        animData?.let {
            viewModel.loadAnimationData(it)
        }
        setupVideoBackground()
        showEditMode()
    }

    override fun initListener() {
        binding.btnImportPhoto.setOnClickListener { pickImageLauncher.launch("image/*") }

        binding.btnSaveEdit.setOnClickListener { saveCurrentSlotAndMoveNext() }

        binding.btnPreview.setOnClickListener { showPreviewMode() }

        binding.btnSaveWallpaper.setOnClickListener { showSaveOptions() }

        binding.btnPrevSlot.setOnClickListener {
            saveCurrentSlotTransform()
            if (currentSlotIndex > 0) {
                currentSlotIndex--
                updateSlotUI()
            }
        }
        binding.btnNextSlot.setOnClickListener {
            if (animData == null) return@setOnClickListener
            saveCurrentSlotTransform()
            if (currentSlotIndex < animData!!.photos.size - 1) {
                currentSlotIndex++
                updateSlotUI()
            }
        }
    }

    override fun initData() {

    }

    private fun setupViewModel() {
        viewModel.photoSlots.observe(this) { slots ->
            binding.animationPreviewView.setPhotoSlots(slots)
            updateSlotUI()
        }
    }

    private fun setupVideoBackground() {
        videoUri =
            Uri.parse("android.resource://${requireActivity().packageName}/${R.raw.background_video}")
        binding.videoView.setVideoURI(videoUri)
        binding.videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.start()
            mp.seekTo(mp.duration)
            mp.pause()
        }
    }

    private fun showEditMode() {
        binding.editContainer.visibility = View.VISIBLE
        binding.animationPreviewView.visibility = View.GONE
        binding.btnPreview.visibility = View.VISIBLE
        binding.btnSaveWallpaper.visibility = View.GONE
        updateSlotUI()
    }

    private fun showPreviewMode() {
        // Kiểm tra tất cả slot đã có ảnh chưa
        if (!viewModel.allSlotsHavePhotos()) {
            Toast.makeText(
                requireContext(),
                "Vui lòng import ảnh cho tất cả các khung",
                Toast.LENGTH_SHORT
            )
                .show()
            return
        }

        binding.editContainer.visibility = View.GONE
        binding.animationPreviewView.visibility = View.VISIBLE
        binding.btnPreview.visibility = View.GONE
        binding.btnSaveWallpaper.visibility = View.VISIBLE

        // Đặt dữ liệu cho preview
        animData?.let {
            binding.animationPreviewView.setAnimationData(it)
        }
        binding.animationPreviewView.setPhotoSlots(viewModel.photoSlots.value ?: emptyList())

        // Chạy video từ đầu và sync animation
        binding.videoView.seekTo(0)
        binding.videoView.start()

        // Cập nhật animation theo thời gian video
        startAnimationSync()
    }

    private fun returnToEditMode() {
        binding.videoView.pause()                      // Dừng video
        binding.videoView.seekTo(binding.videoView.duration - 100)  // Về frame cuối

        // Toggle UI visibility
        binding.editContainer.visibility = View.VISIBLE
        binding.animationPreviewView.visibility = View.GONE
        binding.btnBackToEdit.visibility = View.GONE
    }

    private fun startAnimationSync() {
        val handler = Handler(requireActivity().mainLooper)
        val runnable = object : Runnable {
            override fun run() {
                val timeMs = binding.videoView.currentPosition.toLong()
                binding.animationPreviewView.updateTime(timeMs)
                if (binding.videoView.isPlaying) {
                    handler.postDelayed(this, 16L)
                }
            }
        }
        handler.post(runnable)
    }

    private fun showSaveOptions() {
        val options = arrayOf("Màn hình chính (Home)", "Màn hình khoá (Lock)", "Cả hai")
        AlertDialog.Builder(requireContext())
            .setTitle("Lưu làm wallpaper")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> applyAsWallpaper(WallpaperTarget.HOME)
                    1 -> applyAsWallpaper(WallpaperTarget.LOCK)
                    2 -> applyAsWallpaper(WallpaperTarget.BOTH)
                }
            }
            .show()
    }

//    private fun applyAsWallpaper(target: WallpaperTarget) {
//        WallpaperDataStore.videoUri = videoUri
//        WallpaperDataStore.animationData = animData
//        WallpaperDataStore.photoSlots = viewModel.photoSlots.value ?: emptyList()
//        // TODO: Set video URI thực
//
//        // Mở settings để user set live wallpaper
//        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
//            putExtra(
//                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
//                ComponentName(
//                    requireContext(),
//                    LiveWallpaperService::class.java
//                )
//            )
//        }
//        startActivity(intent)
//    }
    private fun applyAsWallpaper(target: WallpaperTarget) {
        if (composedVideoUri == null) {
            // Chưa render video → Phải render trước
            showRenderDialog()
            return
        }

        // Đã có video rendered → Pass video mới vào WallpaperDataStore
        WallpaperDataStore.videoUri = composedVideoUri

        // Mở settings
        val intent = Intent(android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                android.app.WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                android.content.ComponentName(requireActivity(), LiveWallpaperService::class.java)
            )
        }
        startActivity(intent)
    }

    private fun showRenderDialog() {
        val progressDialog = ProgressDialog(requireContext()).apply {
            setTitle("Đang tạo wallpaper")
            setMessage("Đang render video với animation...")
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            max = 100
            setCancelable(false)
            show()
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // File output
                val outputFile = File(requireActivity().filesDir, "wallpaper_${System.currentTimeMillis()}.mp4")

                // Compose video
                val result = VideoComposer.compose(
                    context = requireContext(),
                    sourceVideoUri = videoUri!!,
                    animData = animData!!,
                    photoSlots = viewModel.photoSlots.value ?: emptyList(),
                    outputFile = outputFile,
                    onProgress = { progress ->
                        requireActivity().runOnUiThread {
                            progressDialog.progress = progress
                        }
                    }
                )

                progressDialog.dismiss()

                if (result.success) {
                    // Lưu URI video mới
                    composedVideoUri = Uri.fromFile(outputFile)

                    // Hiện dialog hỏi set wallpaper
                    showSaveOptions()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Lỗi render video: ${result.error}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                progressDialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ===== Slot Management =====

    private fun updateSlotUI() {
        if (animData == null) return
        val slotKeys = animData!!.photos.keys.toList()
        if (slotKeys.isEmpty()) return

        val key = slotKeys[currentSlotIndex]
        binding.txtSlotLabel.text = "Khung ${currentSlotIndex + 1} / ${slotKeys.size}"

        binding.btnPrevSlot.isEnabled = currentSlotIndex > 0
        binding.btnNextSlot.isEnabled = currentSlotIndex < slotKeys.size - 1

        // Cập nhật PhotoFrameEditView với rect của slot hiện tại
        val photo = animData!!.photos[key]!!

        binding.photoFrameEditView.setDesign(animData!!.design)
        binding.photoFrameEditView.setPhotoFrame(
            Rect(
                x = photo.keys.last().x,
                y = photo.keys.last().y,
                w = photo.rect.w,
                h = photo.rect.h
            ),
            photo.keys.last()
        )

        // Restore bitmap & transform nếu đã có
        val slot = viewModel.getSlot(key)
        binding.photoFrameEditView.setBitmap(slot?.bitmap)
        if (slot != null) {
            binding.photoFrameEditView.restoreTransform(
                slot.userOffsetX,
                slot.userOffsetY,
                slot.userScale
            )
        }

        // Lắng nghe thay đổi transform
        binding.photoFrameEditView.onPhotoTransformChanged = { ox, oy, scale ->
            viewModel.saveTransformForSlot(key, ox, oy, scale)
        }
    }

    private fun saveCurrentSlotTransform() {
        if (animData == null) return
        val slotKeys = animData!!.photos.keys.toList()
        val key = slotKeys.getOrNull(currentSlotIndex) ?: return
        viewModel.saveTransformForSlot(
            key,
            binding.photoFrameEditView.imgOffsetX,
            binding.photoFrameEditView.imgOffsetY,
            binding.photoFrameEditView.imgScale
        )
    }

    private fun saveCurrentSlotAndMoveNext() {
        if (animData == null) return
        saveCurrentSlotTransform()
        val slotKeys = animData!!.photos.keys.toList()
        if (currentSlotIndex < slotKeys.size - 1) {
            currentSlotIndex++
            updateSlotUI()
        } else {
            Toast.makeText(requireContext(), "Tất cả khung đã được thiết lập!", Toast.LENGTH_SHORT)
                .show()
            showPreviewMode()
        }
    }

    private fun loadImageForCurrentSlot(uri: Uri) {
        if (animData == null) return
        val slotKeys = animData!!.photos.keys.toList()
        val key = slotKeys.getOrNull(currentSlotIndex) ?: return

        try {
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
            viewModel.setPhotoForSlot(key, bitmap)
            binding.photoFrameEditView.setBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Không thể tải ảnh: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    // ===== Sample Data (thay bằng JSON load thực tế) =====
    private fun createSampleAnimationData(): AnimationData {
        return AnimationData(
            design = Design(1080, 1920),
            photos = mapOf(
                "PHOTO_1" to Photo(
                    rect = Rect(0.05, 0.1, 0.4, 0.35),
                    keys = listOf(
                        KeyFrame(0, 0.25, 0.275, 1.0, 0.0),
                        KeyFrame(2000, 0.27, 0.29, 1.05, 2.0),
                        KeyFrame(4000, 0.25, 0.275, 1.0, 0.0),
                        KeyFrame(8000, 0.25, 0.275, 1.0, 0.0)
                    )
                ),
                "PHOTO_2" to Photo(
                    rect = Rect(0.55, 0.1, 0.4, 0.35),
                    keys = listOf(
                        KeyFrame(0, 0.75, 0.275, 1.0, 0.0),
                        KeyFrame(2500, 0.73, 0.26, 1.05, -2.0),
                        KeyFrame(5000, 0.75, 0.275, 1.0, 0.0),
                        KeyFrame(8000, 0.75, 0.275, 1.0, 0.0)
                    )
                ),
                "PHOTO_3" to Photo(
                    rect = Rect(0.2, 0.5, 0.6, 0.4),
                    keys = listOf(
                        KeyFrame(0, 0.5, 0.7, 0.9, 0.0),
                        KeyFrame(3000, 0.5, 0.68, 1.1, 3.0),
                        KeyFrame(6000, 0.5, 0.7, 0.9, 0.0),
                        KeyFrame(8000, 0.5, 0.7, 0.9, 0.0)
                    )
                )
            )
        )
    }

    enum class WallpaperTarget { HOME, LOCK, BOTH }
}
package com.ext.mediasnaplibrary.api

import android.net.Uri
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import com.ext.mediasnaplibrary.MediaSnapBottomSheet
import com.ext.mediasnaplibrary.config.MediaSnapConfig
import com.ext.mediasnaplibrary.theme.MediaSnapTheme

class MediaSnap private constructor(private val activity: FragmentActivity) {

    companion object {
        fun with(activity: FragmentActivity): MediaSnap {
            MediaSnapConfig.reset()
            return MediaSnap(activity)
        }
    }

    fun images() = apply { MediaSnapConfig.allowImages = true }
    fun videos() = apply { MediaSnapConfig.allowVideos = true }
    fun camera() = apply { MediaSnapConfig.allowCamera = true }

    fun maxSelection(count: Int) = apply {
        MediaSnapConfig.maxSelection = count
    }

    fun setGridSpanCount(count: Int) = apply {
        MediaSnapConfig.gridSpanCount = count
    }

    fun enablePreview(enable: Boolean) = apply {
        MediaSnapConfig.enablePreview = enable
    }

    fun enableCameraX(enable: Boolean) = apply {
        MediaSnapConfig.enableCameraX = enable
    }

    fun setTheme(theme: MediaSnapTheme) = apply {
        MediaSnapConfig.theme = theme
    }

    fun setResultDestination(clazz: Class<*>) = apply {
        MediaSnapConfig.resultDestination = clazz
    }

    fun start(callback: (List<Uri>) -> Unit) {
        MediaSnapConfig.resultCallback = callback

        // ✅ SAFETY CHECK — at least ONE option must be enabled
        if (
            !MediaSnapConfig.allowImages &&
            !MediaSnapConfig.allowVideos &&
            !MediaSnapConfig.allowCamera
        ) {
            throw IllegalStateException(
                "MediaSnap: You must enable at least one of images(), videos(), or camera()."
            )
        }

        val sheet = MediaSnapBottomSheet()
        sheet.show(activity.supportFragmentManager, "MediaSnap")
    }
    fun setBottomSheetLayout(@LayoutRes layoutId: Int) = apply {
        MediaSnapConfig.customBottomSheetLayout = layoutId
    }

}

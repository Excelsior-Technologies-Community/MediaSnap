package com.ext.mediasnaplibrary.config

import android.net.Uri
import com.ext.mediasnaplibrary.theme.MediaSnapTheme

internal object MediaSnapConfig {

    var allowImages = false
    var allowVideos = false
    var allowCamera = false

    var maxSelection = 1
    var gridSpanCount = 3

    var enablePreview = true
    var enableCameraX = true

    var theme: MediaSnapTheme = MediaSnapTheme.default()

    var customBottomSheetLayout: Int? = null

    var resultDestination: Class<*>? = null
    var resultCallback: ((List<Uri>) -> Unit)? = null

    fun reset() {
        allowImages = false
        allowVideos = false
        allowCamera = false
        maxSelection = 1
        gridSpanCount = 3
        enablePreview = true
        enableCameraX = true
        theme = MediaSnapTheme.default()
        resultDestination = null
        resultCallback = null
        customBottomSheetLayout = null
    }
}

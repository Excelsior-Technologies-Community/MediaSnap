package com.ext.mediasnaplibrary

import android.net.Uri

data class MediaItem(
    val uri: Uri,
    val type: MediaType,
    val date: Long,
    var isSelected: Boolean = false
)

enum class MediaType {
    IMAGE,
    VIDEO
}


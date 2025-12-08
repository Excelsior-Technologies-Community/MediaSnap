package com.ext.mediasnaplibrary.theme

data class MediaSnapTheme(
    val primaryColor: Int,
    val accentColor: Int,
    val checkmarkColor: Int,
    val sendButtonColor: Int
) {
    companion object {
        fun default() = MediaSnapTheme(
            primaryColor = 0xFF000000.toInt(),
            accentColor = 0xFFFFFFFF.toInt(),
            checkmarkColor = 0xFF4CAF50.toInt(),
            sendButtonColor = 0xFF2196F3.toInt()
        )
    }
}

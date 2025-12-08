package com.ext.mediasnaplibrary.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.ext.mediasnaplibrary.config.MediaSnapConfig

internal object MediaSnapResultDispatcher {

    fun deliver(context: Context, uris: List<Uri>) {

        // ✅ CALLBACK
        MediaSnapConfig.resultCallback?.invoke(uris)

        // ✅ ACTIVITY DESTINATION
        MediaSnapConfig.resultDestination?.let { clazz ->
            val intent = Intent(context, clazz).apply {
                putParcelableArrayListExtra(
                    "media_result",
                    ArrayList(uris)
                )
            }
            context.startActivity(intent)
        }
    }
}

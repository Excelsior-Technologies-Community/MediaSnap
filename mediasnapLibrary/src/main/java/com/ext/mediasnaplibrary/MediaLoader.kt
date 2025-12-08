package com.ext.mediasnaplibrary

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

class MediaLoader(private val context: Context) {

    fun loadImagesAndVideos(): List<MediaItem> {
        val list = mutableListOf<MediaItem>()

        // Load IMAGES
        loadFromStore(
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            type = MediaType.IMAGE,
            list = list
        )

        // Load VIDEOS
        loadFromStore(
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            type = MediaType.VIDEO,
            list = list
        )

        // Sort by newest
        return list.sortedByDescending { it.date }
    }

    fun loadImagesOnly(): List<MediaItem> {
        val list = mutableListOf<MediaItem>()

        // Load IMAGES
        loadFromStore(
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            type = MediaType.IMAGE,
            list = list
        )

        // Sort by newest
        return list.sortedByDescending { it.date }

    }

    fun loadVideosOnly(): List<MediaItem> {
        val list = mutableListOf<MediaItem>()
        // Load VIDEOS
        loadFromStore(
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            type = MediaType.VIDEO,
            list = list
        )

        // Sort by newest
        return list.sortedByDescending { it.date }
    }

    private fun loadFromStore(
        uri: Uri,
        type: MediaType,
        list: MutableList<MediaItem>
    ) {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATE_ADDED
        )

        val cursor = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val dateColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val date = it.getLong(dateColumn)

                val contentUri = ContentUris.withAppendedId(uri, id)

                list.add(
                    MediaItem(
                        uri = contentUri,
                        type = type,
                        date = date
                    )
                )
            }
        }
    }
}


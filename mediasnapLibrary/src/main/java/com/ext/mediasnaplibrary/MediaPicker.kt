package com.ext.mediasnaplibrary

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import java.text.SimpleDateFormat
import java.util.*

class MediaPicker private constructor(private val host: PickerHostFragment) {

    private var allowImages = false
    private var allowVideos = false
    private var allowDocs = false
    private var allowCamera = false
    private var maxSelection = 1
    private var resultCallback: ((List<Uri>) -> Unit)? = null

    companion object {
        fun with(activity: FragmentActivity): MediaPicker {
            val fm = activity.supportFragmentManager
            val tag = "MediaSnap_PickerHost"
            var frag = fm.findFragmentByTag(tag) as? PickerHostFragment
            if (frag == null) {
                frag = PickerHostFragment()
                fm.beginTransaction().add(frag, tag).commitNow()
            }
            return MediaPicker(frag)
        }
    }

    fun images(): MediaPicker { allowImages = true; return this }
    fun videos(): MediaPicker { allowVideos = true; return this }
    fun documents(): MediaPicker { allowDocs = true; return this }
    fun camera(): MediaPicker { allowCamera = true; return this }
    fun maxSelection(count: Int): MediaPicker { maxSelection = count; return this }

    fun start(callback: (List<Uri>) -> Unit) {
        resultCallback = callback
        val mime = buildMime()
        if (allowCamera) {
            host.launchCamera { uri -> callback.invoke(listOf(uri)) ; resultCallback = null }
        } else {
            host.launchPicker(mime, maxSelection > 1) { uris ->
                callback.invoke(uris)
                resultCallback = null
            }
        }
    }

    private fun buildMime(): String {
        // If caller selected only images -> image/*
        if (allowImages && !allowVideos && !allowDocs) return "image/*"
        if (allowVideos && !allowImages && !allowDocs) return "video/*"
        if (allowDocs && !allowImages && !allowVideos) return "*/*"
        // fallback allow all
        return "*/*"
    }

    // Host fragment that handles ActivityResult APIs (keeps lifecycle safe)
    class PickerHostFragment : Fragment() {

        private var pickerLauncher: ActivityResultLauncher<Intent>? = null
        private var cameraLauncher: ActivityResultLauncher<Uri>? = null
        private var cameraUri: Uri? = null
        private var pendingPickerCallback: ((List<Uri>) -> Unit)? = null
        private var pendingCameraCallback: ((Uri) -> Unit)? = null

        override fun onAttach(context: Context) {
            super.onAttach(context)

            pickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
                ActivityResultCallback { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val data = result.data
                        val list = mutableListOf<Uri>()
                        data?.clipData?.let { clip ->
                            for (i in 0 until clip.itemCount) {
                                list.add(clip.getItemAt(i).uri)
                            }
                        } ?: data?.data?.let { list.add(it) }
                        // persist permission
                        list.forEach { uri ->
                            try {
                                requireContext().contentResolver.takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                )
                            } catch (_: Exception) {}
                        }
                        pendingPickerCallback?.invoke(list)
                        pendingPickerCallback = null
                    } else {
                        pendingPickerCallback?.invoke(emptyList())
                        pendingPickerCallback = null
                    }
                })

            cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success && cameraUri != null) {
                    pendingCameraCallback?.invoke(cameraUri!!)
                } else {
                    pendingCameraCallback?.invoke(Uri.EMPTY)
                }
                pendingCameraCallback = null
            }
        }

        fun launchPicker(mime: String, allowMultiple: Boolean, callback: (List<Uri>) -> Unit) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = mime
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
            }
            pendingPickerCallback = callback
            pickerLauncher?.launch(intent)
        }

        fun launchCamera(callback: (Uri) -> Unit) {
            // Create an output Uri using MediaStore so there's no FileProvider hassle on modern Android
            val resolver = requireContext().contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${timestamp()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/MediaSnap")
                }
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            cameraUri = uri
            pendingCameraCallback = callback
            cameraLauncher?.launch(uri)
        }

        private fun timestamp(): String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    }
}

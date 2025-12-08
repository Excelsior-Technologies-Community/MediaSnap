package com.ext.mediasnaplibrary

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ext.mediasnaplibrary.config.MediaSnapConfig
import com.ext.mediasnaplibrary.core.MediaSnapResultDispatcher
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MediaSnapCameraFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var recentRecyclerView: RecyclerView
    private val recentList = mutableListOf<MediaItem>()

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var flashEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_mediasnap_camera, container, false)

        previewView = view.findViewById(R.id.previewView)
        recentRecyclerView = view.findViewById(R.id.recentRecyclerView)
        recentRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        loadRecentMedia()

        val btnCapture = view.findViewById<ImageButton>(R.id.btnCapture)
        val btnSwitch = view.findViewById<ImageButton>(R.id.btnSwitch)
        val btnFlash = view.findViewById<ImageButton>(R.id.btnFlash)

        btnCapture.setOnClickListener {
            if (MediaSnapConfig.enableCameraX) {
                takePhoto()
            }
        }


        btnSwitch.setOnClickListener {
            lensFacing =
                if (lensFacing == CameraSelector.LENS_FACING_BACK)
                    CameraSelector.LENS_FACING_FRONT
                else
                    CameraSelector.LENS_FACING_BACK
            startCamera()
        }

        btnFlash.setOnClickListener {
            flashEnabled = !flashEnabled
            imageCapture.flashMode =
                if (flashEnabled) ImageCapture.FLASH_MODE_ON
                else ImageCapture.FLASH_MODE_OFF
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // ✅ APPLY enableCameraX FLAG
        if (MediaSnapConfig.enableCameraX) {
            startCamera()
        }

        return view
    }

    // ✅ CAMERA START
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setFlashMode(
                    if (flashEnabled) ImageCapture.FLASH_MODE_ON
                    else ImageCapture.FLASH_MODE_OFF
                )
                .build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (_: Exception) {}

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // ✅ TAKE PHOTO
    private fun takePhoto() {
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(System.currentTimeMillis())

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_$name.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/MediaSnap")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { uri ->

                        parentFragmentManager.beginTransaction()
                            .replace(
                                android.R.id.content,
                                MediaPreviewFragment(uri)
                            )
                            .addToBackStack(null)
                            .commit()
                    }
                }

                override fun onError(exception: ImageCaptureException) {}
            }
        )
    }

    // ✅ RECENT MEDIA
    private fun loadRecentMedia() {
        val loader = MediaLoader(requireContext())
        val all = loader.loadImagesAndVideos()

        recentList.clear()
        recentList.addAll(all.take(15))

        recentRecyclerView.adapter =
            RecentMediaAdapter(recentList) { mediaItem ->

                parentFragmentManager.beginTransaction()
                    .replace(
                        android.R.id.content,
                        MediaPreviewFragment(mediaItem.uri)
                    )
                    .addToBackStack(null)
                    .commit()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

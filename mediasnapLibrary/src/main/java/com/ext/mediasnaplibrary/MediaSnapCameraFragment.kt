package com.ext.mediasnaplibrary

import android.content.ContentValues
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ext.mediasnaplibrary.config.MediaSnapConfig
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MediaSnapCameraFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var videoCapture: VideoCapture<Recorder>

    private lateinit var btnCapture: ImageButton
    private lateinit var txtTimer: TextView

    private var activeRecording: Recording? = null
    private var isRecording = false

    private var recordSeconds = 0
    private val timerHandler = Handler(Looper.getMainLooper())

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
        btnCapture = view.findViewById(R.id.btnCapture)
        txtTimer = view.findViewById(R.id.txtTimer)

        val btnSwitch = view.findViewById<ImageButton>(R.id.btnSwitch)
        val btnFlash = view.findViewById<ImageButton>(R.id.btnFlash)

        recentRecyclerView = view.findViewById(R.id.recentRecyclerView)
        recentRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        loadRecentMedia()

        // ✅ TAP = PHOTO
        btnCapture.setOnClickListener {
            if (!isRecording) takePhoto()
        }

        // ✅ HOLD = START VIDEO
        btnCapture.setOnLongClickListener {
            startVideoRecording()
            true
        }

        // ✅ RELEASE = STOP VIDEO
        btnCapture.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP && isRecording) {
                stopVideoRecording()
            }
            false
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

        if (MediaSnapConfig.enableCameraX) startCamera()

        return view
    }

    // ✅ CAMERA START (PHOTO + VIDEO)
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

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                    videoCapture
                )
            } catch (_: Exception) {}

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // ✅ PHOTO
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
                    output.savedUri?.let { openPreview(it) }
                }

                override fun onError(exception: ImageCaptureException) {}
            }
        )
    }

    // ✅ START VIDEO
    private fun startVideoRecording() {
        if (isRecording) return

        val name = "VID_${System.currentTimeMillis()}.mp4"

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/MediaSnap")
            }
        }

        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(values).build()

        activeRecording =
            videoCapture.output
                .prepareRecording(requireContext(), mediaStoreOutput)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(requireContext())) { event ->

                    if (event is VideoRecordEvent.Finalize) {
                        isRecording = false
                        btnCapture.setBackgroundResource(R.drawable.bg_capture_button)
                        timerHandler.removeCallbacksAndMessages(null)
                        txtTimer.visibility = View.GONE

                        event.outputResults.outputUri?.let { openPreview(it) }
                    }
                }

        // ✅ RECORDING START UI
        isRecording = true
        btnCapture.setBackgroundResource(R.drawable.bg_capture_button_recording)

        recordSeconds = 0
        txtTimer.text = "00:00"
        txtTimer.visibility = View.VISIBLE

        timerHandler.post(object : Runnable {
            override fun run() {
                recordSeconds++
                val mins = recordSeconds / 60
                val secs = recordSeconds % 60
                txtTimer.text = String.format("%02d:%02d", mins, secs)
                timerHandler.postDelayed(this, 1000)
            }
        })
    }

    // ✅ STOP VIDEO
    private fun stopVideoRecording() {
        activeRecording?.stop()
        activeRecording = null
        isRecording = false

        timerHandler.removeCallbacksAndMessages(null)
        txtTimer.visibility = View.GONE
        recordSeconds = 0

        btnCapture.setBackgroundResource(R.drawable.bg_capture_button)
    }

    // ✅ PREVIEW
    private fun openPreview(uri: Uri) {
        parentFragmentManager.beginTransaction()
            .replace(
                android.R.id.content,
                MediaPreviewFragment(uri)
            )
            .addToBackStack(null)
            .commit()
    }

    // ✅ RECENT MEDIA
    private fun loadRecentMedia() {
        val loader = MediaLoader(requireContext())
        val all = loader.loadImagesAndVideos()

        recentList.clear()
        recentList.addAll(all.take(15))

        recentRecyclerView.adapter =
            RecentMediaAdapter(recentList) { mediaItem ->
                openPreview(mediaItem.uri)
            }
    }

    override fun onStop() {
        super.onStop()
        timerHandler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

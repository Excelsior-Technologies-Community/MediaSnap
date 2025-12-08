package com.ext.mediasnap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ext.mediasnaplibrary.MediaPicker
import com.ext.mediasnaplibrary.MediaSnapBottomSheet
import com.ext.mediasnaplibrary.MediaSnapCameraFragment
import com.ext.mediasnaplibrary.PickerHostFragment

class MainActivity : AppCompatActivity() {
    private val cameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.main,
                        MediaSnapCameraFragment { uri ->
                            Log.d("MediaSnap", "Captured from CameraX: $uri")
                        }
                    )
                    .addToBackStack(null)
                    .commit()

            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
            }
        }
    private val galleryPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val granted = result.values.any { it }

            if (granted) {
                openGalleryGrid()
            } else {
                Toast.makeText(this, "Gallery permission required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnOpenMediaSnap).setOnClickListener {

            // Runtime permissions for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.CAMERA
                    ), 101
                )
            }

            val sheet = MediaSnapBottomSheet(object : MediaSnapBottomSheet.OptionSelectedListener {

                override fun onCameraSelected() {
                    if (checkSelfPermission(Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        supportFragmentManager.beginTransaction()
                            .replace(
                                R.id.main,
                                MediaSnapCameraFragment { uri ->
                                    Log.d("MediaSnap", "Captured from CameraX: $uri")
                                }
                            )
                            .addToBackStack(null)
                            .commit()
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }


                override fun onGallerySelected() {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // Android 13 and above
                        galleryPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_VIDEO
                            )
                        )
                    } else {
                        // Android 12 and below
                        galleryPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        )
                    }
                }


                override fun onFilesSelected() {
                    MediaPicker.with(this@MainActivity)
                        .images()
                        .videos()
                        .documents()
                        .start { uris ->
                            Log.d("MediaSnap", "Files selected: $uris")
                        }
                }
            })

            sheet.show(supportFragmentManager, "MediaSnapSheet")
        }
    }

    private fun openGalleryGrid() {
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, PickerHostFragment())
            .addToBackStack(null)
            .commit()
    }


}

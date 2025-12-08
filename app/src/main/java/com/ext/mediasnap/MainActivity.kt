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
import androidx.core.content.ContextCompat
import com.ext.mediasnaplibrary.api.MediaSnap
import com.ext.mediasnaplibrary.theme.MediaSnapTheme

class MainActivity : AppCompatActivity() {

    // ✅ CAMERA + AUDIO PERMISSION LAUNCHER
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->

            val cameraGranted = result[Manifest.permission.CAMERA] == true
            val audioGranted = result[Manifest.permission.RECORD_AUDIO] == true

            if (cameraGranted) {
                openMediaSnap()   // ✅ Open even if audio denied (video will be silent)
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnOpenMediaSnap).setOnClickListener {

            val cameraOk = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            val audioOk = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (cameraOk && audioOk) {
                openMediaSnap()
            } else {
                // ✅ Request both permissions safely
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    )
                )
            }
        }
    }

    // ✅ OPEN MEDIASNAP ONLY AFTER PERMISSION IS GRANTED
    private fun openMediaSnap() {
        MediaSnap.with(this)
            .images()
            .videos()
            .camera()
            .maxSelection(5)
            .setGridSpanCount(3)
            .enablePreview(true)
            .enableCameraX(true)
            .setTheme(MediaSnapTheme.default())
            .start { uris ->
                Log.d("MediaSnap", "Final Selected URIs: $uris")
            }
    }
}

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

    // ✅ CAMERA PERMISSION LAUNCHER
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openMediaSnap()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnOpenMediaSnap).setOnClickListener {

            // ✅ Check CAMERA permission properly
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                openMediaSnap()
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

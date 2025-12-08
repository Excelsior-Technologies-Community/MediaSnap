package com.ext.mediasnaplibrary

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.ext.mediasnaplibrary.config.MediaSnapConfig
import com.ext.mediasnaplibrary.core.MediaSnapResultDispatcher

class MediaSnapBottomSheet : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val layoutId =
            MediaSnapConfig.customBottomSheetLayout
                ?: R.layout.bottom_sheet_mediasnap

        val view = LayoutInflater.from(context).inflate(layoutId, null)


        dialog.setContentView(view)

        // ✅ Bottom Sheet Styling
        dialog.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.BOTTOM)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes.windowAnimations = android.R.style.Animation_Dialog
        }

        val btnCamera = view.findViewById<Button>(R.id.btnCamera)
        val btnGallery = view.findViewById<Button>(R.id.btnGallery)
        val btnFiles = view.findViewById<Button>(R.id.btnFiles)

        // ✅ APPLY CONFIG VISIBILITY
        btnCamera.visibility =
            if (MediaSnapConfig.allowCamera) View.VISIBLE else View.GONE

        btnGallery.visibility =
            if (MediaSnapConfig.allowImages || MediaSnapConfig.allowVideos)
                View.VISIBLE else View.GONE

        btnFiles.visibility = View.VISIBLE

        // ✅ CAMERA HANDLING (CameraX OR System)
        btnCamera.setOnClickListener {

            if (MediaSnapConfig.enableCameraX) {
                parentFragmentManager.beginTransaction()
                    .replace(
                        android.R.id.content,
                        MediaSnapCameraFragment()
                    )
                    .addToBackStack(null)
                    .commit()
            } else {
                openSystemCamera()
            }

            dismiss()
        }

        // ✅ OPEN GALLERY GRID
        btnGallery.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    android.R.id.content,
                    PickerHostFragment()
                )
                .addToBackStack(null)
                .commit()

            dismiss()
        }

        // ✅ OPEN SYSTEM FILE PICKER
        btnFiles.setOnClickListener {
            MediaPicker.with(requireActivity())
                .images()
                .videos()
                .documents()
                .start { uris ->
                    MediaSnapResultDispatcher.deliver(requireContext(), uris)
                }

            dismiss()
        }

        return dialog
    }

    private fun openSystemCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/MediaSnap")
            }
        }

        val uri = requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }

        startActivityForResult(intent, 991)
    }
}

package com.ext.mediasnaplibrary

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.DialogFragment

class MediaSnapBottomSheet(
    private val listener: OptionSelectedListener
) : DialogFragment() {

    interface OptionSelectedListener {
        fun onCameraSelected()
        fun onGallerySelected()
        fun onFilesSelected()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val view = LayoutInflater.from(context)
            .inflate(R.layout.bottom_sheet_mediasnap, null)

        dialog.setContentView(view)

        // Styling so it behaves like a bottom sheet
        dialog.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.BOTTOM)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes.windowAnimations = android.R.style.Animation_Dialog
        }

        // Click Listeners
        view.findViewById<Button>(R.id.btnCamera).setOnClickListener {
            listener.onCameraSelected()
            dismiss()
        }

        view.findViewById<Button>(R.id.btnGallery).setOnClickListener {
            listener.onGallerySelected()
            dismiss()
        }

        view.findViewById<Button>(R.id.btnFiles).setOnClickListener {
            listener.onFilesSelected()
            dismiss()
        }

        return dialog
    }
}

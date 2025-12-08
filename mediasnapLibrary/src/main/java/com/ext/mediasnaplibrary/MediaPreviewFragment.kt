package com.ext.mediasnaplibrary

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.ext.mediasnaplibrary.core.MediaSnapResultDispatcher

class MediaPreviewFragment(
    private val uri: Uri
) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_media_preview, container, false)

        val imgPreview = view.findViewById<ImageView>(R.id.imgPreview)
        val videoPreview = view.findViewById<VideoView>(R.id.videoPreview)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnSend = view.findViewById<ImageButton>(R.id.btnSend)

        val isVideo =
            requireContext().contentResolver.getType(uri)?.startsWith("video") == true

        if (isVideo) {
            imgPreview.visibility = View.GONE
            videoPreview.visibility = View.VISIBLE

            videoPreview.setVideoURI(uri)
            videoPreview.setOnPreparedListener { mp ->
                mp.isLooping = true
                videoPreview.start()
            }

        } else {
            videoPreview.visibility = View.GONE
            imgPreview.visibility = View.VISIBLE

            Glide.with(imgPreview)
                .load(uri)
                .into(imgPreview)
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // ✅ SEND RESULT VIA CENTRAL DISPATCHER
        btnSend.setOnClickListener {
            MediaSnapResultDispatcher.deliver(requireContext(), listOf(uri))
            parentFragmentManager.popBackStack()
        }

        return view
    }
}

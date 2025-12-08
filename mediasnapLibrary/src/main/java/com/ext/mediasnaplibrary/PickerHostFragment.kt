package com.ext.mediasnaplibrary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ext.mediasnaplibrary.config.MediaSnapConfig
import com.ext.mediasnaplibrary.core.MediaSnapResultDispatcher

class PickerHostFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var sendBar: View
    private lateinit var txtSelectedCount: TextView
    private lateinit var btnSend: ImageButton

    private lateinit var adapter: MediaAdapter
    private val mediaList = mutableListOf<MediaItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_picker_host, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        sendBar = view.findViewById(R.id.sendBar)
        txtSelectedCount = view.findViewById(R.id.txtSelectedCount)
        btnSend = view.findViewById(R.id.btnSend)

        sendBar.setBackgroundColor(MediaSnapConfig.theme.primaryColor)
        btnSend.setColorFilter(MediaSnapConfig.theme.sendButtonColor)

        recyclerView.layoutManager =
            GridLayoutManager(requireContext(), MediaSnapConfig.gridSpanCount)

        adapter = MediaAdapter(
            mediaList,
            onSelectionChanged = { selectedCount ->
                if (selectedCount > 0) {
                    sendBar.visibility = View.VISIBLE
                    txtSelectedCount.text = "Send ($selectedCount)"
                } else {
                    sendBar.visibility = View.GONE
                }
            },
            onPreview = { uri ->
                parentFragmentManager.beginTransaction()
                    .replace(
                        android.R.id.content,
                        MediaPreviewFragment(uri)
                    )
                    .addToBackStack(null)
                    .commit()
            }
        )

        recyclerView.adapter = adapter

        btnSend.setOnClickListener {
            val selectedUris =
                mediaList.filter { it.isSelected }.map { it.uri }

            if (selectedUris.isNotEmpty()) {
                MediaSnapResultDispatcher.deliver(requireContext(), selectedUris)
            }
        }


        loadMedia()

        return view
    }

    private fun loadMedia() {
        val loader = MediaLoader(requireContext())

        val result = when {
            MediaSnapConfig.allowImages && MediaSnapConfig.allowVideos ->
                loader.loadImagesAndVideos()

            MediaSnapConfig.allowImages ->
                loader.loadImagesOnly()

            MediaSnapConfig.allowVideos ->
                loader.loadVideosOnly()

            else -> emptyList()
        }

        mediaList.clear()
        mediaList.addAll(result)
        adapter.notifyDataSetChanged()
    }
}

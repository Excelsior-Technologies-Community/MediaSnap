package com.ext.mediasnaplibrary

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PickerHostFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MediaAdapter
    private val mediaList = mutableListOf<MediaItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        recyclerView = RecyclerView(requireContext())
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        adapter = MediaAdapter(mediaList)
        recyclerView.adapter = adapter

        loadMedia()


        return recyclerView
    }

    private fun loadMedia() {
        val loader = MediaLoader(requireContext())
        val result = loader.loadImagesAndVideos()

        mediaList.clear()
        mediaList.addAll(result)
        adapter.notifyDataSetChanged()
    }
}

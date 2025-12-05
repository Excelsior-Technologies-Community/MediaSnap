package com.ext.mediasnaplibrary

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MediaAdapter(private val items: List<MediaItem>) :
    RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    class MediaViewHolder(val imageView: ImageView) :
        RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {

        val image = ImageView(parent.context)
        image.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            300
        )
        image.scaleType = ImageView.ScaleType.CENTER_CROP

        return MediaViewHolder(image)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {

        Glide.with(holder.imageView.context)
            .load(items[position].uri)
            .centerCrop()
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = items.size
}

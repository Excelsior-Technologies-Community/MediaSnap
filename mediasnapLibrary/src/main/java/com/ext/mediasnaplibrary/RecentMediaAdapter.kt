package com.ext.mediasnaplibrary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecentMediaAdapter(
    private val items: List<MediaItem>,
    private val onClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<RecentMediaAdapter.RecentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(150, 150)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return RecentViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        val item = items[position]

        if (item.type == MediaType.VIDEO) {
            Glide.with(holder.imageView)
                .load(item.uri)
                .frame(1_000_000) // ✅ REQUIRED
                .centerCrop()
                .into(holder.imageView)
        } else {
            Glide.with(holder.imageView)
                .load(item.uri)
                .centerCrop()
                .into(holder.imageView)
        }



        holder.imageView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    class RecentViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
}

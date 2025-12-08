package com.ext.mediasnaplibrary

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.media.MediaMetadataRetriever
import android.widget.TextView
import com.ext.mediasnaplibrary.config.MediaSnapConfig

class MediaAdapter(
    private val items: MutableList<MediaItem>,
    private val onSelectionChanged: (Int) -> Unit,
    private val onPreview: (Uri) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgThumb: ImageView = view.findViewById(R.id.imgThumb)
        val overlay: View = view.findViewById(R.id.overlay)
        val imgCheck: ImageView = view.findViewById(R.id.imgCheck)

        val imgPlay: ImageView = view.findViewById(R.id.imgPlay)
        val txtDuration: TextView = view.findViewById(R.id.txtDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media_grid, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val item = items[position]

        // ✅ THEME: Apply checkmark & overlay colors
        holder.imgCheck.setColorFilter(MediaSnapConfig.theme.checkmarkColor)
        holder.overlay.setBackgroundColor(
            MediaSnapConfig.theme.primaryColor and 0x55FFFFFF   // semi-transparent overlay
        )

        if (item.type == MediaType.VIDEO) {

            // ✅ Load video thumbnail frame
            Glide.with(holder.imgThumb)
                .load(item.uri)
                .frame(1_000_000)
                .centerCrop()
                .into(holder.imgThumb)

            // ✅ Show Play Icon & Duration
            holder.imgPlay.visibility = View.VISIBLE
            holder.txtDuration.visibility = View.VISIBLE

            // ✅ THEME: Play icon color
            holder.imgPlay.setColorFilter(MediaSnapConfig.theme.accentColor)

            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(holder.itemView.context, item.uri)

                val durationMs =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull() ?: 0L

                val seconds = (durationMs / 1000) % 60
                val minutes = (durationMs / 1000) / 60

                holder.txtDuration.text =
                    String.format("%02d:%02d", minutes, seconds)

            } catch (e: Exception) {
                holder.txtDuration.text = "00:00"
            } finally {
                retriever.release()
            }

        } else {
            // ✅ Normal image load
            Glide.with(holder.imgThumb)
                .load(item.uri)
                .centerCrop()
                .into(holder.imgThumb)

            // ✅ Hide video UI
            holder.imgPlay.visibility = View.GONE
            holder.txtDuration.visibility = View.GONE
        }

        // ✅ Selection UI
        holder.overlay.visibility =
            if (item.isSelected) View.VISIBLE else View.GONE

        holder.imgCheck.visibility =
            if (item.isSelected) View.VISIBLE else View.GONE

        // ✅ TAP = PREVIEW or DIRECT SELECT
        holder.itemView.setOnClickListener {
            if (MediaSnapConfig.enablePreview) {
                onPreview(item.uri)
            } else {
                if (!item.isSelected && getSelectedCount() >= MediaSnapConfig.maxSelection) return@setOnClickListener
                item.isSelected = !item.isSelected
                notifyItemChanged(position)
                onSelectionChanged(getSelectedCount())
            }

        }

        // ✅ LONG PRESS = MULTI-SELECT WITH LIMIT
        holder.itemView.setOnLongClickListener {
            if (!item.isSelected && getSelectedCount() >= MediaSnapConfig.maxSelection) {
                return@setOnLongClickListener true
            }

            item.isSelected = !item.isSelected
            notifyItemChanged(position)
            onSelectionChanged(getSelectedCount())
            true
        }
    }

    override fun getItemCount() = items.size

    private fun getSelectedCount(): Int =
        items.count { it.isSelected }
}

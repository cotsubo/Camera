package org.fossify.camera.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import org.fossify.camera.R
import org.fossify.camera.models.CapturedMedia
import org.fossify.camera.models.UploadStatus
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MediaAdapter(
    private val onItemClick: (CapturedMedia) -> Unit
) : ListAdapter<CapturedMedia, MediaAdapter.MediaViewHolder>(MediaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnail: ImageView = itemView.findViewById(R.id.media_thumbnail)
        private val uploadStatus: ImageView = itemView.findViewById(R.id.upload_status_icon)
        private val mediaType: ImageView = itemView.findViewById(R.id.media_type_icon)
        private val timestamp: TextView = itemView.findViewById(R.id.media_timestamp)

        fun bind(media: CapturedMedia) {
            val file = File(media.filePath)
            
            // Load thumbnail
            Glide.with(itemView.context)
                .load(file)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(thumbnail)

            // Show video icon for videos
            mediaType.visibility = if (media.isPhoto) View.GONE else View.VISIBLE

            // Show upload status
            when (media.uploadStatus) {
                UploadStatus.SUCCESS -> {
                    uploadStatus.visibility = View.VISIBLE
                    uploadStatus.setImageResource(R.drawable.ic_check_circle_vector)
                }
                UploadStatus.UPLOADING -> {
                    uploadStatus.visibility = View.VISIBLE
                    uploadStatus.setImageResource(R.drawable.ic_flash_auto_vector)
                }
                UploadStatus.FAILED -> {
                    uploadStatus.visibility = View.VISIBLE
                    uploadStatus.setImageResource(R.drawable.ic_error_outline_vector)
                }
                else -> {
                    uploadStatus.visibility = View.GONE
                }
            }

            // Format timestamp
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            timestamp.text = dateFormat.format(Date(media.timestamp))

            itemView.setOnClickListener {
                onItemClick(media)
            }
        }
    }

    class MediaDiffCallback : DiffUtil.ItemCallback<CapturedMedia>() {
        override fun areItemsTheSame(oldItem: CapturedMedia, newItem: CapturedMedia): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CapturedMedia, newItem: CapturedMedia): Boolean {
            return oldItem == newItem
        }
    }
}

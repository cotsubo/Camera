package org.fossify.camera.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "captured_media")
data class CapturedMedia(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val fileName: String,
    val mimeType: String,
    val isPhoto: Boolean,
    val timestamp: Long,
    val fileSize: Long,
    val uploadStatus: UploadStatus = UploadStatus.PENDING,
    val uploadAttempts: Int = 0,
    val lastUploadAttempt: Long? = null,
    val serverUrl: String? = null
)

enum class UploadStatus {
    PENDING,
    UPLOADING,
    SUCCESS,
    FAILED
}

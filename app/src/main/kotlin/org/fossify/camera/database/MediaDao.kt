package org.fossify.camera.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.fossify.camera.models.CapturedMedia
import org.fossify.camera.models.UploadStatus

@Dao
interface MediaDao {
    @Query("SELECT * FROM captured_media ORDER BY timestamp DESC")
    fun getAllMedia(): Flow<List<CapturedMedia>>
    
    @Query("SELECT * FROM captured_media WHERE uploadStatus = :status ORDER BY timestamp DESC")
    fun getMediaByStatus(status: UploadStatus): Flow<List<CapturedMedia>>
    
    @Query("SELECT * FROM captured_media WHERE id = :id")
    suspend fun getMediaById(id: Long): CapturedMedia?
    
    @Insert
    suspend fun insertMedia(media: CapturedMedia): Long
    
    @Update
    suspend fun updateMedia(media: CapturedMedia)
    
    @Delete
    suspend fun deleteMedia(media: CapturedMedia)
    
    @Query("UPDATE captured_media SET uploadStatus = :status WHERE id = :id")
    suspend fun updateUploadStatus(id: Long, status: UploadStatus)
    
    @Query("UPDATE captured_media SET uploadStatus = :status, uploadAttempts = :attempts, lastUploadAttempt = :timestamp WHERE id = :id")
    suspend fun updateUploadAttempt(id: Long, status: UploadStatus, attempts: Int, timestamp: Long)
}

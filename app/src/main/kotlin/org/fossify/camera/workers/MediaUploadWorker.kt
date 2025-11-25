package org.fossify.camera.workers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.fossify.camera.database.SimpleMediaStorage
import org.fossify.camera.extensions.config
import org.fossify.camera.models.UploadStatus
import org.fossify.camera.network.RetrofitClient
import java.io.File

class MediaUploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val mediaStorage = SimpleMediaStorage.getInstance(context)
    private val config = context.config

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val mediaId = inputData.getLong("media_id", -1L)
            if (mediaId == -1L) {
                return@withContext Result.failure()
            }

            val media = mediaStorage.getMediaById(mediaId) ?: return@withContext Result.failure()
            
            // Check if upload is enabled
            if (!config.autoUploadEnabled) {
                return@withContext Result.failure()
            }
            
            // Check WiFi requirement
            if (config.uploadOnlyOnWifi && !isWifiConnected()) {
                return@withContext Result.retry()
            }
            
            // Check server URL
            if (config.serverUrl.isEmpty()) {
                return@withContext Result.failure()
            }

            // Update status to uploading
            mediaStorage.updateMedia(media.copy(uploadStatus = UploadStatus.UPLOADING))

            // Prepare file for upload
            val file = File(media.filePath)
            if (!file.exists()) {
                mediaStorage.updateMedia(media.copy(uploadStatus = UploadStatus.FAILED))
                return@withContext Result.failure()
            }

            val requestBody = file.asRequestBody(media.mimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData(
                "file",
                media.fileName,
                requestBody
            )

            // Get device ID
            val deviceId = Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            // Upload to server
            val api = RetrofitClient.getUploadApi(config.serverUrl)
            val authToken = if (config.serverAuthToken.isNotEmpty()) {
                "Bearer ${config.serverAuthToken}"
            } else {
                ""
            }
            
            val response = api.uploadMedia(
                authToken = authToken,
                file = filePart,
                timestamp = media.timestamp,
                isPhoto = media.isPhoto,
                deviceId = deviceId
            )

            if (response.isSuccessful && response.body()?.success == true) {
                // Upload successful
                mediaStorage.updateMedia(media.copy(uploadStatus = UploadStatus.SUCCESS))
                Result.success()
            } else {
                // Upload failed
                val attempts = media.uploadAttempts + 1
                mediaStorage.updateMedia(
                    media.copy(
                        uploadStatus = UploadStatus.FAILED,
                        uploadAttempts = attempts,
                        lastUploadAttempt = System.currentTimeMillis()
                    )
                )
                
                // Retry up to 3 times
                if (attempts < 3) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val mediaId = inputData.getLong("media_id", -1L)
            if (mediaId != -1L) {
                val media = mediaStorage.getMediaById(mediaId)
                if (media != null) {
                    val attempts = media.uploadAttempts + 1
                    mediaStorage.updateMedia(
                        media.copy(
                            uploadStatus = UploadStatus.FAILED,
                            uploadAttempts = attempts,
                            lastUploadAttempt = System.currentTimeMillis()
                        )
                    )
                    
                    if (attempts < 3) {
                        return@withContext Result.retry()
                    }
                }
            }
            Result.failure()
        }
    }

    private fun isWifiConnected(): Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    companion object {
        const val WORK_NAME = "media_upload_work"

        fun createWorkRequest(mediaId: Long): OneTimeWorkRequest {
            val inputData = Data.Builder()
                .putLong("media_id", mediaId)
                .build()

            return OneTimeWorkRequestBuilder<MediaUploadWorker>()
                .setInputData(inputData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    java.util.concurrent.TimeUnit.MILLISECONDS
                )
                .build()
        }
    }
}

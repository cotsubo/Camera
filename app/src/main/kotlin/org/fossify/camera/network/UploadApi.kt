package org.fossify.camera.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface UploadApi {
    @Multipart
    @POST("upload")
    suspend fun uploadMedia(
        @Header("Authorization") authToken: String,
        @Part file: MultipartBody.Part,
        @Part("timestamp") timestamp: Long,
        @Part("isPhoto") isPhoto: Boolean,
        @Part("deviceId") deviceId: String
    ): Response<UploadResponse>
}

data class UploadResponse(
    val success: Boolean,
    val message: String?,
    val fileId: String?
)

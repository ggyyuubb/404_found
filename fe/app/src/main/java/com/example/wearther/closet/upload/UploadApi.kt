package com.example.wearther.closet.upload

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

// UploadApi.kt (업로드만 새 경로)
interface UploadApi {
    @Multipart
    @POST("upload/")  // 새 업로드 경로
    suspend fun uploadClothes(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part,
        @Part("category") category: RequestBody,
        @Part("type") type: RequestBody
    ): UploadResponse
}

data class UploadResponse(
    val message: String? = null,
    val url: String? = null,
    val error: String? = null
)
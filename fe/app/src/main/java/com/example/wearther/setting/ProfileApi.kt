package com.example.wearther.setting.data

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

data class ProfileUploadResponse(
    val message: String,
    val url: String
)

interface ProfileApi {
    @Multipart
    @POST("user/profile_image")
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part
        // ✅ @Header 제거! AuthInterceptor가 자동으로 Bearer 토큰 추가
    ): Response<ProfileUploadResponse>
}
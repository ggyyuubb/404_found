package com.example.wearther.setting.data

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

// ProfileApi.kt 수정
data class ProfileUploadResponse(
    val message: String,
    val url: String
)

interface ProfileApi {
    @Multipart
    @POST("user/profile_image")
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part,
        @Header("Authorization") token: String
    ): Response<ProfileUploadResponse>
}
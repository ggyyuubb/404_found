package com.example.wearther.closet.upload

import okhttp3.MultipartBody
import retrofit2.http.*

interface UploadApi {
    @Multipart
    @POST("upload/")  // image/ → upload/
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): UploadResponse

    @FormUrlEncoded
    @PUT("upload/edit_image/{image_id}")
    suspend fun updateClothing(
        @Header("Authorization") token: String,
        @Path("image_id") imageId: String,
        @Field("clothing_type") clothingType: String? = null,
        @Field("material") material: String? = null,
        @Field("suitable_temperature") suitableTemperature: String? = null
    ): UpdateResponse
}

data class UploadResponse(
    val message: String? = null,
    val url: String? = null,
    val id: String? = null,

    val ai_result: AIResult? = null,
    val error: String? = null
)

data class AIResult(
    val clothing_type: String? = null,
    val colors: List<String>? = null,
    val material: String? = null,
    val suitable_temperature: String? = null
)

data class UpdateResponse(
    val message: String? = null,
    val updated: Map<String, Any>? = null
)
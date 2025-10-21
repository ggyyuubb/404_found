package com.example.wearther.closet.upload

import okhttp3.MultipartBody
import retrofit2.http.*

interface UploadApi {
    @Multipart
    @POST("upload/")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): UploadResponse

    // JSON 바디로 한 번에 업데이트 (colors 배열 포함)
    @PUT("upload/edit_image/{image_id}")
    @Headers("Content-Type: application/json")
    suspend fun updateClothingJson(
        @Header("Authorization") token: String,
        @Path("image_id") imageId: String,
        @Body body: UpdateClothingRequest
    ): UpdateResponse
}

// 서버 구조에 맞춘 바디
data class UpdateClothingRequest(
    val type: String? = null,                  // 한글 대분류: "상의" / "하의" / "아우터" / "원피스"
    val category: String? = null,              // 영문 세부: "shortsleeve", "denim", "dress" 등
    val colors: List<String>? = null,          // ["Black","White"] 처럼 첫 글자 대문자
    val material: String? = null,
    val suitable_temperature: String? = null
)

// 기존 그대로 유지 (업로드 응답)
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

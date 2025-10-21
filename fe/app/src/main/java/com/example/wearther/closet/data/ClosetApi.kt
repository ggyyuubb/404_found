package com.example.wearther.closet.data

import retrofit2.http.*

interface ClosetApi {
    @GET("upload/my_images")
    suspend fun getMyImages(
        @Header("Authorization") token: String
    ): ClosetResponse

    @DELETE("upload/delete_image/{imageId}")
    suspend fun deleteImage(
        @Header("Authorization") token: String,
        @Path("imageId") imageId: String
    ): DeleteResponse

    // ✅ 옷 정보 수정
    @PATCH("upload/update_image/{imageId}")
    suspend fun updateImage(
        @Header("Authorization") token: String,
        @Path("imageId") imageId: String,
        @Body updateData: UpdateImageRequest
    ): ClosetImage
}

// ✅ 수정 요청 DTO
data class UpdateImageRequest(
    val type: String,                    // 한글 대분류: "상의", "하의", "아우터", "원피스"
    val category: String,                // 영문 세부: "longsleeve", "sweater" 등
    val colors: List<String>,            // 첫글자 대문자: ["Blue", "Gray"]
    val material: String?,
    val suitable_temperature: String?
)


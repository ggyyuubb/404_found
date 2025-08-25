// closet/data/ClosetApi.kt (원래 코드로 복구)
package com.example.wearther.closet.data

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ClosetApi {
    @GET("upload/my_images")  // 원래 경로로 복구
    suspend fun getMyImages(
        @Header("Authorization") token: String,
        @Query("type") type: String? = null,
        @Query("category") category: String?
    ): ClosetResponse

    @DELETE("upload/delete_image/{imageId}")  // 원래 경로로 복구
    suspend fun deleteImage(
        @Header("Authorization") token: String,
        @Path("imageId") imageId: String
    ): DeleteResponse
}
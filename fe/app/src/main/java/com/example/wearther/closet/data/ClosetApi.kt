package com.example.wearther.closet.data

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ClosetApi {
    @GET("upload/my_images")
    suspend fun getMyImages(
        @Header("Authorization") token: String,
        @Query("type") type: String? = null
    ): ClosetResponse

    @DELETE("upload/delete_image/{imageId}")
    suspend fun deleteImage(
        @Header("Authorization") token: String,
        @Path("imageId") imageId: String
    ): DeleteResponse
}

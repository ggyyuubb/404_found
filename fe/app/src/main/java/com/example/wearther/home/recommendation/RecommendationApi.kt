package com.example.wearther.home.recommendation

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface RecommendationApi {
    // ✅ Flask 블루프린트 등록된 경로 반영 (/api/recommend/recommendations/ai)
    @POST("api/recommend/recommendations/ai")
    suspend fun getRecommendation(
        @Header("Authorization") token: String,
        @Body request: RecommendationRequest
    ): RecommendationResponse
}

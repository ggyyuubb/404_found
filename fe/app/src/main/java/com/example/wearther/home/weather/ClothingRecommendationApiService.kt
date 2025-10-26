// 📁 ClothingRecommendationApiService.kt - 새 파일
package com.example.wearther.home.weather

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header

// 👇 백엔드로 보낼 요청 데이터 (간소화)
data class ClothingRecommendationRequest(
    val location: String,           // 위치명
    val weather: WeatherInfoForBackend, // 날씨 정보 (필요한 것만 추출)
    val day_index: Int,             // 요일 인덱스
    val comment: String? = null     // 사용자 코멘트 (선택)
)

// 👇 백엔드가 필요로 하는 날씨 정보만 추출한 모델
data class WeatherInfoForBackend(
    val temp: Double,               // 현재 온도
    val feelsLike: Double,          // 체감 온도
    val humidity: Int,              // 습도
    val weather: String,            // 날씨 상태 (Clear, Rain 등)
    val description: String,        // 날씨 설명
    val windSpeed: Double,          // 풍속
    val timestamp: String           // 조회 시간
)

// 👇 백엔드로부터 받을 응답 데이터
data class ClothingRecommendationResponse(
    val recommended: RecommendedClothing?,
    val temp: Int?,
    val weather_code: String?,
    val claude_response: Any? = null,
    val replace_json: Any? = null,
    val new_recommend: RecommendedClothing? = null
)

data class RecommendedClothing(
    val top: String?,
    val bottom: String?,
    val outer: String?
)

interface ClothingRecommendationApiService {
    @POST("recommendations/ai")
    suspend fun getRecommendation(
        @Body request: ClothingRecommendationRequest,
        @Header("Authorization") token: String
    ): Response<ClothingRecommendationResponse>
}
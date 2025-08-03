package com.example.wearther.home.recommendation

// ✅ 데이터 모델 import: 요청(Request), 응답(Response) 형식 정의

// ✅ Retrofit의 HTTP 어노테이션과 관련 기능 import
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// ✅ 추천 기능을 위한 API 인터페이스 정의
interface RecommendationApi {

    @POST("recommend/recommendations/ai")
    suspend fun getRecommendation(
        @Header("Authorization") token: String, // 헤더에 JWT 토큰을 포함시킴 ("Bearer xxx" 형식)
        @Body request: RecommendationRequest = RecommendationRequest() // 요청 본문에 날씨/사용자 정보 포함 (기본값 사용 가능)
    ): RecommendationResponse // 서버 응답은 RecommendationResponse 형태로 반환됨
}

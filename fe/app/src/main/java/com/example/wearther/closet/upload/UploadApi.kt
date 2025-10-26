package com.example.wearther.closet.upload

import okhttp3.MultipartBody
import retrofit2.http.*

// BASE_URL은 루트까지만 넣어 주세요. (예: http://HOST:PORT/)
// 아래 경로는 전부 /upload/... 으로 일치

interface UploadApi {
    // 업로드 1회로: AI 분석 + Firestore 저장 + id 발급
    @Multipart
    @POST("upload/")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): UploadResponse

    // ✅ 백엔드 라우트에 맞춰 PATCH 사용 (edit_image -> update_image 로 경로 수정)
    @PATCH("upload/update_image/{image_id}")
    @Headers("Content-Type: application/json")
    suspend fun updateClothingJson(
        @Header("Authorization") token: String,
        @Path("image_id") imageId: String,
        @Body body: UpdateClothingRequest
    ): UpdateResponse

    // ✅ 분석 프리뷰 (저장 X) — 로딩 화면에서 하드코딩 대신 실제 호출
    @Multipart
    @POST("upload/analyze")
    suspend fun analyzeOnly(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): AnalyzeResponse
}

// 서버 구조에 맞춘 바디
data class UpdateClothingRequest(
    val type: String? = null,                  // 한글 대분류: "상의"/"하의"/"아우터"/"원피스"
    val category: String? = null,              // 영문 세부: "shortsleeve", "denim", "dress" 등
    val colors: List<String>? = null,          // ["Black","White"]
    val material: String? = null,
    val suitable_temperature: String? = null
)

// 업로드 응답
data class UploadResponse(
    val message: String? = null,
    val url: String? = null,
    val id: String? = null,
    val ai_result: AIResult? = null, // ⭐️ 이 부분에 AIResult가 포함됨
    val error: String? = null
)

// ⭐️ [수정됨]
// 백엔드의 'upload_file' 함수가 반환하는 'ai_result' (doc_data) 구조에 맞춤
data class AIResult(
    val type: String? = null,                  // ⬅️ clothing_type에서 변경
    val category: String? = null,            // ⬅️ category 추가
    val colors: List<String>? = null,
    val material: String? = null,
    val suitable_temperature: String? = null,
    // 백엔드가 id, url 등도 보내지만, UploadResponse와 중복되므로 생략 가능
    val id: String? = null
)

// PATCH 응답(백엔드에서 success/message/updated_fields 등도 줄 수 있음)
data class UpdateResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val updated: Map<String, Any>? = null,
    val id: String? = null,
    val updated_fields: List<String>? = null,
    val error: String? = null
)

// 분석 프리뷰 응답 (저장 X)
// ⭐️ 이 부분은 백엔드의 'analyze_only' 응답과 이미 일치하므로 수정 X
data class AnalyzeResponse(
    val success: Boolean? = null,
    val type: String? = null,
    val category: String? = null,
    val colors: List<String>? = null,
    val material: String? = null,
    val suitable_temperature: String? = null,
    val error: String? = null
)
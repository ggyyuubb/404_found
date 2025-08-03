// RecommendedClothing.kt
package com.example.wearther.home.recommendation

// ✅ AI 추천 옷 정보를 표현하는 데이터 클래스
data class RecommendedClothing(
    val category: String,
    val filename: String,
    val type: String,
    val url: String,
    val user_id: String
)

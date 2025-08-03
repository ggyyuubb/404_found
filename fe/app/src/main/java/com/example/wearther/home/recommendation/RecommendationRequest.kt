package com.example.wearther.home.recommendation

// ✅ 서버에 날씨 기반 옷 추천 요청을 보낼 때 사용하는 데이터 모델 클래스
data class RecommendationRequest(
    val city: String = "Seoul"  // 🔹 요청에 포함할 도시 이름 (기본값은 "Seoul")
    // 🔸 백엔드 서버는 이 도시명으로 날씨를 조회하고 추천 결과를 반환함
    // 🔸 사용자가 위치 기반 추천을 원할 경우 이 값은 동적으로 변경 가능
)

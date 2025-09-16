package com.example.wearther.community.data

enum class PostSourceOption(val title: String, val description: String, val iconRes: Int) {
    ALBUM("앨범", "휴대폰에서 사진 선택", com.example.wearther.R.drawable.ic_photo_library),
    CLOSET("옷장", "내 옷장에서 선택", com.example.wearther.R.drawable.ic_closet),
    AI_RECOMMENDATION("AI 추천", "AI가 추천하는 코디", com.example.wearther.R.drawable.ic_ai_stars)
}
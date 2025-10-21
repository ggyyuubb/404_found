package com.example.wearther.ui.screens.closet.data

import com.example.wearther.closet.data.FashionItem

val sampleItems = listOf(
    // 상의
    FashionItem(
        id = "1",
        type = "상의",
        category = "티셔츠",
        image = "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=400&h=500&fit=crop",
        colors = listOf("화이트", "베이지"),
        material = "면",
        suitable_temperature = "20-25°C"
    ),
    FashionItem(
        id = "2",
        type = "상의",
        category = "셔츠",
        image = "https://images.unsplash.com/photo-1485230895905-ec40ba36b9bc?w=400&h=500&fit=crop",
        colors = listOf("블랙"),
        material = "폴리에스터",
        suitable_temperature = "15-20°C"
    ),
    FashionItem(
        id = "3",
        type = "상의",
        category = "니트", // 세부분류로 ‘니트’는 허용(소재 ‘니트’는 금지)
        image = "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=400&h=500&fit=crop",
        colors = listOf("네이비"),
        material = "울",
        suitable_temperature = "10-20°C"
    ),

    // 하의
    FashionItem(
        id = "4",
        type = "하의",
        category = "데님",
        image = "https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=400&h=500&fit=crop",
        colors = listOf("블루"),
        material = "데님",
        suitable_temperature = "10-25°C"
    ),
    FashionItem(
        id = "5",
        type = "하의",
        category = "슬랙스",
        image = "https://images.unsplash.com/photo-1506629905607-45dc4a7d7044?w=400&h=500&fit=crop",
        colors = listOf("그레이"),
        material = "코튼",
        suitable_temperature = "15-25°C"
    ),

    // 원피스
    FashionItem(
        id = "6",
        type = "원피스",
        category = "롱원피스",
        image = "https://images.unsplash.com/photo-1515372039744-b8f02a3ae446?w=400&h=500&fit=crop",
        colors = listOf("레드", "화이트"),
        material = "실크",
        suitable_temperature = "20-28°C"
    ),
    FashionItem(
        id = "7",
        type = "원피스",
        category = "숏원피스",
        image = "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=400&h=500&fit=crop",
        colors = listOf("플로럴"),
        material = "면",
        suitable_temperature = "22-30°C"
    ),

    // 상의 (후드)
    FashionItem(
        id = "8",
        type = "상의",
        category = "후드",
        image = "https://images.unsplash.com/photo-1618932260643-eee4a2f652a6?w=400&h=500&fit=crop",
        colors = listOf("블랙"),
        material = "폴리에스터",
        suitable_temperature = "5-15°C"
    ),

    // 하의
    FashionItem(
        id = "9",
        type = "하의",
        category = "코튼",
        image = "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=400&h=500&fit=crop",
        colors = listOf("베이지"),
        material = "면",
        suitable_temperature = "15-28°C"
    )
)

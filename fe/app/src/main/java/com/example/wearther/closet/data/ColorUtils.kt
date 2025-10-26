package com.example.wearther.closet.data

import androidx.compose.ui.graphics.Color

fun nameToColor(name: String): Color = when (name.trim().lowercase()) {
    "beige" -> Color(0xFFD9C3A3)
    "black" -> Color(0xFF000000)
    "blue" -> Color(0xFF3667C8)
    "brown" -> Color(0xFF7A4E2B)
    "gray", "grey" -> Color(0xFF808080)
    "green" -> Color(0xFF2E7D32)
    "orange" -> Color(0xFFF57C00)
    "pink" -> Color(0xFFF8BBD0)
    "purple" -> Color(0xFF7E57C2)
    "red" -> Color(0xFFD32F2F)
    "white" -> Color(0xFFFFFFFF)
    "yellow" -> Color(0xFFFFEB3B)
    "navy" -> Color(0xFF0F2D52)
    "khaki" -> Color(0xFF6B7B59)
    "ivory" -> Color(0xFFFFF8E1)
    else -> Color(0xFFE0E0E0)
}
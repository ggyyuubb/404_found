package com.example.wearther.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.wearther.R

val Suit = FontFamily(
    Font(R.font.suit_thin, FontWeight.Thin),              // 100
    Font(R.font.suit_extralight, FontWeight.ExtraLight),  // 200
    Font(R.font.suit_light, FontWeight.Light),            // 300
    Font(R.font.suit_regular, FontWeight.Normal),         // 400
    Font(R.font.suit_medium, FontWeight.Medium),          // 500
    Font(R.font.suit_semibold, FontWeight.SemiBold),      // 600
    Font(R.font.suit_bold, FontWeight.Bold),              // 700
    Font(R.font.suit_extrabold, FontWeight.ExtraBold),    // 800
    Font(R.font.suit_heavy, FontWeight.W900)              // 900 (Black 자리에 Heavy 매핑)
)

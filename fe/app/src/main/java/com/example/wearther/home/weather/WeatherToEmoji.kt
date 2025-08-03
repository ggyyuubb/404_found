package com.example.wearther.home.weather

fun weatherToEmoji(main: String): String {
    return when (main.lowercase()) {
        "clear" -> "☀️"
        "clouds" -> "☁️"
        "rain" -> "🌧️"
        "drizzle" -> "🌦️"
        "thunderstorm" -> "⛈️"
        "snow" -> "❄️"
        "mist", "fog", "haze" -> "🌫️"
        "wind", "breeze" -> "🌬️"
        else -> "🌡️"
    }
}

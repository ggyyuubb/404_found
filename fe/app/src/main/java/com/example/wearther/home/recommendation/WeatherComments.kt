package com.example.wearther.home.recommendation

data class WeatherAdvice(val weatherComment: String, val tempComment: String)

fun getWeatherAdvice(code: Int?, temp: Int?): WeatherAdvice {
    val weatherComment = when (code) {
        0 -> "☀️ 맑은 날이에요. 외출하기 좋은 날이에요."
        1 -> "☁️ 흐린 날이에요. 우산은 필요 없어요."
        2 -> "🌧️ 비가 올 수 있어요. 우산을 챙기세요."
        3 -> "❄️ 눈이 올 수 있어요. 미끄럼 조심하세요."
        4 -> "🔥 폭염주의보에요. 외출을 피하세요."
        5 -> "🍃 쌀쌀한 날이에요. 따뜻하게 입으세요."
        else -> "날씨 정보를 확인할 수 없어요."
    }

    val tempComment = when {
        temp == null -> ""
        temp >= 30 -> "오늘은 매우 더워요. 시원하게 입으세요."
        temp in 20..29 -> "따뜻한 날씨에요. 가볍게 입어도 괜찮아요."
        temp in 10..19 -> "선선한 날씨에요. 얇은 겉옷을 챙기세요."
        else -> "추운 날씨에요. 따뜻하게 입으세요."
    }

    return WeatherAdvice(weatherComment, tempComment)
}

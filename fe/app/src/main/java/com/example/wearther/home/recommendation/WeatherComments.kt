package com.example.wearther.home.recommendation

import com.example.wearther.home.weather.*
import kotlin.random.Random
import java.util.*
import java.text.SimpleDateFormat

data class WeatherAdvice(
    val comments: List<String> // 특보(있으면 1개) + 랜덤 코멘트 2개
)

private enum class TimeBucket { MORNING, DAYTIME, EVENING, NIGHT }

private fun formatTime(ts: Long): String {
    val date = Date(ts * 1000) // Unix seconds → millis
    val sdf = SimpleDateFormat("a h시 m분", Locale.KOREA)
    return sdf.format(date)
}

fun generateWeatherAdvice(
    current: CurrentWeather,
    hourly: List<HourlyWeather>,
    daily: List<DailyWeather>?,
    alerts: List<WeatherAlert>?
): WeatherAdvice {
    val out = mutableListOf<String>()
    val bucket = currentTimeBucket(current)
    val candidates = linkedSetOf<String>() // 후보군(중복 제거)

    // 1️⃣ 특보: 있으면 항상 첫 줄
    alerts?.firstOrNull()?.let { out += "📢 ${it.event}: ${it.description}" }

    // ---- 현재 날씨 기반 조건 (모든 날씨 범위 포괄) ----
    val rainNow = current.rain?.oneHour ?: 0.0
    val snowNow = current.snow?.oneHour ?: 0.0
    val windy = current.wind_speed >= 8 || (current.wind_gust ?: 0.0) >= 12
    val veryWindy = current.wind_speed >= 15 || (current.wind_gust ?: 0.0) >= 20
    val extremeWind = current.wind_speed >= 25 || (current.wind_gust ?: 0.0) >= 35

    // 🌡️ 상호배타적 온도 분류 (범위가 겹치지 않도록)
    val extremeCold = current.temp < -15      // -15도 미만
    val freezing = current.temp >= -15 && current.temp < -5   // -15도 이상 -5도 미만
    val veryCold = current.temp >= -5 && current.temp < 0     // -5도 이상 0도 미만
    val cold = current.temp >= 0 && current.temp < 5          // 0도 이상 5도 미만
    val chilly = current.temp >= 5 && current.temp < 10       // 5도 이상 10도 미만
    val cool = current.temp >= 10 && current.temp < 15        // 10도 이상 15도 미만
    val mild = current.temp >= 15 && current.temp < 22        // 15도 이상 22도 미만
    val warm = current.temp >= 22 && current.temp < 28        // 22도 이상 28도 미만 (22도는 여기)
    val hot = current.temp >= 28 && current.temp < 35         // 28도 이상 35도 미만
    val veryHot = current.temp >= 35 && current.temp < 40     // 35도 이상 40도 미만
    val scorching = current.temp >= 40                        // 40도 이상

    // 습도 및 체감
    val veryHumid = current.humidity >= 85
    val humid = current.humidity >= 70
    val dryAir = current.humidity <= 30
    val veryDryAir = current.humidity <= 20
    val muggyDp = current.dew_point >= 25
    val stickyDp = current.dew_point >= 20
    val comfortableDp = current.dew_point in 10.0..16.0

    // 시정 및 구름
    val veryLowVis = current.visibility <= 500
    val lowVis = current.visibility in 501..1500
    val moderateVis = current.visibility in 1501..5000
    val clearSky = current.clouds <= 15
    val partlyCloudy = current.clouds in 25..75
    val overcast = current.clouds >= 85

    // 자외선
    val extremeUV = current.uvi >= 11
    val veryHighUV = current.uvi >= 8
    val highUV = current.uvi >= 6
    val moderateUV = current.uvi >= 3
    val strongUV = current.uvi >= 6

    // 기압
    val veryLowPressure = current.pressure <= 990
    val lowPressure = current.pressure <= 1005
    val highPressure = current.pressure >= 1020
    val veryHighPressure = current.pressure >= 1030

    val rangeToday = daily?.firstOrNull()?.let { it.temp.max - it.temp.min } ?: 0.0

    // ---- 단기 예보 ----
    val next3 = hourly.take(3)
    val next6 = hourly.take(6)
    val next12 = hourly.take(12)
    val next3MaxPop = next3.maxOfOrNull { it.pop } ?: 0.0
    val next6MaxPop = next6.maxOfOrNull { it.pop } ?: 0.0
    val next3RainSum = next3.sumOf { it.rain?.oneHour ?: 0.0 }
    val next6RainSum = next6.sumOf { it.rain?.oneHour ?: 0.0 }
    val next12RainSum = next12.sumOf { it.rain?.oneHour ?: 0.0 }
    val next3SnowSum = next3.sumOf { it.snow?.oneHour ?: 0.0 }
    val next6SnowSum = next6.sumOf { it.snow?.oneHour ?: 0.0 }
    val nextDryHourIndex = if (rainNow > 0) next6.indexOfFirst { it.pop < 0.2 } else -1
    val soonRainHourIndex = if (rainNow == 0.0) next6.indexOfFirst { it.pop >= 0.6 } else -1

    // ==========================
    // 🌡️ 온도 & 체감 (수정된 온도 범위에 맞춤)
    // ==========================
    if (extremeCold) {
        candidates += "극한 추위로 몇 분만 밖에 있어도 위험할 수 있어요."
        candidates += "체감온도가 실제보다 훨씬 낮아 동상 위험이 있어요."
        candidates += "모든 노출된 피부를 완전히 가려야 해요."
    }
    if (freezing) {
        candidates += "혹독한 추위로 야외 활동이 매우 어려워요."
        candidates += "자동차 시동이 잘 안 걸릴 수 있어요."
        candidates += "수도관 동파에 주의해야 해요."
    }
    if (veryCold) {
        candidates += "매우 추워 두꺼운 외투가 필수예요."
        candidates += "손발이 금세 시려워질 수 있어요."
        candidates += "따뜻한 음료를 준비해두는 게 좋아요."
    }
    if (cold) {
        candidates += "찬 공기로 호흡할 때 목이 아플 수 있어요."
        candidates += "겨울 코트와 장갑이 필요한 날씨예요."
    }
    if (chilly) {
        candidates += "쌀쌀해서 가벼운 외투가 필요해요."
        candidates += "아침저녁으로 더 쌀쌀해질 수 있어요."
    }
    if (cool) {
        candidates += "선선한 날씨로 산책하기 좋은 기온이에요."
        candidates += "바람이 불어 실제보다 더 시원하게 느껴져요."
    }
    if (mild) {
        candidates += "온화한 기온으로 야외 활동하기 좋아요."
        candidates += "산책하기 좋은 온도와 맑은 하늘이에요."
    }
    if (warm) {
        candidates += "기온이 따뜻해서 기분 좋은 날씨예요."
        candidates += "야외 활동하기 딱 좋은 따뜻한 기온이에요."
    }
    if (hot) {
        candidates += "한낮의 더위가 강해 외출 시 지치기 쉬워요."
        candidates += "그늘에서도 더위를 느낄 수 있어요."
    }
    if (veryHot) {
        candidates += "매우 더워 야외 활동 시 주의가 필요해요."
        candidates += "충분한 수분 섭취가 중요해요."
    }
    if (scorching) {
        candidates += "폭염 수준의 더위로 야외 활동을 피하세요."
        candidates += "열사병 위험이 높으니 시원한 곳에 머무르세요."
    }

    // 복합 온도 조건들
    if (warm && partlyCloudy) candidates += "기온은 따뜻하지만 구름이 있어 한결 편안해요."
    if (cool && windy) candidates += "바람이 불어 실제보다 더 쌀쌀하게 느껴져요."
    if (cold && veryHumid) candidates += "차갑고 습한 공기로 체감 추위가 더 심해요."
    if (cold && dryAir) candidates += "찬 바람과 건조한 공기로 피부가 건조해질 수 있어요."
    if (hot && windy) candidates += "바람이 불어 체감 더위는 약간 덜해요."
    if (hot && overcast) candidates += "구름이 많아 햇볕은 가려졌지만 공기는 여전히 무더워요."

    // ==========================
    // 💧 습도 & 이슬점
    // ==========================
    if (veryHumid && muggyDp) candidates += "무더위와 높은 습도로 불쾌지수가 매우 높아요."
    if (humid && stickyDp) candidates += "공기가 눅눅해 답답하게 느껴져요."
    if (veryHumid && !windy) candidates += "바람이 없어 습도가 더 크게 느껴져요."
    if (dryAir) candidates += "공기가 건조해 입술과 피부가 트기 쉬워요."
    if (dryAir && windy) candidates += "바람과 건조한 공기가 겹쳐 피부가 더 쉽게 건조해져요."
    if (veryHumid) candidates += "빨래가 잘 마르지 않을 수 있어요."

    // ==========================
    // 🌞 자외선
    // ==========================
    if (extremeUV) candidates += "자외선이 매우 강해 모자와 선크림이 꼭 필요해요."
    if (strongUV && clearSky) candidates += "구름이 없어 햇볕이 따갑게 내리쬐고 있어요."
    if (strongUV && overcast) candidates += "구름이 많아도 자외선은 강해요."

    // ==========================
    // 🌬️ 바람
    // ==========================
    if (extremeWind) {
        candidates += "매우 강한 바람으로 외출이 위험할 수 있어요."
        candidates += "간판이나 나뭇가지가 떨어질 위험이 있어요."
        candidates += "운전 시 핸들을 꽉 잡고 주의하세요."
    }
    if (veryWindy) {
        candidates += "강풍으로 우산 사용이 거의 불가능해요."
        candidates += "걸을 때 균형을 잡기 어려울 수 있어요."
        candidates += "먼지나 작은 물체들이 날아다녀요."
    }
    if (windy) candidates += "강한 바람이 불어 우산 사용이 불편할 수 있어요."
    if ((current.wind_gust ?: 0.0) >= 15) candidates += "돌풍이 거세게 불어 체감이 훨씬 강하게 느껴져요."
    if (windy && cold) candidates += "바람 때문에 체감 추위가 두드러져요."

    // ==========================
    // 🌫️ 시정 & 구름
    // ==========================
    if (veryLowVis) candidates += "짙은 안개로 앞이 거의 보이지 않아요."
    if (lowVis && overcast) candidates += "안개와 구름이 겹쳐 시야가 흐려요."
    if (clearSky) candidates += "하늘이 맑아 시야가 탁 트여 있어요."
    if (overcast) candidates += "하늘이 잔뜩 흐려져 있습니다."
    if (partlyCloudy) candidates += "구름이 드문드문 떠 있어요."

    // ==========================
    // 🌧️ 강수 & 적설
    // ==========================
    if (rainNow in 0.1..0.9) candidates += "보슬비가 내려 우산이 필요할 수 있어요."
    if (rainNow in 1.0..4.9) candidates += "약한 비가 이어지고 있어요."
    if (rainNow in 5.0..14.9) candidates += "비가 제법 내려 이동 시 불편할 수 있어요."
    if (rainNow >= 15.0) candidates += "시간당 폭우 수준의 강한 비가 내리고 있어요."
    if (snowNow > 0 && current.temp <= 0) candidates += "눈이 내려 길이 미끄럽습니다."

    // ==========================
    // 🕒 시간대 특화
    // ==========================
    when (bucket) {
        TimeBucket.MORNING -> {
            if (cool) candidates += "아침 공기가 선선해 상쾌해요."
            if (strongUV) candidates += "한낮엔 자외선이 강해질 예정이에요."
        }
        TimeBucket.DAYTIME -> {
            if (hot) candidates += "햇볕이 강해 금세 더워질 수 있어요."
        }
        TimeBucket.EVENING -> {
            if (rangeToday >= 10) candidates += "해가 지며 기온이 빠르게 내려가고 있어요."
        }
        TimeBucket.NIGHT -> {
            if (lowVis) candidates += "밤사이 안개로 시야가 짧아질 수 있어요."
        }
    }

    // ==========================
    // 📅 단기 예보 기반
    // ==========================
    if (rainNow > 0 && nextDryHourIndex in 0..2) {
        candidates += "잠시 후 비가 잦아들 전망이에요."
    }
    if (rainNow == 0.0 && soonRainHourIndex in 0..2) {
        candidates += "잠시 뒤 비 예보가 있어 우산을 챙기는 게 좋아요."
    }
    if (next3RainSum >= 10.0) candidates += "앞 몇 시간 동안 제법 많은 비가 올 수 있어요."
    if (next6RainSum >= 20.0) candidates += "오늘 누적 강수가 상당히 많아질 수 있어요."
    if (next3SnowSum >= 2.0) candidates += "짧은 시간에 눈이 굵어질 가능성이 있어요."

    // ==========================
    // 📈 기압
    // ==========================
    if (lowPressure) candidates += "기압이 낮아 날씨가 불안정할 수 있어요."
    if (highPressure && clearSky) candidates += "기압이 높아 하늘이 안정적으로 맑아요."

    // 🌅 일출/일몰 코멘트
    val now = current.dt
    val sunrise = current.sunrise
    val sunset = current.sunset

    if (now in (sunrise - 2 * 3600)..(sunrise + 1 * 3600)) {
        candidates += "오늘 해는 ${formatTime(sunrise)}에 떠요."
    }

    if (now in (sunset - 2 * 3600)..(sunset + 1 * 3600)) {
        candidates += "오늘 해는 ${formatTime(sunset)}에 져요."
    }

    // 3️⃣ 최종 랜덤 2개
    val picked = if (candidates.isNotEmpty()) {
        candidates.shuffled(Random(System.currentTimeMillis())).take(2)
    } else listOf("날씨 코멘트를 생성하지 못했어요.")

    return WeatherAdvice(out + picked)
}

// ---------------- 헬퍼 ----------------
private fun currentTimeBucket(cur: CurrentWeather): TimeBucket {
    val H2 = 2 * 3600L
    val H3 = 3 * 3600L
    return when {
        cur.dt < cur.sunrise -> TimeBucket.NIGHT
        cur.dt in cur.sunrise..(cur.sunrise + H3) -> TimeBucket.MORNING
        cur.dt in (cur.sunrise + H3)..(cur.sunset - H2) -> TimeBucket.DAYTIME
        cur.dt in (cur.sunset - H2)..(cur.sunset + H2) -> TimeBucket.EVENING
        else -> TimeBucket.NIGHT
    }
}
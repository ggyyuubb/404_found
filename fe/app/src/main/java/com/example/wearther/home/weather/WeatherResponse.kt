// 📁 model/WeatherResponse.kt
// ✅ OpenWeatherMap One Call API 3.0 응답(JSON)을 Kotlin 데이터 클래스로 매핑한 모델입니다.
// ✅ 현재 날씨(current), 시간별 예보(hourly), 일별 예보(daily)를 모두 포함합니다.

package com.example.wearther.home.weather

// ✅ 전체 응답 데이터 구조
data class WeatherResponse(
    val timezone: String, // 예: "Asia/Seoul"
    val current: CurrentWeather, // 현재 날씨 정보
    val hourly: List<HourlyWeather>, // 시간별 예보 리스트 (보통 48시간)
    val daily: List<DailyWeather>? // 일별 예보 리스트 (nullable → exclude 시 null 가능)
)

// ✅ 현재 날씨 정보
data class CurrentWeather(
    val dt: Long, // UTC 기준 시간 (timestamp)
    val temp: Double, // 현재 온도 (섭씨)
    val feels_like: Double, // 체감 온도
    val humidity: Int, // 습도 (%)
    val wind_speed: Double, // 풍속 (m/s)
    val clouds: Int, // 구름 양 (%)
    val weather: List<WeatherInfo>, // 날씨 설명 및 아이콘 정보 (리스트로 감싸져 있음)
    val rain: Map<String, Double>? = null // 강수량 정보 (예: {"1h": 0.5}), 없을 수 있음
)

// ✅ 시간별 날씨 예보 (보통 48개 항목)
data class HourlyWeather(
    val dt: Long, // 예보 시간 (timestamp)
    val temp: Double, // 예보 시점의 온도
    val feels_like: Double, // 체감 온도
    val humidity: Int, // 습도
    val wind_speed: Double, // 풍속
    val clouds: Int, // 구름 양
    val weather: List<WeatherInfo>, // 날씨 설명
    val pop: Double, // 강수 확률 (0.0 ~ 1.0)
    val rain: Map<String, Double>? = null, // 시간별 강수량
    val snow: Map<String, Double>? = null // 시간별 적설량 (있을 수 있음)
)

// ✅ 일별 날씨 예보 (보통 7일치)
data class DailyWeather(
    val dt: Long, // 날짜 (timestamp)
    val temp: Temp, // 하루 온도 정보
    val weather: List<WeatherInfo>, // 날씨 설명
    val pop: Double, // 강수 확률
    val rain: Map<String, Double>? = null // 일간 강수량 정보
)

// ✅ 하루 중 온도 정보
data class Temp(
    val min: Double, // 최저 기온
    val max: Double, // 최고 기온
    val day: Double // 낮 시간대 평균 기온
)

// ✅ 날씨 설명 정보 (현재, 시간별, 일별에서 공통 사용)
data class WeatherInfo(
    val main: String, // 간단한 분류 (예: "Rain", "Clear")
    val description: String, // 상세 설명 (예: "light rain")
    val icon: String // 아이콘 코드 (예: "10d", "01n") → https://openweathermap.org/weather-conditions 참조
)

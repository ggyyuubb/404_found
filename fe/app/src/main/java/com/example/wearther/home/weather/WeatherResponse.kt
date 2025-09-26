// 📁 model/WeatherResponse.kt
// ✅ OpenWeatherMap One Call API 3.0 응답(JSON)을 Kotlin 데이터 클래스로 매핑한 모델입니다.

package com.example.wearther.home.weather

import com.google.gson.annotations.SerializedName

// ✅ 전체 응답 데이터 구조
data class WeatherResponse(
    val timezone: String, // 예: "Asia/Seoul"
    val current: CurrentWeather, // 현재 날씨 정보
    val hourly: List<HourlyWeather>, // 시간별 예보 리스트 (보통 48시간)
    val daily: List<DailyWeather>?, // 일별 예보 리스트 (nullable → exclude 시 null 가능)
    val alerts: List<WeatherAlert>? = null // ✅ 기상 특보
)

// ✅ 현재 날씨 정보
data class CurrentWeather(
    val dt: Long, // UTC 기준 시간 (timestamp)
    val sunrise: Long, // ✅ 일출 시간
    val sunset: Long, // ✅ 일몰 시간
    val temp: Double, // 현재 온도 (섭씨)
    val feels_like: Double, // 체감 온도
    val pressure: Int, // ✅ 기압 (hPa)
    val humidity: Int, // 습도 (%)
    val dew_point: Double, // ✅ 이슬점 (°C)
    val uvi: Double, // ✅ 자외선 지수
    val clouds: Int, // 구름 양 (%)
    val visibility: Int, // ✅ 가시거리 (m)
    val wind_speed: Double, // 풍속 (m/s)
    val wind_deg: Int, // ✅ 풍향 (도)
    val wind_gust: Double? = null, // ✅ 돌풍 (있을 수 있음)
    val weather: List<WeatherInfo>, // 날씨 설명 및 아이콘 정보 (리스트)
    @SerializedName("rain") val rain: RainInfo? = null, // 강수량 정보
    @SerializedName("snow") val snow: RainInfo? = null  // ✅ 적설량 정보
)

// ✅ 시간별 날씨 예보 (보통 48개 항목)
data class HourlyWeather(
    val dt: Long, // 예보 시간 (timestamp)
    val temp: Double, // 예보 시점의 온도
    val feels_like: Double, // 체감 온도
    val pressure: Int, // ✅ 기압 (hPa)
    val humidity: Int, // 습도
    val dew_point: Double, // ✅ 이슬점 (°C)
    val uvi: Double, // ✅ 자외선 지수
    val clouds: Int, // 구름 양
    val visibility: Int, // ✅ 가시거리 (m)
    val wind_speed: Double, // 풍속
    val wind_deg: Int, // ✅ 풍향 (도)
    val wind_gust: Double? = null, // ✅ 돌풍
    val weather: List<WeatherInfo>, // 날씨 설명
    val pop: Double, // 강수 확률 (0.0 ~ 1.0)
    @SerializedName("rain") val rain: RainInfo? = null, // 시간별 강수량
    @SerializedName("snow") val snow: RainInfo? = null  // 시간별 적설량
)

// ✅ 일별 날씨 예보 (보통 7일치)
data class DailyWeather(
    val dt: Long, // 날짜 (timestamp)
    val temp: Temp, // 하루 온도 정보
    val weather: List<WeatherInfo>, // 날씨 설명
    val pop: Double, // 강수 확률
    @SerializedName("rain") val rain: Double? = null // API에서 숫자로 오는 경우 있음
)

// ✅ 하루 중 온도 정보
data class Temp(
    val min: Double, // 최저 기온
    val max: Double, // 최고 기온
    val day: Double  // 낮 시간대 평균 기온
)

// ✅ 강수량/적설량 정보 (현재·시간별 공통)
data class RainInfo(
    @SerializedName("1h") val oneHour: Double? = null
)

// ✅ 날씨 설명 정보 (현재, 시간별, 일별에서 공통 사용)
data class WeatherInfo(
    val main: String, // 간단한 분류 (예: "Rain", "Clear")
    val description: String, // 상세 설명 (예: "light rain")
    val icon: String // 아이콘 코드 (예: "10d", "01n")
)

// ✅ 기상 특보 정보
data class WeatherAlert(
    val sender_name: String, // 특보 발령 기관 (예: 기상청)
    val event: String, // 특보 종류 (예: Heatwave Warning)
    val start: Long, // 시작 시간 (timestamp)
    val end: Long, // 종료 시간 (timestamp)
    val description: String // 상세 설명
)

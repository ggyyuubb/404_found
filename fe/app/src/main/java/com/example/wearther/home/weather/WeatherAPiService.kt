// 📁 network/WeatherApiService.kt
// ✅ OpenWeatherMap의 One Call API 3.0 버전을 사용해 날씨 데이터를 요청하는 Retrofit 인터페이스입니다.
// ✅ 이 API는 현재 날씨(current), 시간별 예보(hourly), 주간 예보(daily)를 포함하며, 불필요한 항목(minutely, alerts)만 제외합니다.

package com.example.wearther.home.weather

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    /**
     * ✅ 위도(lat), 경도(lon)를 기준으로 날씨 데이터를 요청합니다.
     * - API 버전: One Call API 3.0 (`data/3.0/onecall`)
     * - 응답 데이터는 WeatherResponse 형태로 받아옵니다.
     * - suspend 함수이므로 Coroutine 내에서 호출 가능
     *
     * @param lat 위도
     * @param lon 경도
     * @param apiKey OpenWeatherMap에서 발급받은 개인 API 키 (기본값 설정됨)
     * @param units 온도 단위: "metric"은 섭씨
     * @param exclude 제외할 항목: "minutely", "alerts" (daily는 포함하여 주간 예보 데이터 수신)
     *
     * @return 날씨 정보를 담은 HTTP Response<WeatherResponse>
     */
    @GET("data/3.0/onecall")
    suspend fun getWeather(
        @Query("lat") lat: Double, // ✅ 위도
        @Query("lon") lon: Double, // ✅ 경도
        @Query("appid") apiKey: String = "9f77037105f413b870f9c9f2c1a2fb32", // ✅ API 키 (기본값)
        @Query("units") units: String = "metric", // ✅ 섭씨 기준
        @Query("exclude") exclude: String = "minutely,alerts" // ✅ daily 제외하지 않음 → 주간 예보 포함!
    ): Response<WeatherResponse>
}
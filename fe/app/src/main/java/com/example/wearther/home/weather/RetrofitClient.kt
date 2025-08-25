// 📁 network/RetrofitClient.kt
// ✅ OpenWeatherMap API에 연결하기 위한 Retrofit 클라이언트를 생성하고,
//    WeatherApiService 인터페이스를 통해 API 요청을 수행할 수 있도록 제공합니다.
// ✅ GeocodingApi도 추가하여 위치 검색 기능을 지원합니다.

package com.example.wearther.home.weather

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // ✅ OpenWeatherMap API의 기본 URL (v3 기준 도메인, 엔드포인트는 WeatherApiService에 정의됨)
    private const val BASE_URL = "https://api.openweathermap.org/"

    // ✅ 싱글톤 Retrofit 인스턴스 생성
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // API 서버 주소 설정
            .addConverterFactory(GsonConverterFactory.create()) // JSON -> 객체 변환기
            .build()
    }

    // ✅ 기존 날씨 API 서비스
    val apiService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java) // WeatherApiService 인터페이스 구현체 생성
    }

    // ✅ 새로 추가: 위치 검색을 위한 Geocoding API 서비스
    val geocodingApiService: GeocodingApi by lazy {
        retrofit.create(GeocodingApi::class.java) // GeocodingApi 인터페이스 구현체 생성
    }
}
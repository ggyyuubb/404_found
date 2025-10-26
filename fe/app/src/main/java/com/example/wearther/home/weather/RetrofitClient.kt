// 📁 network/RetrofitClient.kt
// ✅ OpenWeatherMap API에 연결하기 위한 Retrofit 클라이언트를 생성하고,
//    WeatherApiService 인터페이스를 통해 API 요청을 수행할 수 있도록 제공합니다.
// ✅ GeocodingApi도 추가하여 위치 검색 기능을 지원합니다.
// ✅ 백엔드 서버 연결을 위한 ClothingRecommendationApiService도 추가합니다.

package com.example.wearther.home.weather

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // ✅ OpenWeatherMap API의 기본 URL (v3 기준 도메인, 엔드포인트는 WeatherApiService에 정의됨)
    private const val WEATHER_BASE_URL = "https://api.openweathermap.org/"

    // 👇 백엔드 서버 URL (Flask 서버 - 실제 서버 주소로 변경 필요!)
    private const val BACKEND_BASE_URL = "http://54.174.194.52:5000/"

    // ✅ OpenWeather용 Retrofit 인스턴스 생성
    private val weatherRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL) // OpenWeather API 서버 주소
            .addConverterFactory(GsonConverterFactory.create()) // JSON -> 객체 변환기
            .build()
    }

    // 👇 백엔드용 Retrofit 인스턴스 생성
    private val backendRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL) // 백엔드 서버 주소
            .addConverterFactory(GsonConverterFactory.create()) // JSON -> 객체 변환기
            .build()
    }

    // ✅ 기존 날씨 API 서비스
    val apiService: WeatherApiService by lazy {
        weatherRetrofit.create(WeatherApiService::class.java)
    }

    // ✅ 위치 검색을 위한 Geocoding API 서비스
    val geocodingApiService: GeocodingApi by lazy {
        weatherRetrofit.create(GeocodingApi::class.java)
    }

    // 👇 백엔드 옷 추천 API 서비스 추가
    val clothingRecommendationService: ClothingRecommendationApiService by lazy {
        backendRetrofit.create(ClothingRecommendationApiService::class.java)
    }
}
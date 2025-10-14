package com.example.wearther.home.recommendation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearther.remote.BASE_URL
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.ConnectException
import javax.net.ssl.SSLHandshakeException


class RecommendationViewModel : ViewModel() {

    private val _recommendations = MutableStateFlow<List<String>>(emptyList())
    val recommendations: StateFlow<List<String>> = _recommendations

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    private val _temp = MutableStateFlow(0.0)
    val temp: StateFlow<Double> = _temp.asStateFlow()

    private val _weatherCode = MutableStateFlow(0)
    val weatherCode: StateFlow<Int> = _weatherCode.asStateFlow()

    private val _response = MutableStateFlow<RecommendationResponse?>(null)
    val response: StateFlow<RecommendationResponse?> = _response.asStateFlow()

    val isLoading: StateFlow<Boolean> = _loading

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor { msg -> Log.d("Net", msg) }
                .apply { level = HttpLoggingInterceptor.Level.BODY }
        )
        .build()

    fun fetchRecommendations(jwt: String, city: String) {
        val t0 = System.currentTimeMillis()

        viewModelScope.launch(Dispatchers.IO) {
            val safeCity = if (city.isBlank()) "Seoul" else city
            Log.d("RecoVM", "🚀 fetchRecommendations 시작 - city=$safeCity, jwt=${jwt.take(10)}...")

            _loading.value = true
            _errorMessage.value = ""

            try {
                // ✅ 요청 body
                val requestBody = gson.toJson(mapOf("city" to safeCity))
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("${BASE_URL}api/recommend/recommendations/ai") // ✅ 수정
                    .addHeader("Authorization", "Bearer $jwt")
                    .post(requestBody)
                    .build()

                Log.d("RecoVM", "🌍 요청 URL: ${request.url}")
                Log.d("RecoVM", "🔑 요청 헤더: Authorization=Bearer ${jwt.take(10)}...")
                Log.d("RecoVM", "📦 요청 Body: $safeCity")

                val response = client.newCall(request).execute()


                if (response.isSuccessful) {
                    val body = response.body?.string()
                    Log.d("RecoVM", "✅ 서버 응답: $body")

                    val recommendationResponse =
                        gson.fromJson(body, RecommendationResponse::class.java)

                    _response.value = recommendationResponse
                    _temp.value = recommendationResponse.temp.toDouble()
                    _weatherCode.value = recommendationResponse.weather_code

                    val outfits = mutableListOf<String>()
                    recommendationResponse.getTop()?.url?.let { outfits.add(it) }
                    recommendationResponse.getBottom()?.url?.let { outfits.add(it) }
                    recommendationResponse.getOuter()?.url?.let { outfits.add(it) }
                    _recommendations.value = outfits

                    Log.d("RecoVM", "🎉 추천 파싱 완료")
                } else {
                    val errorBody = response.body?.string()
                    val errorMsg = "API 응답 실패: code=${response.code}, body=$errorBody"
                    _errorMessage.value = errorMsg
                    Log.e("RecoVM", "❌ $errorMsg")
                }
            } catch (e: SocketTimeoutException) {
                val dt = System.currentTimeMillis() - t0
                _errorMessage.value = "TIMEOUT"
                Log.e("RecoVM", "TIMEOUT after ${dt}ms: ${e.message}")
            } catch (e: UnknownHostException) {
                _errorMessage.value = "DNS_FAIL"
                Log.e("RecoVM", "DNS_FAIL: ${e.message}")
            } catch (e: ConnectException) {
                _errorMessage.value = "CONNECT_FAIL"
                Log.e("RecoVM", "CONNECT_FAIL: ${e.message}")
            } catch (e: SSLHandshakeException) {
                _errorMessage.value = "TLS_FAIL"
                Log.e("RecoVM", "TLS_FAIL: ${e.message}")
            }
            }
    }

    // 🔹 새로 추가된 sendFeedback 함수
    fun sendFeedback(jwt: String, location: String, isPositive: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("RecoVM", "📤 피드백 전송 시작 - location=$location, isPositive=$isPositive")

            try {
                val requestBody = gson.toJson(mapOf(
                    "location" to location,
                    "isPositive" to isPositive
                )).toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("${BASE_URL}api/recommend/feedback") // 피드백 API 엔드포인트 (실제 엔드포인트에 맞게 수정)
                    .addHeader("Authorization", "Bearer $jwt")
                    .post(requestBody)
                    .build()

                Log.d("RecoVM", "🌍 피드백 요청 URL: ${request.url}")
                Log.d("RecoVM", "📦 피드백 Body: location=$location, isPositive=$isPositive")

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d("RecoVM", "✅ 피드백 전송 성공")
                } else {
                    val errorBody = response.body?.string()
                    Log.e("RecoVM", "❌ 피드백 전송 실패: code=${response.code}, body=$errorBody")
                }
            } catch (e: Exception) {
                Log.e("RecoVM", "❌ 피드백 전송 중 오류: ${e.message}", e)
            }
        }
    }

    fun clearRecommendations() {
        Log.d("RecoVM", "🗑️ 추천 데이터 초기화")
        _recommendations.value = emptyList()
        _response.value = null
        _errorMessage.value = ""
        _temp.value = 0.0
        _weatherCode.value = 0
    }
}
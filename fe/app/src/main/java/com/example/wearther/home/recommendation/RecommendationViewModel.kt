package com.example.wearther.home.recommendation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import com.google.gson.Gson
import com.google.gson.JsonElement

class RecommendationViewModel : ViewModel() {


    // 🔹 전체 응답 그대로 보관 (getTop/getBottom/getOuter 사용용)
    private val _response = MutableStateFlow<RecommendationResponse?>(null)
    val response: StateFlow<RecommendationResponse?> = _response

    // 🔹 에러 메시지 상태
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // 🔹 현재 온도 / 날씨 코드
    private val _temp = MutableStateFlow<Int?>(null)
    val temp: StateFlow<Int?> = _temp

    private val _weatherCode = MutableStateFlow<Int?>(null)
    val weatherCode: StateFlow<Int?> = _weatherCode

    // 🔸 추천 요청 함수
    fun fetchRecommendations(token: String, city: String = "Seoul") {
        viewModelScope.launch {
            try {
                Log.d("RECOMMEND_REQUEST", "요청 시작: 도시 = $city / 토큰 = $token")
                val result = RecommendationService.api.getRecommendation(
                    "Bearer $token",
                    RecommendationRequest(city = city)
                )

                _errorMessage.value = null

                // 🔸 응답에서 온도, 날씨코드 저장
                _temp.value = result.temp
                _weatherCode.value = result.weather_code

                // 🔸 전체 응답 저장 (HomeBottomSheet에서 getTop/Bottom/Outer 쓰기 위함)
                _response.value = result

            } catch (e: HttpException) {
                Log.e("RECOMMEND_RESPONSE", "❌ HTTP 예외 발생: ${e.message}")
                _errorMessage.value = "서버와의 통신 오류"
            } catch (e: IOException) {
                Log.e("RECOMMEND_RESPONSE", "❌ 네트워크 오류 발생: ${e.message}")
                _errorMessage.value = "네트워크 오류"
            } catch (e: Exception) {
                Log.e("RECOMMEND_RESPONSE", "❌ 예외 발생: ${e.message}")
                _errorMessage.value = "알 수 없는 오류"
            }
        }
    }
}

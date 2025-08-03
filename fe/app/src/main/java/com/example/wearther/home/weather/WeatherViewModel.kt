// 📁 viewmodel/WeatherViewModel.kt
// ✅ 현재 위치 기반 날씨 정보를 가져오고, 위치를 주소(도시명 등)로 변환하여 상태로 관리하는 ViewModel입니다.
// ✅ 날씨 API와 Geocoder를 이용해 데이터를 불러오며, 상태는 StateFlow로 Compose에서 관찰할 수 있도록 구성되어 있습니다.

package com.example.wearther.home.weather

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    // ✅ 날씨 정보를 담는 상태 변수 (null이면 로딩 중이거나 실패한 상태)
    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData

    // ✅ 위치 주소를 담는 상태 변수 (예: 서울 강남구 삼성동)
    private val _locationText = MutableStateFlow("")
    val locationText: StateFlow<String> = _locationText

    /**
     * ✅ 위도와 경도를 기반으로 날씨 정보를 서버에서 가져옵니다.
     * - RetrofitClient를 통해 API 호출
     * - 성공하면 StateFlow에 데이터 저장
     * - daily 데이터가 null이거나 비어 있으면 로그 출력 (UI에서도 처리 필요)
     */
    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getWeather(lat, lon)
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        if (body.daily.isNullOrEmpty()) {
                            Log.e("WeatherVM", "❗ daily 예보가 없습니다. UI에서도 안전하게 표시됨")
                        }
                        _weatherData.value = body
                    }
                }
            } catch (e: Exception) {
                Log.e("WeatherVM", "API 호출 오류: ${e.message}")
            }
        }
    }

    /**
     * ✅ 위도와 경도를 기반으로 실제 주소(시/구/동)를 문자열로 변환하여 상태에 저장합니다.
     * - Android Geocoder 사용
     * - 예: "서울 강남구 삼성동" 형식으로 구성
     * - 변환 실패 시 기본 메시지 설정
     */
    fun fetchAddress(context: Context, lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val geocoder = Geocoder(context, java.util.Locale.KOREA)
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val city = addresses[0].adminArea ?: ""
                    val district = addresses[0].subLocality ?: ""
                    val town = addresses[0].thoroughfare ?: ""
                    _locationText.value = "$city $district $town".trim()
                } else {
                    _locationText.value = "주소를 찾을 수 없습니다"
                }
            } catch (e: Exception) {
                Log.e("WeatherVM", "주소 변환 실패: ${e.localizedMessage}")
            }
        }
    }

    /**
     * ✅ 날씨 데이터를 초기화합니다.
     * - 새로고침 또는 위치 변경 시 호출
     */
    fun clearWeather() {
        _weatherData.value = null
    }
}

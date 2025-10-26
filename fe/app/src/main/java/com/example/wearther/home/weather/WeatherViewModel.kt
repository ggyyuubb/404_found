// 📁 viewmodel/WeatherViewModel.kt

package com.example.wearther.home.weather

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WeatherViewModel(
    private val context: Context
) : ViewModel() {

    // ✅ 날씨 정보를 담는 상태 변수 (null이면 로딩 중이거나 실패한 상태)
    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData

    // ✅ 위치 주소를 담는 상태 변수 (예: 서울 강남구 삼성동)
    private val _locationText = MutableStateFlow("")
    val locationText: StateFlow<String> = _locationText

    // ✅ 저장된 위치들을 담는 상태 변수
    private val _savedLocations = MutableStateFlow<List<SavedLocation>>(emptyList())
    val savedLocations: StateFlow<List<SavedLocation>> = _savedLocations.asStateFlow()

    // 👇 옷 추천 상태 추가
    private val _clothingRecommendation = MutableStateFlow<ClothingRecommendationResponse?>(null)
    val clothingRecommendation: StateFlow<ClothingRecommendationResponse?> = _clothingRecommendation.asStateFlow()

    // 👇 옷 추천 로딩 상태
    private val _isLoadingRecommendation = MutableStateFlow(false)
    val isLoadingRecommendation: StateFlow<Boolean> = _isLoadingRecommendation.asStateFlow()

    // 👇 현재 위치 저장 (옷 추천 요청 시 사용)
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    // ✅ 위치 관련 Repository들
    private val locationRepository = LocationRepository(context)
    private val locationSearchRepository = LocationSearchRepository(
        geocodingApi = RetrofitClient.geocodingApiService,
        apiKey = "9f77037105f413b870f9c9f2c1a2fb32"
    )

    // ✅✅ 커뮤니티에서 사용할 현재 온도 (간단한 문자열)
    val currentTemperature: StateFlow<String> = weatherData.map {
        it?.current?.temp?.toInt()?.toString() ?: ""
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), "")

    // ✅✅ 커뮤니티에서 사용할 현재 날씨 (한글 변환)
    val currentWeather: StateFlow<String> = weatherData.map {
        it?.current?.weather?.firstOrNull()?.main?.let { main ->
            when(main.lowercase()) {
                "clear" -> "맑음"
                "clouds" -> "흐림"
                "rain" -> "비"
                "snow" -> "눈"
                "thunderstorm" -> "천둥번개"
                "drizzle" -> "이슬비"
                "mist", "fog" -> "안개"
                else -> main
            }
        } ?: ""
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), "")

    init {
        loadSavedLocations()
    }

    /**
     * ✅ 위도와 경도를 기반으로 날씨 정보를 서버에서 가져옵니다.
     * 👉 수정됨: 날씨 조회 성공 시 자동으로 옷 추천도 요청합니다.
     */
    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                // 위치 저장
                currentLatitude = lat
                currentLongitude = lon

                Log.d("WeatherVM", "🌡️ 날씨 조회 시작: lat=$lat, lon=$lon")

                val response = RetrofitClient.apiService.getWeather(lat, lon)
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        if (body.daily.isNullOrEmpty()) {
                            Log.e("WeatherVM", "❗ daily 예보가 없습니다. UI에서도 안전하게 표시됨")
                        }
                        _weatherData.value = body

                        Log.d("WeatherVM", "✅ 날씨 조회 성공")
                        Log.d("WeatherVM", "   - 온도: ${body.current.temp}°C")
                        Log.d("WeatherVM", "   - 날씨: ${body.current.weather.firstOrNull()?.main}")

                        // 👇 날씨 조회 성공하면 바로 옷 추천 요청
                        fetchClothingRecommendation(body, _locationText.value)
                    }
                } else {
                    Log.e("WeatherVM", "❌ 날씨 조회 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("WeatherVM", "API 호출 오류: ${e.message}")
            }
        }
    }

    /**
     * 👇 새로 추가: 옷 추천 요청
     * 날씨 데이터 + 위치 정보를 백엔드로 전송하여 AI 옷 추천을 받습니다.
     */
    private fun fetchClothingRecommendation(
        weatherData: WeatherResponse,
        locationName: String
    ) {
        viewModelScope.launch {
            _isLoadingRecommendation.value = true

            try {
                Log.d("WeatherVM", "👔 옷 추천 요청 시작")

                // 기존 WeatherResponse에서 필요한 데이터만 추출
                val weatherInfo = WeatherInfoForBackend(
                    temp = weatherData.current.temp,
                    feelsLike = weatherData.current.feels_like,
                    humidity = weatherData.current.humidity,
                    weather = weatherData.current.weather.firstOrNull()?.main ?: "Unknown",
                    description = weatherData.current.weather.firstOrNull()?.description ?: "",
                    windSpeed = weatherData.current.wind_speed,
                    timestamp = System.currentTimeMillis().toString()
                )

                val request = ClothingRecommendationRequest(
                    location = locationName,
                    weather = weatherInfo,
                    day_index = 0  // 오늘 기준
                )

                Log.d("WeatherVM", "📤 요청 데이터:")
                Log.d("WeatherVM", "   - 위치: $locationName")
                Log.d("WeatherVM", "   - 온도: ${weatherInfo.temp}°C")
                Log.d("WeatherVM", "   - 날씨: ${weatherInfo.weather}")

                // JWT 토큰 가져오기
                val token = getJwtToken()

                if (token.isNullOrEmpty()) {
                    Log.e("WeatherVM", "❌ JWT 토큰 없음")
                    _clothingRecommendation.value = null
                    return@launch
                }

                // 백엔드에 옷 추천 요청
                val response = RetrofitClient.clothingRecommendationService
                    .getRecommendation(request, "Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { result ->
                        _clothingRecommendation.value = result
                        Log.d("WeatherVM", "✅ 옷 추천 성공!")
                        Log.d("WeatherVM", "   - 상의: ${result.recommended?.top}")
                        Log.d("WeatherVM", "   - 하의: ${result.recommended?.bottom}")
                        Log.d("WeatherVM", "   - 아우터: ${result.recommended?.outer}")
                    }
                } else {
                    Log.e("WeatherVM", "❌ 옷 추천 실패: ${response.code()}")
                    Log.e("WeatherVM", "   - 에러: ${response.errorBody()?.string()}")
                    _clothingRecommendation.value = null
                }

            } catch (e: Exception) {
                Log.e("WeatherVM", "💥 옷 추천 요청 오류: ${e.message}")
                e.printStackTrace()
                _clothingRecommendation.value = null
            } finally {
                _isLoadingRecommendation.value = false
            }
        }
    }

    /**
     * 👇 JWT 토큰 가져오기 (Firebase Auth 사용)
     */
    private suspend fun getJwtToken(): String? {
        return try {
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            user?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Log.e("WeatherVM", "토큰 가져오기 실패: ${e.message}")
            null
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
                _locationText.value = "주소 변환 실패"
            }
        }
    }

    /**
     * ✅ 날씨 데이터를 초기화합니다.
     * 👉 수정됨: 옷 추천 데이터도 함께 초기화합니다.
     */
    fun clearWeather() {
        _weatherData.value = null
        _clothingRecommendation.value = null  // 👈 추가
    }

    // ==================== 위치 선택 기능 ====================

    /**
     * ✅ 저장된 위치 목록을 불러옵니다.
     */
    private fun loadSavedLocations() {
        _savedLocations.value = locationRepository.getSavedLocations()
    }

    /**
     * ✅ 위치를 검색합니다. (오픈웨더 Geocoding API 사용)
     */
    suspend fun searchLocations(query: String): List<SavedLocation> {
        return try {
            locationSearchRepository.searchLocations(query)
        } catch (e: Exception) {
            Log.e("WeatherVM", "위치 검색 실패: ${e.message}")
            emptyList()
        }
    }

    /**
     * ✅ 새 위치를 저장합니다.
     */
    fun addLocation(location: SavedLocation) {
        locationRepository.saveLocation(location)
        loadSavedLocations()
        Log.d("WeatherVM", "새 위치 저장됨: ${location.name}")
    }

    /**
     * ✅ 저장된 위치를 삭제합니다.
     */
    fun deleteLocation(locationId: String) {
        locationRepository.removeLocation(locationId)
        loadSavedLocations()
        Log.d("WeatherVM", "위치 삭제됨: ID = $locationId")
    }

    /**
     * ✅ 위치를 선택하여 해당 위치의 날씨를 불러옵니다.
     * 👉 수정됨: 날씨 조회 후 자동으로 옷 추천도 업데이트됩니다.
     */
    fun selectLocation(location: SavedLocation) {
        viewModelScope.launch {
            if (location.isCurrentLocation) {
                // 현재 위치로 변경
                try {
                    getCurrentLocation(context)?.let { currentLocation ->
                        fetchWeather(currentLocation.latitude, currentLocation.longitude)
                        fetchAddress(context, currentLocation.latitude, currentLocation.longitude)
                        Log.d("WeatherVM", "현재 위치로 변경됨")
                    }
                } catch (e: Exception) {
                    Log.e("WeatherVM", "현재 위치 가져오기 실패: ${e.message}")
                }
            } else {
                // 저장된 위치로 변경
                fetchWeather(location.latitude, location.longitude)
                _locationText.value = location.name
                Log.d("WeatherVM", "저장된 위치로 변경됨: ${location.name}")
            }
        }
    }

    /**
     * ✅ 현재 위치를 기반으로 날씨와 주소를 모두 가져옵니다.
     * 👉 수정됨: 날씨 조회 후 자동으로 옷 추천도 업데이트됩니다.
     */
    fun fetchCurrentLocationWeather() {
        viewModelScope.launch {
            try {
                getCurrentLocation(context)?.let { location ->
                    fetchWeather(location.latitude, location.longitude)
                    fetchAddress(context, location.latitude, location.longitude)
                }
            } catch (e: Exception) {
                Log.e("WeatherVM", "현재 위치 날씨 가져오기 실패: ${e.message}")
            }
        }
    }
}
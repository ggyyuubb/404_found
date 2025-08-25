// GeocodingApi.kt - 한국어 지원 오픈웨더 Geocoding API 연동
package com.example.wearther.home.weather

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// Geocoding API 응답 데이터 클래스
data class GeocodingResponse(
    val name: String,
    val local_names: Map<String, String>? = null,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
)

// Geocoding API 인터페이스 - 한국어 지원
interface GeocodingApi {
    @GET("geo/1.0/direct")
    suspend fun searchLocationByName(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("lang") lang: String = "kr", // 👈 한국어 기본값
        @Query("appid") apiKey: String
    ): Response<List<GeocodingResponse>>

    @GET("geo/1.0/reverse")
    suspend fun getLocationByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("limit") limit: Int = 1,
        @Query("lang") lang: String = "kr", // 👈 한국어 기본값
        @Query("appid") apiKey: String
    ): Response<List<GeocodingResponse>>
}

// 위치 저장소 (SharedPreferences 래퍼)
class LocationRepository(private val context: android.content.Context) {
    private val prefs = context.getSharedPreferences("saved_locations", android.content.Context.MODE_PRIVATE)
    private val gson = com.google.gson.Gson()

    fun getSavedLocations(): List<SavedLocation> {
        val locationsJson = prefs.getString("locations", "[]")
        val locationsArray = gson.fromJson(locationsJson, Array<SavedLocation>::class.java)
        val savedLocations = locationsArray?.toList() ?: emptyList()

        // 현재 위치는 항상 첫 번째에 위치
        return listOf(
            SavedLocation("current", "현재 위치", 0.0, 0.0, true)
        ) + savedLocations
    }

    fun saveLocation(location: SavedLocation) {
        val currentLocations = getSavedLocations().filter { !it.isCurrentLocation }
        val updatedLocations = currentLocations + location
        val locationsJson = gson.toJson(updatedLocations)
        prefs.edit().putString("locations", locationsJson).apply()
    }

    fun deleteLocation(locationId: String) {
        val currentLocations = getSavedLocations().filter {
            !it.isCurrentLocation && it.id != locationId
        }
        val locationsJson = gson.toJson(currentLocations)
        prefs.edit().putString("locations", locationsJson).apply()
    }

    // 호환성을 위해 유지
    fun removeLocation(locationId: String) {
        deleteLocation(locationId)
    }
}

// 위치 검색 Repository - 한국어 지원 완성판
class LocationSearchRepository(
    private val geocodingApi: GeocodingApi,
    private val apiKey: String
) {
    suspend fun searchLocations(query: String): List<SavedLocation> {
        return try {
            // lang=kr 파라미터로 한국어 지원
            val response = geocodingApi.searchLocationByName(query, 5, "kr", apiKey)
            if (response.isSuccessful) {
                response.body()?.map { geocoding ->
                    SavedLocation(
                        id = "${geocoding.lat}_${geocoding.lon}",
                        name = buildLocationName(geocoding),
                        latitude = geocoding.lat,
                        longitude = geocoding.lon,
                        isCurrentLocation = false
                    )
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun buildLocationName(geocoding: GeocodingResponse): String {
        // 한국어 이름 우선 사용
        val koreanName = geocoding.local_names?.get("kr")

        return when {
            // 🇰🇷 한국 지역 + 한국어 이름 있음
            geocoding.country == "KR" && !koreanName.isNullOrBlank() -> {
                when {
                    geocoding.state != null -> "$koreanName, ${geocoding.state}, 대한민국"
                    else -> "$koreanName, 대한민국"
                }
            }
            // 🇰🇷 한국 지역 + 한국어 이름 없음 (영어 이름 사용)
            geocoding.country == "KR" -> {
                when {
                    geocoding.state != null -> "${geocoding.name}, ${geocoding.state}, 대한민국"
                    else -> "${geocoding.name}, 대한민국"
                }
            }
            // 🌍 해외 지역 (영어 유지)
            else -> {
                when {
                    geocoding.state != null -> "${geocoding.name}, ${geocoding.state}, ${geocoding.country}"
                    else -> "${geocoding.name}, ${geocoding.country}"
                }
            }
        }
    }

    // 역지오코딩도 한국어 지원
    suspend fun getLocationByCoordinates(lat: Double, lon: Double): String? {
        return try {
            val response = geocodingApi.getLocationByCoordinates(lat, lon, 1, "kr", apiKey)
            if (response.isSuccessful) {
                response.body()?.firstOrNull()?.let { geocoding ->
                    buildLocationName(geocoding)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
// LocationUtils.kt - 위치 관련 유틸리티 함수들
package com.example.wearther.home.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

data class LocationData(
    val latitude: Double,
    val longitude: Double
)

/**
 * 위치 권한이 있는지 확인합니다.
 */
fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * 현재 위치를 가져옵니다.
 * 권한이 없거나 위치를 가져올 수 없으면 null을 반환합니다.
 */
fun getCurrentLocation(context: Context): LocationData? {
    if (!hasLocationPermission(context)) {
        return null
    }

    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // GPS 또는 네트워크 위치 제공자 중 사용 가능한 것을 찾기
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null

        for (provider in providers) {
            val location = locationManager.getLastKnownLocation(provider)
            if (location != null) {
                if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                    bestLocation = location
                }
            }
        }

        return bestLocation?.let {
            LocationData(it.latitude, it.longitude)
        }

    } catch (e: SecurityException) {
        // 권한이 없는 경우
        return null
    } catch (e: Exception) {
        // 기타 오류
        return null
    }
}
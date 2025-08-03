package com.example.wearther.home.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


// ✅ 현재 위치를 비동기로 요청하는 함수
// suspendCancellableCoroutine을 사용하여 위치 요청의 콜백 결과를 일시 중단(suspend)된 형태로 기다림
// 사용자가 위치 권한을 허용한 상태에서 호출되어야 함
suspend fun getCurrentLocation(context: Context): Location? =
    suspendCancellableCoroutine { cont ->
        // ✅ FusedLocationProviderClient는 Google Play Services의 위치 제공 클라이언트
        val fused = LocationServices.getFusedLocationProviderClient(context)

        // ✅ 위치 요청 객체 생성 (높은 정확도, 1회만 업데이트)
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, // 높은 정확도로 요청
            1000 // 위치 업데이트 간격 (밀리초) – 여기는 크게 의미 없음 (1회 요청이므로)
        ).setMaxUpdates(1) // 딱 1번만 위치 요청
            .build()

        // ✅ 위치 결과 콜백 정의
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                // 위치 업데이트 중지
                fused.removeLocationUpdates(this)
                // 콜백으로 받은 마지막 위치를 반환
                cont.resume(result.lastLocation)
            }
        }

        // ✅ 권한 확인은 컴포저블 외부에서 이미 수행했기 때문에 Suppress 처리
        @Suppress("MissingPermission")
        fused.requestLocationUpdates(request, callback, Looper.getMainLooper())

        // ✅ 중간에 코루틴이 취소되면 위치 업데이트도 취소
        cont.invokeOnCancellation {
            fused.removeLocationUpdates(callback)
        }
    }

// ✅ 위치 권한이 부여되었는지 확인하는 함수
// ContextCompat.checkSelfPermission()을 사용하여 ACCESS_FINE_LOCATION 권한 확인
// true면 허용됨, false면 거부됨
fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

// ✅ [추가됨] 현재 위치 권한 상태를 Boolean로 반환하는 rememberLocationPermissionState 함수
// Composable에서 LaunchedEffect 등에서 상태 변경을 감지하기 위한 목적
// ex) LaunchedEffect(locationPermissionGranted) { ... }
@Composable
fun rememberLocationPermissionState(context: Context): Boolean {
    // Context를 remember로 보존하지 않음 – 매 recomposition마다 context가 갱신되어도 안전함
    return remember(context) { hasLocationPermission(context) }
}
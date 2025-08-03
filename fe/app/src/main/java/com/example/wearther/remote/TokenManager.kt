package com.example.wearther.remote

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

// ✅ Firebase ID 토큰을 백엔드에 전송하여 JWT를 얻고 저장하는 함수
suspend fun sendTokenToBackend(idToken: String, context: Context): Boolean {
    return withContext(Dispatchers.IO) { // IO 스레드에서 실행
        try {
            val client = OkHttpClient() // OkHttp 클라이언트 생성

            val request = Request.Builder() // 요청 빌더 시작
                .url("${BASE_URL}auth/firebase/") // 백엔드 인증 API 주소
                .post("".toRequestBody("application/json".toMediaType())) // 빈 JSON 바디 전송
                .addHeader("Authorization", "Bearer $idToken") // Authorization 헤더에 Firebase ID 토큰 포함
                .build() // 요청 객체 생성

            val response = client.newCall(request).execute() // 동기식 요청 실행

            if (response.isSuccessful) { // 응답 성공 시
                val body = response.body?.string() // 응답 바디 문자열 추출
                val jwtToken = extractJwtTokenFromResponse(body) // JWT 파싱

                jwtToken?.let {
                    storeJwtToken(context, it) // SharedPreferences에 저장
                    return@withContext true // 성공적으로 저장되었음을 반환
                }
            } else {
                Log.e("BackendToken", "❌ 응답 실패: ${response.code}") // 서버 에러 코드 출력
            }

            false // JWT 저장 실패 시 false 반환
        } catch (e: Exception) {
            Log.e("BackendToken", "❌ 요청 실패: ${e.message}") // 네트워크 예외 발생 시 로그
            false // 실패 반환
        }
    }
}

// ✅ JWT 토큰을 SharedPreferences에 저장하는 함수
fun storeJwtToken(context: Context, jwtToken: String) {
    try {
        val prefs = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE) // SharedPreferences 인스턴스 생성
        prefs.edit().putString("jwt", jwtToken).apply() // jwt라는 key에 JWT 문자열 저장

        Log.d("JWT_SAVE", "🎯 JWT 저장 시도 값: $jwtToken") // 저장하려는 토큰 로그 출력

        val readBack = prefs.getString("jwt", null) // 저장 후 제대로 들어갔는지 재확인
        Log.d("JWT_SAVE", "📥 저장 후 바로 읽은 값: $readBack") // 읽어온 토큰 로그
    } catch (e: Exception) {
        Log.e("JWT_SAVE", "❌ 저장 실패: ${e.message}") // 저장 중 예외 발생 시 에러 로그
    }
}
// ✅ SharedPreferences에서 JWT 토큰을 불러오는 함수
fun getStoredJwtToken(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("jwt", null)
}

// ✅ 백엔드 응답에서 JWT 토큰을 추출하는 함수
fun extractJwtTokenFromResponse(response: String?): String? {
    return try {
        val json = JSONObject(response ?: "") // 응답 문자열을 JSONObject로 파싱
        val jwtToken = json.optString("token") // "token" 필드 값 추출
        Log.d("BackendToken", "✅ 추출된 토큰: $jwtToken") // 추출 결과 로그
        jwtToken // 추출한 토큰 반환
    } catch (e: Exception) {
        Log.e("BackendToken", "❌ 토큰 파싱 실패: ${e.message}") // JSON 파싱 실패 로그
        null // 실패 시 null 반환
    }
}
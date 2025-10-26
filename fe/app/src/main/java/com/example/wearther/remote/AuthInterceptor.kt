package com.example.wearther.remote

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

// SharedPreferences에서 토큰을 읽어 헤더에 삽입하는 클래스
class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // 1. 님이 만든 getStoredJwtToken 함수로 토큰을 가져옵니다.
        val token = getStoredJwtToken(context.applicationContext)

        if (token.isNullOrBlank()) {
            // 토큰이 없으면 그냥 원래 요청을 보냅니다. (로그인 안 된 상태)
            Log.d("AuthInterceptor", "Token is null/blank. Proceeding without auth.")
            return chain.proceed(chain.request())
        }

        // 2. 토큰이 있으면, 헤더에 "Authorization"을 추가합니다.
        Log.d("AuthInterceptor", "Attaching Token: Bearer $token")
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        // 3. 토큰이 포함된 새 요청을 서버로 보냅니다.
        return chain.proceed(newRequest)
    }
}
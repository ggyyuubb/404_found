package com.example.wearther.remote

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitProvider {

    // Retrofit 인스턴스를 저장할 변수
    private var retrofit: Retrofit? = null

    // 인증 기능이 포함된 Retrofit 인스턴스를 반환하는 함수
    fun getInstance(context: Context): Retrofit {
        if (retrofit == null) {
            // 1. 로그를 찍기 위한 로깅 인터셉터
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY // 모든 요청/응답 로그를 찍음
            }

            // 2. 1단계에서 만든 AuthInterceptor (토큰 삽입용)
            // (AuthInterceptor.kt 파일이 같은 패키지에 있어야 합니다)
            val authInterceptor = AuthInterceptor(context.applicationContext)

            // 3. 두 인터셉터를 탑재한 OkHttpClient 생성
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)    // [ 💡 핵심 ]
                .addInterceptor(loggingInterceptor) // [ 💡 핵심 ]
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            // 4. 공용 Retrofit 인스턴스 생성
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient) // [ 💡 핵심: 인증 기능이 탑재된 client 사용 ]
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}
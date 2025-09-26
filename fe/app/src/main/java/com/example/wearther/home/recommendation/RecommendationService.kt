package com.example.wearther.home.recommendation

// ✅ 네트워크 통신을 위한 주요 라이브러리 import
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.wearther.remote.BASE_URL

// ✅ 추천 API와 연결하기 위한 싱글톤 객체 정의
object RecommendationService {

    // ✅ OkHttpClient 설정: 네트워크 연결 및 데이터 전송 시 타임아웃 지정
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // 서버 연결 시도 제한 시간 30초
        .readTimeout(30, TimeUnit.SECONDS)    // 데이터 응답 대기 시간 30초
        .writeTimeout(30, TimeUnit.SECONDS)   // 데이터 전송 최대 시간 30초
        .build()

    // ✅ Retrofit 객체 생성
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL) // API의 실제 엔드포인트 접두 경로: ex) http://xxx/api/
        .addConverterFactory(GsonConverterFactory.create()) // JSON 변환기 설정 (Gson 사용)
        .client(okHttpClient) // 위에서 설정한 OkHttp 클라이언트 적용
        .build()

    // ✅ Retrofit을 통해 실제 API 인터페이스 구현체 생성
    val api: RecommendationApi = retrofit.create(RecommendationApi::class.java)
}

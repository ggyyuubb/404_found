package com.example.wearther.setting.data

import android.content.Context
import android.util.Log
import com.example.wearther.remote.RetrofitProvider
import com.example.wearther.remote.getStoredJwtToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import retrofit2.http.*
import retrofit2.HttpException
import java.io.File

// JWT 테스트용 API
interface TestApi {
    @GET("user/settings")
    suspend fun getUserSettings(): Response<Map<String, Any>>
}

class ProfileRepository(private val context: Context) {

    private val TAG = "ProfileRepository"

    private val api = RetrofitProvider.getInstance(context).create(ProfileApi::class.java)

    suspend fun testJwtToken(): Boolean = withContext(Dispatchers.IO) {
        try {
            val jwt = getStoredJwtToken(context)
            if (jwt == null) {
                Log.e(TAG, "❌ JWT 토큰이 없습니다")
                return@withContext false
            }

            Log.d(TAG, "🔑 JWT 토큰 테스트 시작: ${jwt.take(20)}...")

            val testApi = RetrofitProvider.getInstance(context).create(TestApi::class.java)
            val response = testApi.getUserSettings() // ✅ AuthInterceptor가 토큰 자동 추가

            if (response.isSuccessful) {
                Log.d(TAG, "✅ JWT 토큰 유효! 응답: ${response.body()}")
                return@withContext true
            } else {
                Log.e(TAG, "❌ JWT 토큰 무효: ${response.code()} ${response.message()}")
                Log.e(TAG, "    에러 바디: ${response.errorBody()?.string()}")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ JWT 테스트 실패", e)
            return@withContext false
        }
    }

    suspend fun uploadPhoto(file: File): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📸 ========== 프로필 사진 업로드 시작 ==========")

            val jwt = getStoredJwtToken(context)
            if (jwt == null) {
                Log.e(TAG, "❌ JWT 토큰이 없습니다")
                throw IllegalStateException("로그인이 필요합니다.")
            }
            Log.d(TAG, "✅ JWT 토큰 확인: ${jwt.take(20)}...")

            if (!file.exists()) {
                Log.e(TAG, "❌ 파일이 존재하지 않습니다: ${file.absolutePath}")
                throw IllegalStateException("파일이 존재하지 않습니다")
            }
            Log.d(TAG, "✅ 파일 확인:")
            Log.d(TAG, "   - 경로: ${file.absolutePath}")
            Log.d(TAG, "   - 크기: ${file.length()} bytes")
            Log.d(TAG, "   - 이름: ${file.name}")

            val reqBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", file.name, reqBody)
            Log.d(TAG, "✅ MultipartBody 생성 완료")

            val baseUrl = RetrofitProvider.getInstance(context).baseUrl().toString()
            Log.d(TAG, "📡 API 호출:")
            Log.d(TAG, "   - URL: ${baseUrl}user/profile_image")
            Log.d(TAG, "   - AuthInterceptor가 Bearer 토큰 자동 추가")

            val response = api.uploadProfileImage(part) // ✅ 토큰 파라미터 제거!

            Log.d(TAG, "📨 응답: ${response.code()} ${response.message()}")

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "응답 없음"
                Log.e(TAG, "❌ 업로드 실패:")
                Log.e(TAG, "   - 코드: ${response.code()}")
                Log.e(TAG, "   - 메시지: ${response.message()}")
                Log.e(TAG, "   - 에러: $errorBody")
                throw HttpException(response)
            }

            val result = response.body() ?: throw IllegalStateException("응답이 비어있습니다")

            Log.d(TAG, "🎉 업로드 성공!")
            Log.d(TAG, "   - 메시지: ${result.message}")
            Log.d(TAG, "   - URL: ${result.url}")
            Log.d(TAG, "========================================")

            return@withContext result.url

        } catch (e: Exception) {
            Log.e(TAG, "💥 업로드 실패", e)
            throw when (e) {
                is HttpException -> e
                is IllegalStateException -> e
                else -> IllegalStateException("이미지 업로드 실패: ${e.message}", e)
            }
        }
    }
}
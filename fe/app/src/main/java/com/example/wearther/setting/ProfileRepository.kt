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
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.HttpException
import java.io.File

// JWT 테스트용 API 인터페이스 추가
interface TestApi {
    @GET("user/settings")
    suspend fun getUserSettings(
        @Header("Authorization") token: String
    ): retrofit2.Response<Map<String, Any>>
}

class ProfileRepository(private val context: Context) {
    private val api = RetrofitProvider.retrofit.create(ProfileApi::class.java)

    // JWT 토큰 테스트용 API 추가
    suspend fun testJwtToken(): Boolean = withContext(Dispatchers.IO) {
        try {
            val jwt = getStoredJwtToken(context) ?: return@withContext false
            Log.d("ProfileRepository", "JWT 토큰 테스트 시작")

            // /user/settings GET 요청으로 JWT 테스트
            val testApi = RetrofitProvider.retrofit.create(TestApi::class.java)
            val response = testApi.getUserSettings("Bearer $jwt")

            if (response.isSuccessful) {
                Log.d("ProfileRepository", "JWT 토큰 유효! 응답: ${response.body()}")
                return@withContext true
            } else {
                Log.e("ProfileRepository", "JWT 토큰 무효: ${response.code()} ${response.message()}")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e("ProfileRepository", "JWT 테스트 실패", e)
            return@withContext false
        }
    }

    suspend fun uploadPhoto(file: File): String = withContext(Dispatchers.IO) {
        try {
            val jwt = getStoredJwtToken(context) ?: throw IllegalStateException("로그인이 필요합니다.")

            // JWT 토큰 확인용 로그 (앞 10자리만)
            Log.d("ProfileRepository", "JWT 토큰 앞부분: ${jwt.take(10)}...")

            val reqBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", file.name, reqBody)

            // 요청 정보 상세 로그
            Log.d("ProfileRepository", "이미지 업로드 시작: ${file.name}")
            Log.d("ProfileRepository", "파일 크기: ${file.length()} bytes")
            Log.d("ProfileRepository", "BASE_URL: ${RetrofitProvider.retrofit.baseUrl()}")
            Log.d("ProfileRepository", "요청 URL: ${RetrofitProvider.retrofit.baseUrl()}user/profile_image")
            Log.d("ProfileRepository", "Authorization 헤더: Bearer ${jwt.take(10)}...")

            val response = api.uploadProfileImage(part, "Bearer $jwt")

            if (!response.isSuccessful) {
                // 에러 응답 상세 정보
                val errorBody = response.errorBody()?.string() ?: "응답 없음"
                Log.e("ProfileRepository", "업로드 실패 상세:")
                Log.e("ProfileRepository", "  - 응답 코드: ${response.code()}")
                Log.e("ProfileRepository", "  - 응답 메시지: ${response.message()}")
                Log.e("ProfileRepository", "  - 에러 바디: $errorBody")
                Log.e("ProfileRepository", "  - 요청 URL: ${response.raw().request.url}")
                throw HttpException(response)
            }

            val result = response.body() ?: throw IllegalStateException("응답이 비어있습니다")
            Log.d("ProfileRepository", "업로드 성공! 메시지: ${result.message}, URL: ${result.url}")
            return@withContext result.url

        } catch (e: Exception) {
            Log.e("ProfileRepository", "업로드 중 오류 발생", e)
            throw when (e) {
                is HttpException -> e
                is IllegalStateException -> e
                else -> IllegalStateException("이미지 업로드 실패: ${e.message}", e)
            }
        }
    }
}
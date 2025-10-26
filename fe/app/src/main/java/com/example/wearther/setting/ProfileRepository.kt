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

    // --- [ 💡 1. 수정 ] ---
    // .retrofit 대신 getInstance(context) 사용
    private val api = RetrofitProvider.getInstance(context).create(ProfileApi::class.java)
    // --- [ 수정 끝 ] ---

    // JWT 토큰 테스트용 API 추가
    suspend fun testJwtToken(): Boolean = withContext(Dispatchers.IO) {
        try {
            val jwt = getStoredJwtToken(context) ?: return@withContext false
            Log.d("ProfileRepository", "JWT 토큰 테스트 시작")

            // --- [ 💡 2. 수정 ] ---
            // .retrofit 대신 getInstance(context) 사용
            val testApi = RetrofitProvider.getInstance(context).create(TestApi::class.java)
            // --- [ 수정 끝 ] ---

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

            Log.d("ProfileRepository", "JWT 토큰 앞부분: ${jwt.take(10)}...")

            val reqBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", file.name, reqBody)

            // --- [ 💡 3. 수정 ] ---
            // .retrofit.baseUrl() 대신 getInstance(context).baseUrl() 사용
            val baseUrl = RetrofitProvider.getInstance(context).baseUrl()
            // --- [ 수정 끝 ] ---

            Log.d("ProfileRepository", "이미지 업로드 시작: ${file.name}")
            Log.d("ProfileRepository", "파일 크기: ${file.length()} bytes")
            Log.d("ProfileRepository", "BASE_URL: $baseUrl")
            Log.d("ProfileRepository", "요청 URL: ${baseUrl}user/profile_image")
            Log.d("ProfileRepository", "Authorization 헤더: Bearer ${jwt.take(10)}...")

            val response = api.uploadProfileImage(part, "Bearer $jwt")

            if (!response.isSuccessful) {
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
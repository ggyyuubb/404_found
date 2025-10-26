package com.example.wearther.closet.upload

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.wearther.remote.BASE_URL
import com.example.wearther.remote.getStoredJwtToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

fun handleUpload(
    context: Context,
    coroutineScope: CoroutineScope,
    selectedImageUri: Uri,
    selectedType: String,
    selectedCategory: String,
    selectedColors: List<String>,
    selectedMaterial: String,
    onSuccess: () -> Unit,
    onError: () -> Unit
) {
    coroutineScope.launch {
        try {
            val jwtToken = getStoredJwtToken(context)
            if (jwtToken == null) {
                Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                onError()
                return@launch
            }

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val uploadApi = retrofit.create(UploadApi::class.java)

            // 파일 준비
            val file = uriToFile(context, selectedImageUri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

            Log.d("Upload", "업로드 시작: type=$selectedType, category=$selectedCategory")

            // 업로드
            val response = uploadApi.uploadImage(
                token = "Bearer $jwtToken",
                image = imagePart
            )

            file.delete()

            Log.d("Upload", "서버 응답: $response")

            if (response.error != null) {
                Toast.makeText(context, "업로드 실패: ${response.error}", Toast.LENGTH_LONG).show()
                onError()
                return@launch
            }

            // 카테고리 정보 업데이트
            val imageId = response.id
            if (!imageId.isNullOrBlank()) {
                try {
                    val englishSub = com.example.wearther.closet.data.ClosetSortUtils
                        .toEnglish(selectedCategory) ?: selectedCategory

                    val body = UpdateClothingRequest(
                        type = selectedType,
                        category = englishSub,
                        colors = selectedColors,
                        material = selectedMaterial,
                        suitable_temperature = null
                    )

                    val updateResponse = uploadApi.updateClothingJson(
                        token = "Bearer $jwtToken",
                        imageId = imageId,
                        body = body
                    )
                    Log.d("Upload", "업데이트 완료: $updateResponse")
                } catch (e: Exception) {
                    Log.e("Upload", "카테고리 업데이트 실패", e)
                }
            }

            Toast.makeText(context, "업로드 완료!", Toast.LENGTH_SHORT).show()
            onSuccess()

        } catch (e: Exception) {
            Log.e("Upload", "업로드 오류", e)
            Toast.makeText(context, "업로드 중 오류: ${e.message}", Toast.LENGTH_LONG).show()
            onError()
        }
    }
}

private fun uriToFile(context: Context, uri: Uri): File {
    val contentResolver = context.contentResolver
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output -> input.copyTo(output) }
    }
    return file
}
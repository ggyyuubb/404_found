package com.example.wearther.setting.screen

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileImage(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    onPhotoChanged: ((String) -> Unit)? = null,
    uploadPhotoFile: (suspend (File) -> String)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ✅ photoUrl이 바뀔 때마다 currentUrl도 업데이트
    var currentUrl by remember { mutableStateOf(photoUrl) }
    var uploading by remember { mutableStateOf(false) }

    // ✅ photoUrl이 외부에서 바뀌면 currentUrl도 업데이트
    LaunchedEffect(photoUrl) {
        Log.d("ProfileImage", "photoUrl 변경됨: $photoUrl")
        currentUrl = photoUrl
    }

    val pickerModern = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        Log.d("ProfileImage", "📸 사진 선택됨: $uri")
        if (uri != null && uploadPhotoFile != null) {
            scope.launch {
                uploading = true
                runCatching {
                    Log.d("ProfileImage", "📁 파일 복사 시작")
                    val file = copyUriToCacheAsFile(context, context.cacheDir, uri)
                    Log.d("ProfileImage", "✅ 파일 복사 완료: ${file.absolutePath}")

                    Log.d("ProfileImage", "☁️ 서버 업로드 시작")
                    val newUrl = uploadPhotoFile.invoke(file)
                    Log.d("ProfileImage", "✅ 서버 업로드 완료: $newUrl")

                    // ✅ UI 즉시 반영 (캐시 무효화)
                    val refreshedUrl = "$newUrl?ts=${System.currentTimeMillis()}"
                    currentUrl = refreshedUrl
                    Log.d("ProfileImage", "🖼️ UI 업데이트: $refreshedUrl")

                    // ✅ 상위에 원본 URL 전달
                    onPhotoChanged?.invoke(newUrl)
                    Log.d("ProfileImage", "📤 상위로 URL 전달: $newUrl")
                }.onFailure { e ->
                    Log.e("ProfileImage", "❌ 업로드 실패", e)
                }
                uploading = false
            }
        }
    }

    val pickerLegacy = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d("ProfileImage", "📸 (Legacy) 사진 선택됨: $uri")
        if (uri != null && uploadPhotoFile != null) {
            scope.launch {
                uploading = true
                runCatching {
                    val file = copyUriToCacheAsFile(context, context.cacheDir, uri)
                    val newUrl = uploadPhotoFile.invoke(file)
                    val refreshedUrl = "$newUrl?ts=${System.currentTimeMillis()}"
                    currentUrl = refreshedUrl
                    onPhotoChanged?.invoke(newUrl)
                    Log.d("ProfileImage", "✅ (Legacy) 업로드 완료: $newUrl")
                }.onFailure { e ->
                    Log.e("ProfileImage", "❌ (Legacy) 업로드 실패", e)
                }
                uploading = false
            }
        }
    }

    fun launchPicker() {
        if (uploadPhotoFile == null) {
            Log.w("ProfileImage", "⚠️ uploadPhotoFile이 null입니다")
            return
        }
        runCatching {
            pickerModern.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }.onFailure {
            Log.d("ProfileImage", "Modern picker 실패, Legacy로 전환")
            pickerLegacy.launch("image/*")
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (!currentUrl.isNullOrEmpty()) {
            Log.d("ProfileImage", "🖼️ 이미지 표시: $currentUrl")

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(currentUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable(enabled = uploadPhotoFile != null && !uploading) {
                        launchPicker()
                    },
                onSuccess = {
                    Log.d("ProfileImage", "✅ 이미지 로드 성공")
                },
                onError = {
                    Log.e("ProfileImage", "❌ 이미지 로드 실패: $currentUrl")
                }
            )
        } else {
            Log.d("ProfileImage", "👤 기본 아이콘 표시")

            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Default Profile",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable(enabled = uploadPhotoFile != null && !uploading) {
                        launchPicker()
                    }
            )
        }

        if (uploading) {
            CircularProgressIndicator(modifier = Modifier.size(40.dp))
        }
    }
}

private fun copyUriToCacheAsFile(
    context: Context,
    cacheDir: File,
    uri: Uri
): File {
    val input = context.contentResolver.openInputStream(uri)
        ?: error("파일 열기 실패")
    val outFile = File(cacheDir, "profile_upload_${System.currentTimeMillis()}.jpg")
    input.use { inp ->
        FileOutputStream(outFile).use { out ->
            inp.copyTo(out)
        }
    }
    Log.d("ProfileImage", "📁 임시 파일 생성: ${outFile.absolutePath}")
    return outFile
}
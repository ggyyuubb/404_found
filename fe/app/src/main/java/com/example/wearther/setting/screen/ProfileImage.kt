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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * - photoUrl 이 있으면 그 이미지, 없으면 기본 아이콘을 원형으로 표시
 * - 탭하면 포토 피커 실행 → (주입된) 업로드 함수 실행 → 성공 시 onPhotoChanged 호출
 *
 * @param photoUrl 현재 표시할 프로필 이미지 URL(구글 로그인 사용자면 여기로 들어옴)
 * @param onPhotoChanged 업로드 성공 후 새 URL을 상위로 전달 (원본 URL 그대로 전달)
 * @param uploadPhotoFile 실제 업로드 처리. 예: { file -> ProfileRepository().uploadPhoto(file) }
 */
@Composable
fun ProfileImage(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    onPhotoChanged: ((String) -> Unit)? = null,
    uploadPhotoFile: (suspend (File) -> String)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentUrl by remember(photoUrl) { mutableStateOf(photoUrl) }
    var uploading by remember { mutableStateOf(false) }

    // ProfileImage.kt의 pickerModern 부분 수정
    val pickerModern = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        Log.d("ProfileImage", "사진 선택됨: $uri")
        if (uri != null && uploadPhotoFile != null) {
            scope.launch {
                uploading = true
                runCatching {
                    Log.d("ProfileImage", "파일 복사 시작")
                    val file = copyUriToCacheAsFile(context, context.cacheDir, uri)
                    Log.d("ProfileImage", "파일 복사 완료: ${file.absolutePath}")

                    Log.d("ProfileImage", "업로드 함수 호출")
                    val newUrl = uploadPhotoFile.invoke(file) // 서버가 돌려준 원본 URL
                    Log.d("ProfileImage", "업로드 완료: $newUrl")

                    // UI 즉시 반영용(캐시 무효화): ts 쿼리 붙여서 표시만 갱신
                    val refreshed = if ('?' in newUrl) {
                        "${newUrl}&ts=${System.currentTimeMillis()}"
                    } else {
                        "${newUrl}?ts=${System.currentTimeMillis()}"
                    }
                    currentUrl = refreshed
                    Log.d("ProfileImage", "UI 업데이트: $refreshed")

                    // 상위에는 원본 URL 그대로 전달(저장/동기화용)
                    onPhotoChanged?.invoke(newUrl)
                    Log.d("ProfileImage", "상위로 URL 전달: $newUrl")
                }.onFailure { e ->
                    Log.e("ProfileImage", "업로드 실패", e)
                }
                uploading = false
            }
        }
    }

    // Android 12 이하 fallback
    val pickerLegacy = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && uploadPhotoFile != null) {
            scope.launch {
                uploading = true
                runCatching {
                    val file = copyUriToCacheAsFile(context, context.cacheDir, uri)
                    val newUrl = uploadPhotoFile.invoke(file)
                    val refreshed = if ('?' in newUrl) {
                        "${newUrl}&ts=${System.currentTimeMillis()}"
                    } else {
                        "${newUrl}?ts=${System.currentTimeMillis()}"
                    }
                    currentUrl = refreshed
                    onPhotoChanged?.invoke(newUrl)
                }
                uploading = false
            }
        }
    }

    fun launchPicker() {
        if (uploadPhotoFile == null) return // 업로드 함수 없으면 선택도 불가
        runCatching {
            pickerModern.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }.onFailure {
            pickerLegacy.launch("image/*")
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (!currentUrl.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(currentUrl),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable(enabled = uploadPhotoFile != null && !uploading) { launchPicker() }
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Default Profile",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable(enabled = uploadPhotoFile != null && !uploading) { launchPicker() }
            )
        }

        if (uploading) {
            CircularProgressIndicator()
        }
    }
}

/** Uri를 캐시 디렉토리에 임시 파일로 복사 (Composable 아님) */
private fun copyUriToCacheAsFile(
    context: Context,
    cacheDir: File,
    uri: Uri
): File {
    val input = context.contentResolver.openInputStream(uri) ?: error("파일 열기 실패")
    val outFile = File(cacheDir, "profile_upload_${System.currentTimeMillis()}.jpg")
    input.use { inp -> FileOutputStream(outFile).use { out -> inp.copyTo(out) } }
    return outFile
}

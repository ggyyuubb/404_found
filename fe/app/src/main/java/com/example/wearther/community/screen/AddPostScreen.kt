package com.example.wearther.community.screen

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log // Log import 추가
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.wearther.community.vm.CommunityViewModel
// 확장 함수 import
import com.example.wearther.community.vm.addFeed
import com.example.wearther.community.vm.addFeedWithImage
import kotlinx.coroutines.launch
import java.io.File

// Logcat 검색용 태그
private const val TAG = "AddPostScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostScreen(
    navController: NavController,
    viewModel: CommunityViewModel
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- 1. 상태 (State) ---
    var description by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var weather by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var aiImageUrl by remember { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val isUploading by viewModel.isLoading.collectAsState()
    var showImageSourceDialog by remember { mutableStateOf(false) }


    // --- 2. 로직 및 이벤트 핸들러 (Logic & Event Handlers) ---

    // 스낵바
    fun showSnack(msg: String) {
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    // AI 이미지 선택 처리
    val selectedAiImageUrl = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_image_url")

    LaunchedEffect(selectedAiImageUrl) {
        selectedAiImageUrl?.let {
            Log.d(TAG, "AI 이미지 선택됨: $it") // 로그 추가
            aiImageUrl = it
            selectedImageUri = null
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<String>("selected_image_url")
        }
    }

    // 업로드 성공/실패 이벤트 감지
    LaunchedEffect(key1 = Unit, snackbarHostState) {
        launch {
            viewModel.uploadSuccessEvent.collect { success ->
                if (success) {
                    Log.d(TAG, "✅ ViewModel로부터 업로드 성공 이벤트 수신") // 로그 추가
                    showSnack("등록 완료!")
                    navController.popBackStack()
                }
            }
        }
        launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Log.e(TAG, "❌ ViewModel로부터 에러 메시지 수신: $it") // 로그 추가
                    showSnack(it)
                    // viewModel.clearErrorMessage() // 에러 소비
                }
            }
        }
    }

    // 카메라/갤러리 런처
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            Log.d(TAG, "📸 카메라 촬영 성공, URI: $pendingCameraUri") // 로그 추가
            selectedImageUri = pendingCameraUri
            aiImageUrl = null
        } else {
            Log.w(TAG, "📸 카메라 촬영 취소 또는 실패") // 로그 추가
        }
        pendingCameraUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Log.d(TAG, "✅ 카메라 권한 허용됨") // 로그 추가
            try {
                val photoFile = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "IMG_${System.currentTimeMillis()}.jpg"
                )
                val fileUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                pendingCameraUri = fileUri
                Log.d(TAG, "📸 카메라 앱 실행 시도, URI: $fileUri") // 로그 추가
                takePictureLauncher.launch(fileUri)
            } catch (e: Exception) {
                Log.e(TAG, "❌ 카메라 실행 중 오류", e) // 로그 추가
                showSnack("카메라 실행 중 오류가 발생했습니다: ${e.localizedMessage}")
            }
        } else {
            Log.w(TAG, "⚠️ 카메라 권한 거부됨") // 로그 추가
            showSnack("카메라 권한이 필요합니다.")
        }
    }

    fun takePhoto() {
        Log.d(TAG, "권한 요청: CAMERA") // 로그 추가
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val pickPhoto13Plus = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            Log.d(TAG, "🖼️ (SDK 33+) 갤러리 선택됨: $it") // 로그 추가
            selectedImageUri = it
            aiImageUrl = null
        } ?: Log.d(TAG, "🖼️ (SDK 33+) 갤러리 선택 취소") // 로그 추가
    }

    val pickPhotoLegacy = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            Log.d(TAG, "🖼️ (Legacy) 갤러리 선택됨: $it") // 로그 추가
            selectedImageUri = it
            aiImageUrl = null
        } ?: Log.d(TAG, "🖼️ (Legacy) 갤러리 선택 취소") // 로그 추가
    }

    fun pickFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "갤러리 실행 (SDK 33+)") // 로그 추가
            pickPhoto13Plus.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            Log.d(TAG, "갤러리 실행 (Legacy)") // 로그 추가
            pickPhotoLegacy.launch("image/*")
        }
    }

    // 업로드 로직
    fun upload() {
        // --- [ 💡 로그 추가 💡 ] ---
        Log.i(TAG, "--- [ 게시물 업로드 시도 ] ---")
        Log.d(TAG, "Description: ${description.take(50)}...") // 내용 일부만 로깅
        Log.d(TAG, "Selected Image URI: $selectedImageUri")
        Log.d(TAG, "AI Image URL: $aiImageUrl")
        Log.d(TAG, "Temperature: $temperature")
        Log.d(TAG, "Weather: $weather")
        // --- [ 로그 추가 끝 ] ---

        if (description.isBlank()) {
            Log.w(TAG, "⚠️ 내용이 비어있어 업로드 중단") // 로그 추가
            showSnack("내용을 입력해 주세요.")
            return
        }

        // 1) 로컬 이미지가 있을 때
        selectedImageUri?.let { uri ->
            // --- [ 💡 로그 추가 💡 ] ---
            Log.d(TAG, "➡️ 로컬 이미지 업로드 함수 호출 (addFeedWithImage)")
            // --- [ 로그 추가 끝 ] ---
            viewModel.addFeedWithImage( // 확장 함수 호출
                description = description,
                temperature = temperature.ifBlank { "N/A" },
                weather = weather.ifBlank { "N/A" },
                imageUri = uri
            )
            return
        }

        // 2) AI 이미지 URL만 있을 때
        aiImageUrl?.let { url ->
            // --- [ 💡 로그 추가 💡 ] ---
            Log.d(TAG, "➡️ AI 이미지 URL 업로드 함수 호출 (addFeed)")
            // --- [ 로그 추가 끝 ] ---
            viewModel.addFeed( // 확장 함수 호출
                description = description,
                temperature = temperature.ifBlank { "N/A" },
                weather = weather.ifBlank { "N/A" },
                imageUrl = url
            )
            return
        }

        // 3) 텍스트만 게시
        // --- [ 💡 로그 추가 💡 ] ---
        Log.d(TAG, "➡️ 텍스트만 업로드 함수 호출 (addFeed)")
        // --- [ 로그 추가 끝 ] ---
        viewModel.addFeed( // 확장 함수 호출
            description = description,
            temperature = temperature.ifBlank { "N/A" },
            weather = weather.ifBlank { "N/A" },
            imageUrl = null
        )
    }


    // --- 3. UI 그리기 (Drawing UI) ---
    Scaffold(
        topBar = {
            AddPostTopBar(
                isUploading = isUploading,
                description = description,
                onUploadClick = { upload() }, // upload 함수 연결 확인
                onBackClick = { navController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // AddPostForm 호출 (AddPostComposables.kt 파일에 있음)
        AddPostForm(
            modifier = Modifier.padding(padding),
            description = description,
            onDescriptionChange = { description = it },
            temperature = temperature,
            onTemperatureChange = { temperature = it },
            weather = weather,
            onWeatherChange = { weather = it },
            selectedImageUri = selectedImageUri,
            aiImageUrl = aiImageUrl,
            isUploading = isUploading,
            onImageClick = {
                Log.d(TAG, "이미지 선택 다이얼로그 표시 요청") // 로그 추가
                showImageSourceDialog = true
            },
            onAiPickerClick = {
                Log.d(TAG, "AI 추천 화면으로 이동 요청") // 로그 추가
                navController.navigate("ai_recommendation_picker")
            },
            onTakePhotoClick = { takePhoto() }, // takePhoto 함수 연결 확인
            onPickFromGalleryClick = { pickFromGallery() } // pickFromGallery 함수 연결 확인
        )
    }

    // 다이얼로그 (AddPostComposables.kt 파일에 있음)
    if (showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showImageSourceDialog = false },
            onTakePhoto = {
                showImageSourceDialog = false
                takePhoto() // takePhoto 함수 연결 확인
            },
            onPickFromGallery = {
                showImageSourceDialog = false
                pickFromGallery() // pickFromGallery 함수 연결 확인
            }
        )
    }
}
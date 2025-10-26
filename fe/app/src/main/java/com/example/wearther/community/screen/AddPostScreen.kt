package com.example.wearther.community.screen

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
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
import com.example.wearther.community.vm.addFeed
import com.example.wearther.community.vm.addFeedWithImage
import com.example.wearther.home.weather.WeatherViewModel // ✅ import 추가
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "AddPostScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostScreen(
    navController: NavController,
    viewModel: CommunityViewModel,
    weatherViewModel: WeatherViewModel
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currentTemp by weatherViewModel.currentTemperature.collectAsState()
    val currentWeather by weatherViewModel.currentWeather.collectAsState()

    var description by remember { mutableStateOf("") }
    var temperature by remember(currentTemp) {
        mutableStateOf(currentTemp.ifBlank { "" })
    }
    var weather by remember(currentWeather) {
        mutableStateOf(currentWeather.ifBlank { "" })
    }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var aiImageUrl by remember { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val isUploading by viewModel.isLoading.collectAsState()
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // ✅ 이미지가 있는지 확인
    val hasImage = selectedImageUri != null || aiImageUrl != null

    LaunchedEffect(currentTemp, currentWeather) {
        if (temperature.isBlank() && currentTemp.isNotBlank()) {
            temperature = currentTemp
            Log.d(TAG, "🌡️ 현재 온도 자동 설정: $currentTemp°C")
        }
        if (weather.isBlank() && currentWeather.isNotBlank()) {
            weather = currentWeather
            Log.d(TAG, "☁️ 현재 날씨 자동 설정: $currentWeather")
        }
    }

    fun showSnack(msg: String) {
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    val selectedAiImageUrl = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_image_url")

    LaunchedEffect(selectedAiImageUrl) {
        selectedAiImageUrl?.let {
            Log.d(TAG, "AI 이미지 선택됨: $it")
            aiImageUrl = it
            selectedImageUri = null
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<String>("selected_image_url")
        }
    }

    LaunchedEffect(key1 = Unit, snackbarHostState) {
        launch {
            viewModel.uploadSuccessEvent.collect { success ->
                if (success) {
                    Log.d(TAG, "✅ ViewModel로부터 업로드 성공 이벤트 수신")
                    showSnack("등록 완료!")
                    navController.popBackStack()
                }
            }
        }
        launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Log.e(TAG, "❌ ViewModel로부터 에러 메시지 수신: $it")
                    showSnack(it)
                }
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            Log.d(TAG, "📸 카메라 촬영 성공, URI: $pendingCameraUri")
            selectedImageUri = pendingCameraUri
            aiImageUrl = null
        } else {
            Log.w(TAG, "📸 카메라 촬영 취소 또는 실패")
        }
        pendingCameraUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Log.d(TAG, "✅ 카메라 권한 허용됨")
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
                Log.d(TAG, "📸 카메라 앱 실행 시도, URI: $fileUri")
                takePictureLauncher.launch(fileUri)
            } catch (e: Exception) {
                Log.e(TAG, "❌ 카메라 실행 중 오류", e)
                showSnack("카메라 실행 중 오류가 발생했습니다: ${e.localizedMessage}")
            }
        } else {
            Log.w(TAG, "⚠️ 카메라 권한 거부됨")
            showSnack("카메라 권한이 필요합니다.")
        }
    }

    fun takePhoto() {
        Log.d(TAG, "권한 요청: CAMERA")
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val pickPhoto13Plus = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            Log.d(TAG, "🖼️ (SDK 33+) 갤러리 선택됨: $it")
            selectedImageUri = it
            aiImageUrl = null
        } ?: Log.d(TAG, "🖼️ (SDK 33+) 갤러리 선택 취소")
    }

    val pickPhotoLegacy = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            Log.d(TAG, "🖼️ (Legacy) 갤러리 선택됨: $it")
            selectedImageUri = it
            aiImageUrl = null
        } ?: Log.d(TAG, "🖼️ (Legacy) 갤러리 선택 취소")
    }

    fun pickFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "갤러리 실행 (SDK 33+)")
            pickPhoto13Plus.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            Log.d(TAG, "갤러리 실행 (Legacy)")
            pickPhotoLegacy.launch("image/*")
        }
    }

    // ✅✅ 업로드 로직 - 유효성 검사 추가
    fun upload() {
        Log.i(TAG, "--- [ 게시물 업로드 시도 ] ---")

        // ✅ 필수 입력 체크
        when {
            description.isBlank() -> {
                showSnack("내용을 입력해 주세요.")
                return
            }
            !hasImage -> {
                showSnack("사진을 추가해 주세요.")
                return
            }
            temperature.isBlank() -> {
                showSnack("온도를 입력해 주세요.")
                return
            }
            weather.isBlank() -> {
                showSnack("날씨를 입력해 주세요.")
                return
            }
        }

        Log.d(TAG, "Description: ${description.take(50)}...")
        Log.d(TAG, "Selected Image URI: $selectedImageUri")
        Log.d(TAG, "AI Image URL: $aiImageUrl")
        Log.d(TAG, "Temperature: $temperature")
        Log.d(TAG, "Weather: $weather")

        selectedImageUri?.let { uri ->
            Log.d(TAG, "➡️ 로컬 이미지 업로드 함수 호출 (addFeedWithImage)")
            viewModel.addFeedWithImage(
                description = description,
                temperature = temperature,
                weather = weather,
                imageUri = uri
            )
            return
        }

        aiImageUrl?.let { url ->
            Log.d(TAG, "➡️ AI 이미지 URL 업로드 함수 호출 (addFeed)")
            viewModel.addFeed(
                description = description,
                temperature = temperature,
                weather = weather,
                imageUrl = url
            )
            return
        }
    }

    // --- 3. UI 그리기 (Drawing UI) ---
    Scaffold(
        topBar = {
            AddPostTopBar(
                isUploading = isUploading,
                description = description,
                temperature = temperature, // ✅ 전달
                weather = weather,         // ✅ 전달
                hasImage = hasImage,       // ✅ 전달
                onUploadClick = { upload() },
                onBackClick = { navController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
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
                Log.d(TAG, "이미지 선택 다이얼로그 표시 요청")
                showImageSourceDialog = true
            },
            onAiPickerClick = {
                Log.d(TAG, "AI 추천 화면으로 이동 요청")
                navController.navigate("ai_recommendation_picker")
            },
            onTakePhotoClick = { takePhoto() },
            onPickFromGalleryClick = { pickFromGallery() }
        )
    }

    if (showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showImageSourceDialog = false },
            onTakePhoto = {
                showImageSourceDialog = false
                takePhoto()
            },
            onPickFromGallery = {
                showImageSourceDialog = false
                pickFromGallery()
            }
        )
    }
}
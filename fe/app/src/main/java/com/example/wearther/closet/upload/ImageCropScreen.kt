package com.example.wearther.closet.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropScreen(
    imageUri: Uri,
    onNavigateBack: () -> Unit,
    onCropComplete: (Uri) -> Unit
) {
    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { croppedUri ->
                onCropComplete(croppedUri)
            }
        } else {
            onNavigateBack()
        }
    }

    LaunchedEffect(imageUri) {
        val cropOptions = CropImageContractOptions(
            uri = imageUri,
            cropImageOptions = CropImageOptions().apply {
                fixAspectRatio = true
                aspectRatioX = 3  // 0.75 = 3:4
                aspectRatioY = 4
                guidelines = CropImageView.Guidelines.ON
                activityTitle = "사진 자르기"
                cropMenuCropButtonTitle = "완료"
                outputCompressFormat = android.graphics.Bitmap.CompressFormat.JPEG
                outputCompressQuality = 90
            }
        )
        cropImageLauncher.launch(cropOptions)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("사진 편집", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    "사진을 불러오는 중...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }
    }
}
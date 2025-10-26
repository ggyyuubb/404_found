package com.example.wearther.community.screen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddPostTopBar(
    isUploading: Boolean,
    description: String,
    onUploadClick: () -> Unit,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { Text("게시글 작성") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
        },
        actions = {
            TextButton(
                onClick = onUploadClick,
                enabled = !isUploading && description.isNotBlank()
            ) {
                Text(
                    if (isUploading) "업로드 중..." else "등록",
                    color = if (!isUploading && description.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else Color.Gray
                )
            }
        }
    )
}

@Composable
internal fun AddPostForm(
    modifier: Modifier = Modifier,
    description: String,
    onDescriptionChange: (String) -> Unit,
    temperature: String,
    onTemperatureChange: (String) -> Unit,
    weather: String,
    onWeatherChange: (String) -> Unit,
    selectedImageUri: Uri?,
    aiImageUrl: String?,
    isUploading: Boolean,
    onImageClick: () -> Unit,
    onAiPickerClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onPickFromGalleryClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 이미지 미리보기
        ImagePreviewBox(
            selectedImageUri = selectedImageUri,
            aiImageUrl = aiImageUrl,
            onClick = onImageClick
        )

        Spacer(Modifier.height(16.dp))

        // AI 추천 코디 버튼
        Button(
            onClick = onAiPickerClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Icon(imageVector = Icons.Filled.PhotoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI 추천 코디 선택")
        }

        Spacer(Modifier.height(24.dp))

        // 텍스트 입력 필드
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("내용을 입력하세요") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            maxLines = 10
        )

        Spacer(Modifier.height(16.dp))

        // 온도/날씨 입력 필드
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = temperature,
                onValueChange = onTemperatureChange,
                label = { Text("온도") },
                placeholder = { Text("예: 18°C") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = weather,
                onValueChange = onWeatherChange,
                label = { Text("날씨") },
                placeholder = { Text("예: 맑음") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        // 카메라/갤러리 버튼
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            FilledTonalButton(
                enabled = !isUploading,
                onClick = onTakePhotoClick
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("카메라")
            }
            OutlinedButton(
                enabled = !isUploading,
                onClick = onPickFromGalleryClick
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("갤러리")
            }
        }
    }
}

@Composable
private fun ImagePreviewBox(
    selectedImageUri: Uri?,
    aiImageUrl: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF3F4F6))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            selectedImageUri != null -> {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = "선택한 이미지",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            aiImageUrl != null -> {
                Image(
                    painter = rememberAsyncImagePainter(aiImageUrl),
                    contentDescription = "AI 추천 이미지",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("사진 추가", color = Color.Gray)
                }
            }
        }
    }
}

// --- 4. 이미지 선택 다이얼로그 ---

@Composable
internal fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("사진 선택") },
        text = {
            Column {
                TextButton(
                    onClick = onTakePhoto,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("카메라로 촬영")
                }
                TextButton(
                    onClick = onPickFromGallery,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("갤러리에서 선택")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        }
    )
}
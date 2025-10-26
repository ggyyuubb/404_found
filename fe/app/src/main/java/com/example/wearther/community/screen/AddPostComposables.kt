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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddPostTopBar(
    isUploading: Boolean,
    description: String,
    temperature: String, // ✅ 추가
    weather: String,     // ✅ 추가
    hasImage: Boolean,   // ✅ 추가
    onUploadClick: () -> Unit,
    onBackClick: () -> Unit
) {
    // ✅ 모든 필수 입력이 채워졌는지 확인
    val isFormValid = description.isNotBlank() &&
            temperature.isNotBlank() &&
            weather.isNotBlank() &&
            hasImage

    TopAppBar(
        title = { Text("게시글 작성") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
        },
        actions = {
            // ✅ 버튼 스타일 개선
            Button(
                onClick = onUploadClick,
                enabled = !isUploading && isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.Gray.copy(alpha = 0.3f),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    if (isUploading) "업로드 중..." else "등록",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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

        // ✅✅ 현재 날씨 정보 카드 (강조)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFEFF6FF) // 연한 파란색 배경
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 헤더: 현재 날씨 표시
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "현재 날씨 정보",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                        Text(
                            "자동으로 채워졌습니다 (수정 가능)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280),
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    // 실시간 아이콘
                    Surface(
                        color = Color(0xFF3B82F6).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "실시간",
                                fontSize = 11.sp,
                                color = Color(0xFF3B82F6),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 온도/날씨 입력 필드
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 온도 입력
                    OutlinedTextField(
                        value = temperature,
                        onValueChange = onTemperatureChange,
                        label = { Text("온도") },
                        placeholder = { Text("예: 18") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6)
                            )
                        },
                        suffix = { Text("°C", color = Color(0xFF6B7280)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFBFDBFE),
                            focusedLeadingIconColor = Color(0xFF3B82F6),
                            unfocusedLeadingIconColor = Color(0xFF93C5FD)
                        )
                    )

                    // 날씨 입력
                    OutlinedTextField(
                        value = weather,
                        onValueChange = onWeatherChange,
                        label = { Text("날씨") },
                        placeholder = { Text("예: 맑음") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFBFDBFE),
                            focusedLeadingIconColor = Color(0xFF3B82F6),
                            unfocusedLeadingIconColor = Color(0xFF93C5FD)
                        )
                    )
                }
            }
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
                onClick = onTakePhotoClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("카메라")
            }
            OutlinedButton(
                enabled = !isUploading,
                onClick = onPickFromGalleryClick,
                modifier = Modifier.weight(1f)
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
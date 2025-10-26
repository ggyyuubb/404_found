package com.example.wearther.community.screen

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

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
        ImagePreviewBox(
            selectedImageUri = selectedImageUri,
            aiImageUrl = aiImageUrl,
            onClick = onImageClick
        )

        Spacer(Modifier.height(16.dp))

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

        WeatherInfoCard(
            temperature = temperature,
            onTemperatureChange = onTemperatureChange,
            weather = weather,
            onWeatherChange = onWeatherChange
        )
    }
}

@Composable
private fun WeatherInfoCard(
    temperature: String,
    onTemperatureChange: (String) -> Unit,
    weather: String,
    onWeatherChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFF6FF)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

@Composable
internal fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = CircleShape,
                color = Color(0xFFE8EAF6),
                modifier = Modifier.size(80.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        tint = Color(0xFF5C6BC0),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        },
        title = {
            Text(
                "사진 추가",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "사진을 올릴 방법을 선택하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onDismiss()
                        onTakePhoto()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5C6BC0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "카메라로 촬영하기",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onPickFromGallery()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF5C6BC0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        tint = Color(0xFF5C6BC0),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "갤러리에서 선택하기",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        color = Color(0xFF5C6BC0)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "취소",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    color = Color.Gray
                )
            }
        }
    )
}
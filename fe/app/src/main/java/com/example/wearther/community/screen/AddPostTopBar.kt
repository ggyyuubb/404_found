package com.example.wearther.community.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddPostTopBar(
    isUploading: Boolean,
    description: String,
    temperature: String,
    weather: String,
    hasImage: Boolean,
    onUploadClick: () -> Unit,
    onBackClick: () -> Unit
) {
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
package com.example.wearther.community.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.wearther.home.recommendation.RecommendationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIRecommendationPickerScreen(
    navController: NavController,
    viewModel: RecommendationViewModel
) {
    val recommendations by viewModel.recommendations.collectAsState()
    val loading by viewModel.loading.collectAsState()
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 추천 코디") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    if (selectedImageUrl != null) {
                        TextButton(
                            onClick = {
                                // TODO: 선택한 이미지 URL을 AddPostScreen으로 전달
                                // Navigation argument로 전달하거나 ViewModel 사용
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("selected_image_url", selectedImageUrl)
                                navController.popBackStack()
                            }
                        ) {
                            Text(
                                "선택",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (recommendations.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "AI 추천 코디가 없습니다",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "홈 화면에서 추천을 받아보세요!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recommendations) { imageUrl ->
                        AIImageItem(
                            imageUrl = imageUrl,
                            isSelected = imageUrl == selectedImageUrl,
                            onClick = { selectedImageUrl = imageUrl }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AIImageItem(
    imageUrl: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF3F4F6))
            .clickable { onClick() }
    ) {
        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = "AI 추천 이미지",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 선택 표시
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "선택됨",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }
    }
}
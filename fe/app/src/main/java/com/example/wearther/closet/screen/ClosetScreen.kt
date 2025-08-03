package com.example.wearther.ui.screens.closet

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.wearther.closet.data.ClosetApi
import com.example.wearther.closet.data.ClosetImage
import com.example.wearther.closet.data.ItemCard
import com.example.wearther.remote.BASE_URL
import com.example.wearther.remote.getStoredJwtToken
import com.example.wearther.ui.screens.closet.components.CategoryTabs
import com.example.wearther.ui.screens.closet.components.EmptyState
import com.example.wearther.ui.screens.closet.components.Header
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun ClosetScreen() {
    val context = LocalContext.current
    val jwtToken = getStoredJwtToken(context)
    val tokenHeader = "Bearer $jwtToken"

    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val closetApi = retrofit.create(ClosetApi::class.java)

    var activeCategory by remember { mutableStateOf("전체") }
    var items by remember { mutableStateOf<List<ClosetImage>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    fun fetchClosetImages(category: String) {
        coroutineScope.launch {
            try {
                val response = closetApi.getMyImages(
                    token = tokenHeader,
                    type = if (category == "전체") null else category
                )
                Log.d("ClosetScreen", "받아온 이미지 개수: ${response.images.size}")
                items = response.images
            } catch (e: Exception) {
                Log.e("ClosetScreen", "옷장 가져오기 실패: ${e.message}")
            }
        }
    }

    fun deleteImage(id: String) {
        coroutineScope.launch {
            try {
                closetApi.deleteImage(tokenHeader, id)
                fetchClosetImages(activeCategory)
            } catch (e: Exception) {
                Log.e("ClosetScreen", "삭제 실패: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchClosetImages(activeCategory)
    }

    val categories = listOf("전체", "상의", "하의", "원피스")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray.copy(alpha = 0.05f))
    ) {
        CategoryTabs(
            categories = categories,
            activeCategory = activeCategory,
            onCategoryChange = {
                activeCategory = it
                fetchClosetImages(it)
            }
        )

        Header(
            totalItems = items.size,
            onSortChange = {} // 정렬 기능은 현재 없음
        )

        if (items.isEmpty()) {
            EmptyState()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp) // ✅ 여기 top 추가
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(items) { item ->
                    ItemCard(
                        imageUrl = item.url,
                        category = item.category,
                        onDelete = { deleteImage(item.id) }
                    )
                }
            }
        }
    }
}

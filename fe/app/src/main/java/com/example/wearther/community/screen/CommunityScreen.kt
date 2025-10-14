package com.example.wearther.community.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.wearther.community.vm.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel = remember { CommunityViewModel.provide(context) }
    val feeds by viewModel.feeds.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFeeds()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("커뮤니티") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: 게시글 작성 */ }
            ) {
                Icon(Icons.Default.Add, contentDescription = "글쓰기")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF7F7F7))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(feeds, key = { it.id }) { feed ->
                    FeedCard(
                        feed = feed,
                        onToggleLike = { viewModel.toggleLike(feed.id) },
                        onCommentClick = { /* TODO */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedCard(
    feed: com.example.wearther.community.data.FeedItem,
    onToggleLike: () -> Unit,
    onCommentClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 프로필 헤더
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E7EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "프로필",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = feed.userName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = feed.postTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 이미지 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(0xFFF3F4F6), MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "코디 이미지",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "코디 이미지",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 설명
            Text(
                text = feed.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF374151),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 하단 정보
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${feed.temperature} ${feed.weather}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2563EB)
                )
                Spacer(modifier = Modifier.weight(1f))

                // 좋아요
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleLike() }
                ) {
                    Icon(
                        imageVector = if (feed.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "좋아요",
                        tint = if (feed.isLiked) Color(0xFFEF4444) else Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = feed.likeCount.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF374151)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 댓글
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCommentClick() }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "댓글",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = feed.commentCount.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF374151)
                    )
                }
            }
        }
    }
}
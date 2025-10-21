package com.example.wearther.community.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wearther.community.vm.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    navController: NavController,
    postId: Int
) {
    val context = LocalContext.current
    val viewModel = remember { CommunityViewModel.provide(context) }
    val feeds by viewModel.feeds.collectAsState()

    var commentText by remember { mutableStateOf("") }

    val post = feeds.find { it.id == postId }

    // 댓글 목록 가져오기
    val comments = remember(postId) {
        viewModel.getCommentsForFeed(postId)
    }

    LaunchedEffect(Unit) {
        viewModel.loadFeeds()
    }

    if (post == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("게시글을 찾을 수 없습니다", color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("돌아가기")
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "게시글",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color(0xFF1F2937)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1F2937)
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = {
                            Text(
                                "댓글을 입력하세요...",
                                color = Color(0xFF9CA3AF)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF3F4F6),
                            unfocusedContainerColor = Color(0xFFF3F4F6)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.addComment(postId, commentText, "현재사용자")
                                commentText = ""
                            }
                        },
                        enabled = commentText.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (commentText.isNotBlank())
                                    Color(0xFF3B82F6)
                                else
                                    Color(0xFFD1D5DB),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "전송",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFAFAFA))
        ) {
            // 게시글 내용
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // 프로필 헤더
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF3B82F6),
                                                Color(0xFF8B5CF6)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    post.userName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF111827)
                                )
                                Text(
                                    post.postTime,
                                    fontSize = 13.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }

                            // 날씨 배지
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFEFF6FF)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = null,
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${post.temperature} ${post.weather}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF1E40AF),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 이미지 영역
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F4F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = Color(0xFFD1D5DB)
                                )
                                Text(
                                    "코디 이미지",
                                    fontSize = 14.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 본문
                        Text(
                            post.description,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF374151)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color(0xFFF3F4F6)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 좋아요/댓글 액션
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // 좋아요
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.toggleLike(postId) }
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (post.isLiked)
                                        Icons.Default.Favorite
                                    else
                                        Icons.Outlined.FavoriteBorder,
                                    contentDescription = "좋아요",
                                    tint = if (post.isLiked)
                                        Color(0xFFEF4444)
                                    else
                                        Color(0xFF6B7280),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "좋아요 ${post.likeCount}개",
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // 댓글
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = "댓글",
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "댓글 ${post.commentCount}개",
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 댓글 섹션 헤더
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFAFA))
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "댓글",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${comments.size}",
                        fontSize = 16.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // 댓글 목록
            items(comments, key = { it.id }) { comment ->
                CommentItem(
                    comment = comment,
                    onLikeClick = {
                        viewModel.toggleCommentLike(postId, comment.id)
                    }
                )
            }

            // 하단 여백
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: com.example.wearther.community.data.Comment,
    onLikeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6),
                            Color(0xFFEC4899)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    comment.userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF111827)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    comment.timestamp,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                comment.content,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF374151)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 댓글 좋아요 버튼 ✅ 활성화!
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onLikeClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (comment.isLiked)
                        Icons.Default.Favorite
                    else
                        Icons.Outlined.FavoriteBorder,
                    contentDescription = "좋아요",
                    tint = if (comment.isLiked)
                        Color(0xFFEF4444)
                    else
                        Color(0xFF9CA3AF),
                    modifier = Modifier.size(16.dp)
                )

                if (comment.likeCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${comment.likeCount}",
                        fontSize = 12.sp,
                        color = if (comment.isLiked)
                            Color(0xFFEF4444)
                        else
                            Color(0xFF9CA3AF),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
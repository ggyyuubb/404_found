package com.example.wearther.community.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wearther.community.data.Comment // Comment data class import
import com.example.wearther.community.data.FeedItem // FeedItem data class import

// --- 1. TopAppBar ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PostDetailTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                "게시글",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
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
}

// --- 2. 댓글 입력 BottomBar ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommentInputBottomBar(
    commentText: String,
    onCommentChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp), // 패딩 조정
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = commentText,
                onValueChange = onCommentChange,
                placeholder = {
                    Text(
                        "댓글을 입력하세요...",
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp // 폰트 크기 조정
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedContainerColor = Color(0xFFF3F4F6),
                    cursorColor = Color(0xFF3B82F6) // 커서 색상 추가
                ),
                maxLines = 5, // 여러 줄 입력 가능하도록
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp) // 입력 텍스트 크기 조정
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSendClick,
                enabled = commentText.isNotBlank(),
                modifier = Modifier
                    .size(40.dp) // 버튼 크기 조정
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
                    modifier = Modifier.size(18.dp) // 아이콘 크기 조정
                )
            }
        }
    }
}

// --- 3. 게시글 내용 카드 ---
@Composable
internal fun PostContentCard(
    post: FeedItem,
    onToggleLike: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp), // 패딩 조정
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // 그림자 조정
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 프로필 헤더
            PostHeader(
                userName = post.userName,
                profileImageUrl = post.userProfileImage, // profileImageUrl 사용
                postTime = post.postTime,
                temperature = post.temperature,
                weather = post.weather
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 이미지 영역 (실제 이미지 로딩 필요 - AsyncImage 사용 예시)
            if (post.outfitImages.isNotEmpty()) {
                // TODO: Coil 등의 라이브러리로 실제 이미지 로드
                // AsyncImage( model = post.outfitImages.first(), ... )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f) // 1:1 비율
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray), // Placeholder
                    contentAlignment = Alignment.Center
                ) {
                    Text("Image Placeholder", color = Color.White)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ImageNotSupported, null, tint = Color.Gray)
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
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF9FAFB)) // Divider 색상 조정
            Spacer(modifier = Modifier.height(12.dp))

            // 좋아요/댓글 액션
            PostActions(
                isLiked = post.isLiked,
                likeCount = post.likeCount,
                commentCount = post.commentCount,
                onToggleLike = onToggleLike
            )
        }
    }
}

// --- 4. 게시글 헤더 --- (209번 줄)
@Composable
private fun PostHeader(
    userName: String,
    profileImageUrl: String?,
    postTime: String,
    temperature: String?,  // ✅ nullable로 변경
    weather: String?       // ✅ nullable로 변경
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // TODO: Coil 등으로 실제 프로필 이미지 로드
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                userName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color(0xFF111827)
            )
            Text(
                postTime,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF)
            )
        }

        // 날씨 배지
        WeatherBadge(
            temperature = temperature ?: "N/A",  // ✅ null 처리
            weather = weather ?: "N/A"           // ✅ null 처리
        )
    }
}

// --- 5. 날씨 배지 ---
@Composable
private fun WeatherBadge(temperature: String, weather: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFEFF6FF)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), // 패딩 조정
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TODO: 날씨에 맞는 아이콘 표시
            Icon(
                imageVector = Icons.Default.WbSunny, // 예시 아이콘
                contentDescription = null,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(13.dp) // 크기 조정
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$temperature $weather",
                style = MaterialTheme.typography.labelSmall, // 스타일 변경
                color = Color(0xFF1E40AF),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// --- 6. 게시글 액션 (좋아요/댓글 수) ---
@Composable
private fun PostActions(
    isLiked: Boolean,
    likeCount: Int,
    commentCount: Int,
    onToggleLike: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp) // 간격 조정
    ) {
        // 좋아요
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onToggleLike)
                .padding(horizontal = 8.dp, vertical = 6.dp) // 패딩 조정
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "좋아요",
                tint = if (isLiked) Color(0xFFEF4444) else Color(0xFF6B7280),
                modifier = Modifier.size(20.dp) // 아이콘 크기 조정
            )
            Spacer(modifier = Modifier.width(5.dp)) // 간격 조정
            Text(
                // "좋아요 ${likeCount}개" -> "${likeCount}" 로 변경 가능성
                "좋아요 ${likeCount}개",
                fontSize = 13.sp, // 폰트 크기 조정
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )
        }

        // 댓글
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp) // 패딩 조정
        ) {
            Icon(
                Icons.Outlined.ChatBubbleOutline,
                contentDescription = "댓글",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(20.dp) // 아이콘 크기 조정
            )
            Spacer(modifier = Modifier.width(5.dp)) // 간격 조정
            Text(
                // "댓글 ${commentCount}개" -> "${commentCount}" 로 변경 가능성
                "댓글 ${commentCount}개",
                fontSize = 13.sp, // 폰트 크기 조정
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )
        }
        // TODO: 공유 버튼 추가 가능성
    }
}


// --- 7. 댓글 섹션 헤더 ---
@Composable
internal fun CommentSectionHeader(commentCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA)) // 배경색 통일
            .padding(horizontal = 16.dp, vertical = 12.dp), // 패딩 조정
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "댓글",
            fontSize = 16.sp, // 크기 조정
            fontWeight = FontWeight.SemiBold, // Bold -> SemiBold
            color = Color(0xFF111827)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "$commentCount", // 숫자만 표시
            fontSize = 15.sp, // 크기 조정
            color = Color(0xFF6B7280)
        )
    }
    HorizontalDivider(thickness = 1.dp, color = Color(0xFFF3F4F6)) // 헤더 아래 구분선
}


// --- 8. 댓글 아이템 ---
@Composable
internal fun CommentItem(
    comment: Comment,
    onLikeClick: () -> Unit
) {
    Column { // Divider를 포함하기 위해 Column으로 감쌈
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* 답글 달기 등 */ } // 댓글 클릭 액션 추가 가능성
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // TODO: Coil 등으로 실제 프로필 이미지 로드
            Box(
                modifier = Modifier
                    .size(32.dp) // 크기 약간 줄임
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        comment.userName,
                        fontWeight = FontWeight.SemiBold, // Bold -> SemiBold
                        fontSize = 13.sp, // 크기 조정
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        comment.timestamp, // TODO: 시간 포맷 변경 ("5분 전" 등)
                        fontSize = 11.sp, // 크기 조정
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

                // 댓글 좋아요 버튼
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onLikeClick)
                        .padding(horizontal = 6.dp, vertical = 3.dp) // 패딩 조정
                ) {
                    Icon(
                        imageVector = if (comment.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "좋아요",
                        tint = if (comment.isLiked) Color(0xFFEF4444) else Color(0xFF9CA3AF),
                        modifier = Modifier.size(14.dp) // 아이콘 크기 조정
                    )

                    // 좋아요 수가 있을 때만 표시 (선택 사항)
                    if (comment.likeCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${comment.likeCount}",
                            fontSize = 11.sp, // 폰트 크기 조정
                            color = if (comment.isLiked) Color(0xFFEF4444) else Color(0xFF9CA3AF),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            // TODO: 댓글 메뉴 버튼 (수정/삭제 등) 추가 가능성
            // IconButton(...) { Icon(Icons.Default.MoreVert, ...) }
        }
        HorizontalDivider(thickness = 1.dp, color = Color(0xFFF9FAFB)) // 댓글 아래 구분선
    }
}
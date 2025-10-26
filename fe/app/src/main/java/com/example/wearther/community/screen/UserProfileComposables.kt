package com.example.wearther.community.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.wearther.community.data.FeedItem
import com.example.wearther.community.data.User

// --- 1. 프로필 헤더 ---
@Composable
internal fun UserProfileHeader(
    user: User,
    onFollowClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(user.profileImage)
                    .crossfade(true)
                    .build(),
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop,
                onLoading = { },
                onError = { }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                user.userName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            if (user.bio.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    user.bio,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProfileStatItem(count = user.postCount, label = "게시글")
                ProfileStatItem(count = user.followerCount, label = "팔로워")
                ProfileStatItem(count = user.followingCount, label = "팔로잉")
            }

            Spacer(modifier = Modifier.height(20.dp))

            FollowButton(
                isFollowing = user.isFollowing,
                onClick = onFollowClick
            )
        }
    }
}

// --- 2. 프로필 통계 아이템 ---
@Composable
private fun ProfileStatItem(count: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = formatStatCount(count),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
        )
    }
}

private fun formatStatCount(count: Int): String {
    return when {
        count >= 10000 -> "${count / 1000}k"
        count >= 1000 -> String.format("%.1fk", count / 1000.0)
        else -> count.toString()
    }
}

// --- 3. 팔로우/팔로잉 버튼 ---
@Composable
private fun FollowButton(isFollowing: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFollowing) Color(0xFFE5E7EB) else Color(0xFF3B82F6),
            contentColor = if (isFollowing) Color(0xFF374151) else Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            imageVector = if (isFollowing) Icons.Default.Check else Icons.Default.PersonAdd,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            if (isFollowing) "팔로잉" else "팔로우",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// --- 4. 사용자 게시글 섹션 헤더 ---
@Composable
internal fun UserProfilePostsHeader(postCount: Int) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFAFAFA))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.GridOn,
                contentDescription = null,
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "게시글",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "$postCount",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )
        }
        HorizontalDivider(thickness = 1.dp, color = Color(0xFFF3F4F6))
    }
}

// --- 5. 게시글 없을 때 표시 ---
@Composable
internal fun EmptyPostsIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Outlined.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = Color(0xFFD1D5DB)
            )
            Text(
                "아직 게시글이 없습니다",
                fontSize = 15.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// --- 6. 사용자 게시글 목록 아이템 ---
@Composable
internal fun UserPostItem(
    post: FeedItem,
    onPostClick: () -> Unit
) {
    val context = LocalContext.current
    val imageUrl = post.outfitImages.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onPostClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (imageUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = "이미지 없음",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFD1D5DB)
                    )
                }
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "게시글 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentScale = ContentScale.Crop,
                    onLoading = { },
                    onError = { }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                post.description,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Icon(
                    imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (post.isLiked) Color(0xFFEF4444) else Color(0xFF9CA3AF)
                )
                Spacer(Modifier.width(4.dp))
                Text("${post.likeCount}", fontSize = 12.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF9CA3AF)
                )
                Spacer(Modifier.width(4.dp))
                Text("${post.commentCount}", fontSize = 12.sp, color = Color(0xFF6B7280))
            }
        }
    }
}
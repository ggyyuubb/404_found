package com.example.wearther.community.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
// 필요한 Compose UI 및 Material 아이콘 import
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Check, GridOn, Person, PersonAdd, Image 등
import androidx.compose.material.icons.outlined.ChatBubbleOutline // 아이콘 추가
import androidx.compose.material.icons.outlined.FavoriteBorder // 아이콘 추가
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale // ContentScale import 추가
import androidx.compose.ui.platform.LocalContext // LocalContext import 추가
// import androidx.compose.ui.res.painterResource // painterResource import 제거
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign // TextAlign import 추가
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // Coil 이미지 로딩 사용 예시
import coil.request.ImageRequest // ImageRequest import 추가
// import com.example.wearther.R // R 파일 import 제거
import com.example.wearther.community.data.FeedItem // FeedItem import
import com.example.wearther.community.data.User // User import

// --- 1. 프로필 헤더 ---
@Composable
internal fun UserProfileHeader(
    user: User,
    onFollowClick: () -> Unit // 팔로우 버튼 클릭 시 동작 전달
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
            // --- [ 💡 수정: placeholder/error 제거 ] ---
            val context = LocalContext.current // Context 가져오기
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(user.profileImage) // User.profileImage 사용 (String?)
                    .crossfade(true) // 부드러운 전환 효과
                    .build(),
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray), // Placeholder 배경색
                contentScale = ContentScale.Crop,
                // 이미지가 null 이거나 로드 실패 시 기본 아이콘 표시 (별도 처리 필요 시)
                onLoading = { /* 로딩 인디케이터 표시 가능 */ },
                onError = { /* 에러 처리 또는 기본 아이콘 Box 표시 */ }
                // fallback, placeholder, error 모두 제거
            )
            // 만약 이미지가 없을 때 기본 아이콘을 꼭 보여줘야 한다면, 아래처럼 Box 사용
            /*
            if (user.profileImage.isNullOrEmpty()) {
                 Box(
                     modifier = Modifier
                         .size(80.dp)
                         .clip(CircleShape)
                         .background(Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)))),
                     contentAlignment = Alignment.Center
                 ) {
                     Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(40.dp))
                 }
            } else {
                 AsyncImage(...) // 위 AsyncImage 코드
            }
            */
            // --- [ 수정 끝 ] ---

            Spacer(modifier = Modifier.height(12.dp))

            // 사용자 이름 (User.userName 사용)
            Text(
                user.userName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            // 자기소개 (User.bio 사용)
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

            // 팔로워/팔로잉/게시글 수 (User 필드 사용)
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProfileStatItem(count = user.postCount, label = "게시글")
                ProfileStatItem(count = user.followerCount, label = "팔로워")
                ProfileStatItem(count = user.followingCount, label = "팔로잉")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 팔로우/팔로잉 버튼 (User.isFollowing 사용)
            FollowButton(
                isFollowing = user.isFollowing,
                onClick = onFollowClick // 전달받은 클릭 리스너 연결
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
            text = formatStatCount(count), // 숫자 포맷팅
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

// 숫자 포맷팅 함수
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
    val context = LocalContext.current // Context 가져오기
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
            // --- [ 💡 수정: placeholder/error 제거 ] ---
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(post.outfitImages.firstOrNull()) // 첫 번째 이미지만 표시
                    .crossfade(true)
                    .build(),
                contentDescription = "게시글 이미지",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // 1:1 비율
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF3F4F6)), // Placeholder 배경색
                contentScale = ContentScale.Crop, // 이미지 스케일 추가
                // 기본 아이콘 Fallback (에러/플레이스홀더 이미지가 없을 경우 대비)
                onLoading = { /* 로딩 인디케이터 표시 가능 */ },
                onError = { /* 에러 처리 또는 기본 아이콘 Box 표시 */ }
                // fallback, placeholder, error 모두 제거
            )
            // --- [ 수정 끝 ] ---

            Spacer(modifier = Modifier.height(12.dp))

            // 내용 일부 표시
            Text(
                post.description,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
                lineHeight = 20.sp // 줄 간격 추가
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 좋아요, 댓글 수
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
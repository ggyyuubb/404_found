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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wearther.community.vm.CommunityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel = remember { CommunityViewModel.provide(context) }
    val feeds by viewModel.feeds.collectAsState()

    // 바텀 시트 상태
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedUserName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadFeeds()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Community",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1F2937)
                )
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // 친구 찾기 FAB
                SmallFloatingActionButton(
                    onClick = { navController.navigate("search_user") },
                    containerColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = "친구 찾기",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 게시글 작성 FAB
                FloatingActionButton(
                    onClick = { navController.navigate("add_post") },
                    containerColor = Color(0xFF3B82F6),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "글쓰기",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFAFAFA))
        ) {
            if (feeds.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFFD1D5DB)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "아직 게시글이 없어요",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "첫 번째 코디를 공유해보세요!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9CA3AF)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(feeds, key = { it.id }) { feed ->
                        FeedCard(
                            feed = feed,
                            onToggleLike = { viewModel.toggleLike(feed.id) },
                            onCommentClick = { navController.navigate("post_detail/${feed.id}") },
                            onCardClick = { navController.navigate("post_detail/${feed.id}") },
                            onProfileClick = {
                                selectedUserName = feed.userName
                                showBottomSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    // 바텀 시트
    if (showBottomSheet) {
        val user = viewModel.getUserByName(selectedUserName)

        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            user?.let { currentUser ->
                ProfileBottomSheetContent(
                    user = currentUser,
                    onViewProfile = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                                navController.navigate("user_profile/${currentUser.userId}")
                            }
                        }
                    },
                    onToggleFollow = {
                        viewModel.toggleFollow(currentUser.userId)
                    },
                    onDismiss = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileBottomSheetContent(
    user: com.example.wearther.community.data.User,
    onViewProfile: () -> Unit,
    onToggleFollow: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 드래그 핸들
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Color(0xFFE5E7EB), RoundedCornerShape(2.dp))
        )

        // 프로필 이미지
        Box(
            modifier = Modifier
                .size(80.dp)
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
                modifier = Modifier.size(40.dp)
            )
        }

        // 사용자 정보
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                user.userName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            if (user.bio.isNotEmpty()) {
                Text(
                    user.bio,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 통계
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${user.postCount}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    "게시글",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${user.followerCount}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    "팔로워",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${user.followingCount}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    "팔로잉",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE5E7EB))

        // 액션 버튼들
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 프로필 보기 버튼
            Button(
                onClick = onViewProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "프로필 보기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 팔로우/언팔로우 버튼
            OutlinedButton(
                onClick = {
                    onToggleFollow()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (user.isFollowing) Color(0xFFF3F4F6) else Color.White,
                    contentColor = if (user.isFollowing) Color(0xFF374151) else Color(0xFF3B82F6)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    if (user.isFollowing) Icons.Default.Check else Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (user.isFollowing) "팔로잉" else "팔로우",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun FeedCard(
    feed: com.example.wearther.community.data.FeedItem,
    onToggleLike: () -> Unit,
    onCommentClick: () -> Unit,
    onCardClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
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
                // 프로필 이미지 (클릭 가능)
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
                        )
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "프로필",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onProfileClick() }
                ) {
                    Text(
                        text = feed.userName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = feed.postTime,
                        style = MaterialTheme.typography.bodySmall,
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
                            text = "${feed.temperature} ${feed.weather}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1E40AF),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 이미지 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "코디 이미지",
                        tint = Color(0xFFD1D5DB),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "코디 이미지",
                        style = MaterialTheme.typography.bodyMedium,
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
                lineHeight = 20.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 구분선
            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFFF3F4F6)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 하단 액션
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 좋아요
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleLike() }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (feed.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "좋아요",
                        tint = if (feed.isLiked) Color(0xFFEF4444) else Color(0xFF6B7280),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = feed.likeCount.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (feed.isLiked) Color(0xFFEF4444) else Color(0xFF374151),
                        fontWeight = FontWeight.Medium
                    )
                }

                // 댓글
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCommentClick() }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "댓글",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = feed.commentCount.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF374151),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 공유 버튼
                IconButton(
                    onClick = { /* TODO: 공유 기능 */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "공유",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
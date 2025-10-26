package com.example.wearther.community.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // items 추가
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wearther.community.vm.CommunityViewModel
import com.example.wearther.community.vm.loadPostsForUser
import com.example.wearther.community.vm.loadUserProfile
import com.example.wearther.community.vm.toggleFollow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    userId: String
) {
    val context = LocalContext.current
    // ViewModel 인스턴스 생성 (remember 사용 권장, 또는 Hilt/Koin 주입)
    val viewModel = remember { CommunityViewModel.provide(context) }

    // --- 데이터 로딩 및 상태 구독 ---
    // 현재 보고 있는 사용자 프로필 상태 구독 (ViewModel에 추가 필요)
    val userProfile by viewModel.viewedUserProfile.collectAsState()
    // 해당 사용자의 게시글 목록 상태 구독 (ViewModel에 추가 필요)
    val userPosts by viewModel.userPosts.collectAsState()
    // 로딩 상태 구독
    val isLoadingProfile by viewModel.isLoadingProfile.collectAsState() // 프로필 로딩 상태 분리 권장
    val isLoadingPosts by viewModel.isLoadingPosts.collectAsState() // 게시글 로딩 상태 분리 권장

    // 화면 진입 시 또는 userId 변경 시 데이터 로드
    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
        viewModel.loadPostsForUser(userId) // 특정 사용자 게시글 로드 함수 호출
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(userProfile?.userName ?: "프로필", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFAFAFA))
        ) {
            // --- 프로필 헤더 ---
            item {
                // 로딩 상태 또는 사용자 정보 없을 때 처리
                if (isLoadingProfile || userProfile == null) {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator() // 로딩 인디케이터
                    }
                } else {
                    // 분리된 UserProfileHeader Composable 사용 (UserProfileComposables.kt 파일에 정의)
                    UserProfileHeader(
                        user = userProfile!!, // non-null 보장 (위에서 체크)
                        onFollowClick = { viewModel.toggleFollow(userId) }
                    )
                }
            }

            // --- 게시글 섹션 헤더 ---
            item {
                UserProfilePostsHeader(postCount = userPosts.size) // 분리된 Composable
            }

            // --- 사용자 게시글 목록 ---
            if (isLoadingPosts) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator() // 로딩 인디케이터
                    }
                }
            } else if (userPosts.isEmpty()) {
                item {
                    EmptyPostsIndicator() // 분리된 Composable
                }
            } else {
                // 분리된 UserPostItem Composable 사용 (UserProfileComposables.kt 파일에 정의)
                items(userPosts, key = { it.id }) { post ->
                    UserPostItem(
                        post = post,
                        onPostClick = {
                            // postId 타입 String으로 전달 확인
                            navController.navigate("post_detail/${post.id}")
                        }
                    )
                }
            }

            // 하단 여백
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
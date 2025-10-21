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
import com.example.wearther.community.data.User
import com.example.wearther.community.vm.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUserScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel = remember { CommunityViewModel.provide(context) }

    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val allUsers = remember { viewModel.getAllUsers() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("친구 찾기", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "뒤로가기",
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFAFAFA))
        ) {
            // 검색창
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchUsers(it)
                    },
                    placeholder = { Text("이름으로 검색", color = Color(0xFF9CA3AF)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                viewModel.clearSearchResults()
                            }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "검색 지우기",
                                    tint = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    ),
                    singleLine = true
                )
            }

            // 결과 목록
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (searchQuery.isEmpty()) {
                    // 추천 사용자
                    item {
                        Text(
                            "추천 친구",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(allUsers.take(5)) { user ->
                        UserListItem(
                            user = user,
                            onClick = { navController.navigate("user_profile/${user.userId}") },
                            onFollowClick = { viewModel.toggleFollow(user.userId) }
                        )
                    }
                } else if (searchResults.isEmpty()) {
                    // 검색 결과 없음
                    item {
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
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color(0xFFD1D5DB)
                                )
                                Text(
                                    "검색 결과가 없습니다",
                                    fontSize = 16.sp,
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "다른 이름을 검색해보세요",
                                    fontSize = 14.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }
                } else {
                    // 검색 결과
                    item {
                        Text(
                            "검색 결과 ${searchResults.size}명",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(searchResults) { user ->
                        UserListItem(
                            user = user,
                            onClick = { navController.navigate("user_profile/${user.userId}") },
                            onFollowClick = { viewModel.toggleFollow(user.userId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserListItem(
    user: User,
    onClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 프로필 이미지
            Box(
                modifier = Modifier
                    .size(56.dp)
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
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 사용자 정보
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.userName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                if (user.bio.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        user.bio,
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 팔로우 버튼
            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (user.isFollowing)
                        Color(0xFFF3F4F6)
                    else
                        Color(0xFF3B82F6)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    if (user.isFollowing) "팔로잉" else "팔로우",
                    fontSize = 14.sp,
                    color = if (user.isFollowing) Color(0xFF374151) else Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


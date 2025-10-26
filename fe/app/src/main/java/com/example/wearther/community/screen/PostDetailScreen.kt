package com.example.wearther.community.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wearther.community.vm.CommunityViewModel
// ViewModel 확장 함수 import 확인 (String 타입으로 수정된 함수)
import com.example.wearther.community.vm.addComment
import com.example.wearther.community.vm.getCommentsForFeed // 사용하지 않는다면 제거 가능
import com.example.wearther.community.vm.loadComments // loadComments 추가
import com.example.wearther.community.vm.loadFeeds // loadFeeds는 postId 불필요
import com.example.wearther.community.vm.toggleCommentLike
import com.example.wearther.community.vm.toggleLike

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    navController: NavController,
    // [ 💡 1. 수정: 파라미터 타입을 Int -> String으로 변경 ]
    postId: String
) {
    val context = LocalContext.current
    val viewModel = remember { CommunityViewModel.provide(context) }
    val feeds by viewModel.feeds.collectAsState()
    val commentsMap by viewModel.comments.collectAsState()

    var commentText by remember { mutableStateOf("") }

    // [ 💡 2. 수정: String ID로 post 찾기 ]
    val post = feeds.find { it.id == postId }

    // postId를 키로 사용하여 commentsMap에서 댓글 목록 가져오기
    val comments = commentsMap[postId] ?: emptyList()

    // 화면 진입 시 & postId 변경 시 댓글 로드
    LaunchedEffect(postId) {
        viewModel.loadComments(postId) // postId는 String
    }

    // (선택 사항) 화면 진입 시 전체 피드 로드 (이미 로드된 상태일 수 있음)
    // LaunchedEffect(Unit) {
    //     viewModel.loadFeeds()
    // }

    if (post == null) {
        // 게시글 없을 때 UI
        PostNotFoundScreen(navController)
        return
    }

    Scaffold(
        topBar = {
            PostDetailTopBar(onBackClick = { navController.popBackStack() })
        },
        bottomBar = {
            CommentInputBottomBar(
                commentText = commentText,
                onCommentChange = { commentText = it },
                onSendClick = {
                    if (commentText.isNotBlank()) {
                        // [ 💡 3. 수정: String ID로 댓글 추가 ]
                        viewModel.addComment(postId, commentText, "현재사용자") // postId는 String
                        commentText = "" // 입력창 비우기
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .fillMaxSize() // LazyColumn이 전체 공간 차지하도록
        ) {
            // 게시글 내용
            item {
                PostContentCard(
                    post = post,
                    onToggleLike = {
                        // [ 💡 4. 수정: String ID로 좋아요 토글 ]
                        viewModel.toggleLike(postId) // postId는 String
                    }
                )
            }

            // 댓글 섹션 헤더
            item {
                CommentSectionHeader(commentCount = comments.size)
            }

            // 댓글 목록
            items(comments, key = { it.id }) { comment ->
                CommentItem(
                    comment = comment,
                    onLikeClick = {
                        // [ 💡 5. 수정: String postId와 Int commentId로 댓글 좋아요 토글 ]
                        viewModel.toggleCommentLike(postId, comment.id) // postId는 String
                    }
                )
            }

            // 하단 여백
            item {
                Spacer(modifier = Modifier.height(16.dp)) // 댓글 입력창 가리지 않도록
            }
        }
    }
}

// 게시글 없을 때 표시하는 Composable (PostDetailComposables.kt로 옮겨도 됨)
@Composable
private fun PostNotFoundScreen(navController: NavController) {
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
}
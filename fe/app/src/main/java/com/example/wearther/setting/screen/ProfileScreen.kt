package com.example.wearther.setting.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle


@Composable // ✅ 이 함수는 Jetpack Compose UI 컴포저블임을 명시
fun ProfileScreen(displayName: String, photoUrl: String?, email: String) {
    Column(
        modifier = Modifier.fillMaxWidth(), // ✅ 가로 폭을 전체로 설정
        horizontalAlignment = Alignment.CenterHorizontally, // ✅ 내부 항목들을 수평 방향으로 가운데 정렬
        verticalArrangement = Arrangement.Center // ✅ 수직 방향으로 가운데 배치
    ) {
        if (!photoUrl.isNullOrEmpty()) { // ✅ 프로필 이미지 URL이 존재하면
            // ✅ 구글 로그인 사용자 → 프로필 이미지 표시
            Image(
                painter = rememberAsyncImagePainter(photoUrl), // ✅ Coil 라이브러리를 사용하여 비동기 이미지 로딩
                contentDescription = "Profile Image", // ✅ 접근성 설명
                modifier = Modifier
                    .size(100.dp) // ✅ 이미지 크기를 100dp로 설정
                    .clip(CircleShape) // ✅ 원형 모양으로 잘라내기
            )
        } else {
            // ✅ 이메일 로그인 사용자 → 기본 아이콘 표시
            Icon(
                imageVector = Icons.Default.AccountCircle, // ✅ Material 기본 아이콘 사용
                contentDescription = "Default Profile", // ✅ 접근성 설명
                modifier = Modifier
                    .size(100.dp) // ✅ 아이콘 크기 설정
                    .clip(CircleShape) // ✅ 원형으로 표시
            )
        }

        Spacer(modifier = Modifier.height(10.dp)) // ✅ 프로필 이미지와 텍스트 사이의 간격

        Text(
            text = "로그인되었습니다!", // ✅ 로그인 상태 안내 텍스트
            style = MaterialTheme.typography.titleMedium // ✅ Material 테마 중간 제목 스타일
        )

        Spacer(modifier = Modifier.height(4.dp)) // ✅ 위 텍스트와 사용자 이름 사이 간격

        Text(
            text = displayName, // ✅ 사용자 이름 또는 닉네임 표시
            style = MaterialTheme.typography.bodyLarge // ✅ 큰 본문 텍스트 스타일
        )

        Spacer(modifier = Modifier.height(4.dp)) // ✅ 이름과 이메일 사이 여백

        Text(
            text = email, // ✅ 사용자 이메일 주소 출력
            style = MaterialTheme.typography.bodySmall // ✅ 작은 본문 텍스트 스타일
        )
    }
}

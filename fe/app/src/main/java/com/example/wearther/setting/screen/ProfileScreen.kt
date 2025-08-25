// com/example/wearther/setting/screen/ProfileScreen.kt
package com.example.wearther.setting.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun ProfileScreen(
    displayName: String,
    photoUrl: String?,     // 프로필 사진 URL (없으면 기본 아이콘)
    email: String,
    onPhotoUrlChanged: (String) -> Unit,     // 업로드 후 새 URL을 상위에 반영
    uploadPhotoFile: suspend (File) -> String // 파일 업로드 함수 (서버/스토리지 업로드)
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ✅ 프로필 이미지: 클릭 → 포토피커 → 업로드 → onPhotoUrlChanged 호출
        ProfileImage(
            photoUrl = photoUrl,
            onPhotoChanged = { newUrl ->
                // 서버에서 받은 원본 URL을 그대로 전달 (타임스탬프 추가하지 않음)
                onPhotoUrlChanged(newUrl)
            },
            uploadPhotoFile = uploadPhotoFile
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "로그인되었습니다!",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = displayName,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = email,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
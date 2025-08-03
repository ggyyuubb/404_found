package com.example.wearther.setting.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.SetOptions


// ✅ 필요한 라이브러리 임포트 (주석 생략 요청에 따라 설명하지 않음)

@OptIn(ExperimentalMaterial3Api::class) // ✅ Material3의 실험적 API 사용 허용
@Composable
fun EditNicknameScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser // ✅ 현재 로그인된 사용자 정보 가져오기
    val context = LocalContext.current // ✅ 현재 Context 가져오기 (Toast 등에 사용)
    var nickname by remember { mutableStateOf("") } // ✅ 입력될 닉네임을 저장할 상태 변수

    // ✅ 화면이 처음 구성될 때 사용자 닉네임을 Firestore에서 불러오기
    LaunchedEffect(Unit) {
        user?.uid?.let { uid -> // ✅ 사용자 UID가 null이 아닐 때만 실행
            Firebase.firestore.collection("users").document(uid) // ✅ Firestore에서 해당 UID의 문서 접근
                .get() // ✅ 데이터 가져오기 요청
                .addOnSuccessListener { doc -> // ✅ 성공 시
                    nickname = doc.getString("nickname") ?: "" // ✅ 닉네임 필드 값 가져와 상태에 반영
                }
        }
    }

    // ✅ 화면 전체 구조를 Scaffold로 정의 (상단 바 포함)
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("닉네임 수정") }) // ✅ 상단 앱 바 제목 설정
        }
    ) { padding -> // ✅ Scaffold 안의 본문 컨텐츠 정의
        Column(
            modifier = Modifier
                .padding(padding) // ✅ 시스템 UI 패딩 반영
                .padding(24.dp), // ✅ 사용자 지정 여백
            horizontalAlignment = Alignment.CenterHorizontally // ✅ 수평 중앙 정렬
        ) {
            // ✅ 닉네임 입력 필드
            TextField(
                value = nickname, // ✅ 현재 닉네임 상태값 표시
                onValueChange = { nickname = it }, // ✅ 입력값 변경 시 상태 업데이트
                label = { Text("새 닉네임") }, // ✅ 라벨 텍스트
                singleLine = true, // ✅ 한 줄 입력만 허용
                modifier = Modifier.fillMaxWidth() // ✅ 너비 전체 사용
            )

            Spacer(modifier = Modifier.height(24.dp)) // ✅ 입력 필드와 버튼 사이 여백

            // ✅ 저장 버튼
            Button(
                onClick = {
                    user?.uid?.let { uid -> // ✅ UID가 있을 경우에만 저장 처리
                        Firebase.firestore.collection("users").document(uid) // ✅ 사용자 문서 접근
                            .set(mapOf("nickname" to nickname), SetOptions.merge()) // ✅ 닉네임 필드만 병합하여 저장
                            .addOnSuccessListener {
                                Toast.makeText(context, "닉네임 저장 완료!", Toast.LENGTH_SHORT).show() // ✅ 성공 메시지
                                navController.popBackStack() // ✅ 이전 화면으로 이동
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "저장 실패: ${it.message}", Toast.LENGTH_SHORT).show() // ✅ 실패 메시지
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth() // ✅ 버튼 너비 전체 사용
            ) {
                Text("저장") // ✅ 버튼 텍스트
            }
        }
    }
}

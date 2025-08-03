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
import at.favre.lib.crypto.bcrypt.BCrypt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current // ✅ 현재 컴포저블의 Context 가져오기
    val auth = FirebaseAuth.getInstance() // ✅ Firebase 인증 객체 가져오기

    var email by remember { mutableStateOf("") } // ✅ 사용자 이메일 상태 저장
    var password by remember { mutableStateOf("") } // ✅ 사용자 비밀번호 상태 저장

    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$") // ✅ 이메일 형식 정규식
    val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$") // ✅ 비밀번호 형식: 영문+숫자 포함 6자 이상

    val isEmailValid = email.matches(emailRegex) // ✅ 현재 이메일이 유효한지 여부
    val isPasswordValid = password.matches(passwordRegex) // ✅ 현재 비밀번호가 유효한지 여부

    Column(
        modifier = Modifier
            .fillMaxSize() // ✅ 전체 화면 채우기
            .padding(16.dp), // ✅ 좌우 여백 추가
        horizontalAlignment = Alignment.CenterHorizontally // ✅ 중앙 정렬
    ) {
        Text("회원가입", style = MaterialTheme.typography.headlineMedium) // ✅ 제목 텍스트
        Spacer(modifier = Modifier.height(24.dp)) // ✅ 여백 추가

        Text("이메일", style = MaterialTheme.typography.bodyMedium) // ✅ 이메일 필드 라벨
        Spacer(modifier = Modifier.height(4.dp)) // ✅ 라벨 아래 간격

        TextField(
            value = email, // ✅ 입력된 이메일 값
            onValueChange = { email = it }, // ✅ 값 변경 시 상태 업데이트
            placeholder = { Text("이메일을 입력하세요") }, // ✅ 힌트 텍스트
            isError = email.isNotEmpty() && !isEmailValid, // ✅ 형식이 틀릴 경우 빨간 테두리
            modifier = Modifier.fillMaxWidth() // ✅ 전체 너비 사용
        )

        if (email.isNotEmpty() && !isEmailValid) {
            Text(
                text = "유효하지 않은 이메일 주소입니다.", // ✅ 유효성 오류 메시지
                color = MaterialTheme.colorScheme.error, // ✅ 오류 색상
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp)) // ✅ 이메일 입력과 비밀번호 사이 여백

        Text("비밀번호", style = MaterialTheme.typography.bodyMedium) // ✅ 비밀번호 필드 라벨
        Spacer(modifier = Modifier.height(4.dp))

        TextField(
            value = password, // ✅ 입력된 비밀번호 값
            onValueChange = { password = it }, // ✅ 값 변경 시 상태 업데이트
            placeholder = { Text("영문+숫자 포함, 6자 이상") }, // ✅ 힌트 텍스트
            isError = password.isNotEmpty() && !isPasswordValid, // ✅ 형식 틀리면 오류 표시
            modifier = Modifier.fillMaxWidth()
        )

        if (password.isNotEmpty() && !isPasswordValid) {
            Text(
                text = "비밀번호는 영문과 숫자를 포함한 6자 이상이어야 합니다.", // ✅ 유효성 메시지
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp)) // ✅ 입력 필드와 버튼 사이 여백

        Button(
            onClick = {
                // ✅ 1차 입력값 확인
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // ✅ 이메일 형식 검증
                if (!isEmailValid) {
                    Toast.makeText(context, "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // ✅ 비밀번호 형식 검증
                if (!isPasswordValid) {
                    Toast.makeText(context, "비밀번호 형식을 확인해주세요.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // ✅ Firebase에 이메일 회원가입 요청
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser // ✅ 회원가입된 사용자 객체
                            val uid = user?.uid ?: return@addOnCompleteListener // ✅ UID 없으면 종료
                            val nickname = email.substringBefore("@") // ✅ 이메일 앞부분을 닉네임으로 사용

                            val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray()) // ✅ 비밀번호 해싱
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // ✅ 날짜 포맷 정의
                            val createdAt = LocalDateTime.now().format(formatter) // ✅ 현재 시간 포맷팅

                            // ✅ Firestore에 저장할 유저 정보 맵
                            val userData = mapOf(
                                "user_id" to uid,
                                "email" to (user.email ?: ""),
                                "password" to hashedPassword,
                                "nickname" to nickname,
                                "created_at" to createdAt
                            )

                            // ✅ Firestore "users" 컬렉션에 사용자 문서 저장
                            Firebase.firestore.collection("users")
                                .document(uid)
                                .set(userData, SetOptions.merge()) // ✅ 덮어쓰기 방지
                                .addOnSuccessListener {
                                    Toast.makeText(context, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack() // ✅ 뒤로 가기 (설정 화면 등으로)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "사용자 정보 저장 실패", Toast.LENGTH_SHORT).show()
                                }

                        } else {
                            val errorMessage = task.exception?.message ?: "회원가입 실패"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show() // ✅ 에러 메시지 출력
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth(), // ✅ 버튼 너비 전체
            enabled = isEmailValid && isPasswordValid // ✅ 형식 유효할 때만 클릭 가능
        ) {
            Text("회원가입") // ✅ 버튼 텍스트
        }
    }
}

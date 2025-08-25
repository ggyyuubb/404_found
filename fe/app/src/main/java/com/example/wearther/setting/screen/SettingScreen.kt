package com.example.wearther.setting.screen

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.wearther.setting.auth.GoogleSignInHelper
import com.example.wearther.setting.auth.handleEmailLogin
import com.example.wearther.setting.data.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun SettingScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    var user by remember { mutableStateOf(auth.currentUser) }

    val context = LocalContext.current
    val activity = context as Activity

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

    // ✅ Google 로그인 런처 등록
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        GoogleSignInHelper.handleSignInResult(activity, result) {
            // 로그인 성공 후 처리: user 상태 업데이트
            user = FirebaseAuth.getInstance().currentUser
            Log.d("GoogleLogin", "🎉 구글 로그인 성공 후 UI 업데이트")
        }
    }

    // ✅ 런처를 GoogleSignInHelper에 등록
    LaunchedEffect(Unit) {
        GoogleSignInHelper.setLauncher(googleSignInLauncher)
        Log.d("GoogleLogin", "✅ Google 로그인 런처 등록 완료")
    }

    // ✅ 로그인된 사용자의 Firestore 닉네임 불러오기
    LaunchedEffect(user) {
        user?.let {
            Firebase.firestore.collection("users").document(it.uid)
                .get()
                .addOnSuccessListener { doc ->
                    nickname = doc.getString("nickname")
                        ?: it.displayName
                                ?: it.email
                                ?: "사용자"
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("설정", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(24.dp))

        if (user != null) {
            // ProfileRepository 추가
            val profileRepository = remember { ProfileRepository(context) }

            ProfileScreen(
                displayName = "$nickname 님",
                photoUrl = user?.photoUrl?.toString(),
                email = user?.email ?: user?.displayName ?: "사용자",
                onPhotoUrlChanged = { newUrl ->
                    Log.d("ProfileScreen", "새 프로필 사진 URL: $newUrl")
                    // Firestore에도 업데이트
                    user?.let { currentUser ->
                        Firebase.firestore.collection("users").document(currentUser.uid)
                            .update("profile_image", newUrl)
                            .addOnSuccessListener {
                                Log.d("ProfileScreen", "Firestore 프로필 이미지 업데이트 성공")
                            }
                            .addOnFailureListener { e ->
                                Log.e("ProfileScreen", "Firestore 업데이트 실패", e)
                            }
                    }
                },
                // SettingScreen.kt의 uploadPhotoFile 부분 수정
                uploadPhotoFile = { file ->
                    try {
                        Log.d("ProfileScreen", "JWT 토큰 테스트 시작")
                        val isValidToken = profileRepository.testJwtToken()

                        if (!isValidToken) {
                            Log.e("ProfileScreen", "JWT 토큰이 유효하지 않음")
                            throw IllegalStateException("로그인이 만료되었습니다. 다시 로그인해주세요.")
                        }

                        Log.d("ProfileScreen", "JWT 토큰 유효, 파일 업로드 시작: ${file.name}")
                        val url = profileRepository.uploadPhoto(file)
                        Log.d("ProfileScreen", "파일 업로드 성공: $url")
                        url
                    } catch (e: Exception) {
                        Log.e("ProfileScreen", "파일 업로드 실패", e)
                        throw e
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Text("계정", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                onClick = { navController.navigate("edit_nickname") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("닉네임")
                    Text(nickname, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    auth.signOut()
                    user = null
                    Log.d("Login", "✅ 로그아웃 완료")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape
            ) {
                Text("로그아웃")
            }

        } else {
            EmailLoginSection(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                onLoginClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                        return@EmailLoginSection
                    }

                    handleEmailLogin(
                        email = email,
                        password = password,
                        context = context,
                        navController = navController,
                        onUserUpdated = { user = it }
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    Log.d("GoogleLogin", "✅ 구글 로그인 클릭")
                    GoogleSignInHelper.signIn(activity)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape
            ) {
                Text("구글 로그인")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { navController.navigate("register") }) {
                Text("계정이 없으신가요? 회원가입")
            }
        }
    }
}

@Composable
fun EmailLoginSection(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    TextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("이메일") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("비밀번호") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.height(56.dp),
                    shape = RectangleShape
                ) {
                    Text("로그인")
                }
            }
        }
    }
}
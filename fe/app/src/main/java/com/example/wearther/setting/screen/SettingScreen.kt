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

    // ✅ 프로필 사진 URL 상태 추가
    var photoUrl by remember { mutableStateOf<String?>(null) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        GoogleSignInHelper.handleSignInResult(activity, result) {
            user = FirebaseAuth.getInstance().currentUser
            // ✅ 로그인 후 프로필 사진도 업데이트
            photoUrl = user?.photoUrl?.toString()
            Log.d("GoogleLogin", "🎉 구글 로그인 성공 후 UI 업데이트")
        }
    }

    LaunchedEffect(Unit) {
        GoogleSignInHelper.setLauncher(googleSignInLauncher)
        Log.d("GoogleLogin", "✅ Google 로그인 런처 등록 완료")
    }

    // ✅ 로그인된 사용자의 Firestore 데이터 불러오기 (닉네임 + 프로필 사진)
    LaunchedEffect(user) {
        user?.let { currentUser ->
            // Firebase Auth의 photoUrl 먼저 설정
            photoUrl = currentUser.photoUrl?.toString()
            Log.d("SettingScreen", "Firebase Auth photoUrl: $photoUrl")

            // Firestore에서 저장된 프로필 사진이 있는지 확인
            Firebase.firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { doc ->
                    nickname = doc.getString("nickname")
                        ?: currentUser.displayName
                                ?: currentUser.email
                                ?: "사용자"

                    // ✅ Firestore에 저장된 프로필 사진 URL이 있으면 우선 사용
                    val firestorePhotoUrl = doc.getString("profile_image")
                    if (!firestorePhotoUrl.isNullOrEmpty()) {
                        photoUrl = firestorePhotoUrl
                        Log.d("SettingScreen", "Firestore photoUrl: $firestorePhotoUrl")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("SettingScreen", "Firestore 데이터 로드 실패", e)
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
            val profileRepository = remember { ProfileRepository(context) }

            Log.d("SettingScreen", "ProfileScreen에 전달할 photoUrl: $photoUrl")

            ProfileScreen(
                displayName = "$nickname 님",
                photoUrl = photoUrl, // ✅ 상태로 관리되는 photoUrl 전달
                email = user?.email ?: user?.displayName ?: "사용자",
                onPhotoUrlChanged = { newUrl ->
                    Log.d("SettingScreen", "🖼️ 새 프로필 사진 URL 받음: $newUrl")

                    // ✅ UI 즉시 업데이트
                    photoUrl = newUrl

                    // Firestore에도 저장
                    user?.let { currentUser ->
                        Firebase.firestore.collection("users").document(currentUser.uid)
                            .update("profile_image", newUrl)
                            .addOnSuccessListener {
                                Log.d("SettingScreen", "✅ Firestore 프로필 이미지 업데이트 성공")
                            }
                            .addOnFailureListener { e ->
                                Log.e("SettingScreen", "❌ Firestore 업데이트 실패", e)
                            }
                    }
                },
                uploadPhotoFile = { file ->
                    try {
                        Log.d("SettingScreen", "🔐 JWT 토큰 테스트 시작")
                        val isValidToken = profileRepository.testJwtToken()

                        if (!isValidToken) {
                            Log.e("SettingScreen", "❌ JWT 토큰이 유효하지 않음")
                            throw IllegalStateException("로그인이 만료되었습니다. 다시 로그인해주세요.")
                        }

                        Log.d("SettingScreen", "✅ JWT 토큰 유효, 파일 업로드 시작: ${file.name}")
                        val url = profileRepository.uploadPhoto(file)
                        Log.d("SettingScreen", "✅ 파일 업로드 성공: $url")
                        url
                    } catch (e: Exception) {
                        Log.e("SettingScreen", "❌ 파일 업로드 실패", e)
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
                    photoUrl = null // ✅ 로그아웃 시 프로필 사진도 초기화
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
                        onUserUpdated = {
                            user = it
                            // ✅ 이메일 로그인 후 프로필 사진도 로드
                            photoUrl = it?.photoUrl?.toString()
                        }
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
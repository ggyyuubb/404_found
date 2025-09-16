package com.example.wearther.setting.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.example.wearther.remote.sendTokenToBackend
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// 🔹 Google 로그인을 위한 헬퍼 객체 선언 (싱글톤으로 사용)
object GoogleSignInHelper {

    private lateinit var launcher: ActivityResultLauncher<Intent> // 외부에서 setLauncher로 등록하는 로그인 실행 런처
    private val auth = FirebaseAuth.getInstance() // Firebase 인증 인스턴스

    // 외부 Activity에서 ActivityResultLauncher를 주입하는 함수
    fun setLauncher(activityResultLauncher: ActivityResultLauncher<Intent>) {
        launcher = activityResultLauncher
    }

    // Google 로그인 요청을 시작하는 함수
    fun signIn(activity: Activity) {
        // launcher가 초기화되었는지 확인
        if (!::launcher.isInitialized) {
            Log.e("GoogleSignInHelper", "❌ Launcher가 초기화되지 않았습니다. setLauncher()를 먼저 호출하세요.")
            return
        }

        // 로그인 옵션 설정: ID 토큰과 이메일 요청
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("932275548518-a9hddce94fnvo4g17n79b1emfm0c7ft0.apps.googleusercontent.com") // Firebase 웹 클라이언트 ID
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(activity, gso) // GoogleSignInClient 생성

        // 이미 로그인된 세션을 로그아웃 후 다시 로그인 시도 (계정 선택창 항상 보이게 하기 위함)
        client.signOut().addOnCompleteListener {
            if (it.isSuccessful) {
                launcher.launch(client.signInIntent) // 로그인 인텐트 실행
            } else {
                Log.e("GoogleSignInHelper", "로그아웃 실패") // 로그아웃 실패 시 로그 출력
            }
        }
    }

    // 로그인 후 결과를 처리하는 함수
    fun handleSignInResult(activity: Activity, result: ActivityResult, onJwtStored: () -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data) // 로그인 결과 Intent로부터 계정 정보를 얻음
        try {
            val account = task.getResult(ApiException::class.java) // 실패할 경우 예외 발생
            val credential = GoogleAuthProvider.getCredential(account.idToken, null) // ID 토큰 기반 Firebase 자격 증명 생성
            val auth = FirebaseAuth.getInstance()

            // Firebase에 자격증명을 사용하여 로그인 요청
            auth.signInWithCredential(credential)
                .addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        Log.d("GoogleLogin", "✅ 로그인 성공: ${authResult.result.user?.email}") // 로그인 성공 시 이메일 로그

                        val user = auth.currentUser // 현재 로그인된 Firebase 사용자

                        if (user != null) {
                            user.getIdToken(true).addOnSuccessListener { result ->
                                val token = result.token // Firebase ID 토큰 획득
                                Log.d("BackendToken", "📦 구글 로그인 후 토큰: $token")

                                if (!token.isNullOrEmpty()) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        val success = sendTokenToBackend(token, activity) // 백엔드에 토큰 전송

                                        if (success) {
                                            storeJwtLocally(token, activity) // SharedPreferences에 JWT 저장
                                            Log.d("BackendToken", "🎉 JWT 저장 성공 → 후처리 시작")
                                            delay(100) // 앱 상태 반영을 위한 약간의 지연
                                            onJwtStored() // 콜백 실행 (예: 화면 전환)
                                        } else {
                                            Log.e("BackendToken", "❌ JWT 저장 실패")
                                        }
                                    }
                                } else {
                                    Log.e("BackendToken", "❌ Firebase ID 토큰이 null 또는 empty")
                                }
                            }
                        } else {
                            Log.e("GoogleLogin", "❌ 사용자 없음") // Firebase 인증은 성공했지만 사용자 정보 없음
                        }
                    } else {
                        Log.e("GoogleLogin", "❌ 파이어베이스 로그인 실패", authResult.exception) // Firebase 로그인 실패
                    }
                }
        } catch (e: ApiException) {
            Log.e("GoogleLogin", "❌ Google 로그인 실패, 상태 코드: ${e.statusCode}", e) // Google 로그인 실패 예외
        }
    }

    // JWT 토큰을 로컬 SharedPreferences에 저장하는 함수
    fun storeJwtLocally(token: String, context: Context) {
        try {
            val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("jwt", token) // "jwt" 키로 토큰 저장
                apply() // 비동기 저장
            }
            Log.d("JWT_STORAGE", "✅ JWT 토큰 저장 성공: $token")
        } catch (e: Exception) {
            Log.e("JWT_STORAGE", "❌ JWT 저장 실패: ${e.message}", e) // 저장 중 예외 발생
        }
    }
}
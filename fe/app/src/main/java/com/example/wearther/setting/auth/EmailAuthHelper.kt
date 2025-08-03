package com.example.wearther.setting.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavController
import com.example.wearther.remote.sendTokenToBackend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ✅ 이메일과 비밀번호로 로그인하고 JWT를 받아 SharedPreferences에 저장한 뒤 옷장 화면으로 이동
fun handleEmailLogin(
    email: String,
    password: String,
    context: Context,
    navController: NavController,
    onUserUpdated: (FirebaseUser?) -> Unit
) {
    val auth = FirebaseAuth.getInstance()

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                onUserUpdated(user)

                user?.getIdToken(true)?.addOnSuccessListener { result ->
                    val token = result.token
                    if (!token.isNullOrBlank()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            val success = sendTokenToBackend(token, context)
                            if (success) {
                                Log.d("Login", "✅ JWT 저장 성공")
                            } else {
                                Toast.makeText(context, "JWT 저장 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.e("BackendToken", "❌ Firebase ID 토큰 없음")
                    }
                }
            } else {
                Log.e("EmailLogin", "❌ 로그인 실패", task.exception)
                Toast.makeText(context, "이메일 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
}

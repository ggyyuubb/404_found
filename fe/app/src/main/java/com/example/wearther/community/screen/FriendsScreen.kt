package com.example.wearther.community.screen

import android.util.Log
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController

@Composable
fun FriendsScreen(navController: NavController) {
    Log.d("FriendsScreen", "FriendsScreen 시작")

    val context = LocalContext.current
    Log.d("FriendsScreen", "Context 획득: ${context::class.java.simpleName}")

    AndroidView(
        factory = { ctx ->
            Log.d("FriendsScreen", "AndroidView factory 실행")
            try {
                FragmentContainerView(ctx).apply {
                    id = View.generateViewId()
                    Log.d("FriendsScreen", "FragmentContainerView 생성, ID: $id")
                }
            } catch (e: Exception) {
                Log.e("FriendsScreen", "FragmentContainerView 생성 에러: ${e.message}", e)
                throw e
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { fragmentContainer ->
            Log.d("FriendsScreen", "AndroidView update 실행")
            try {
                val fragmentManager = (context as FragmentActivity).supportFragmentManager
                Log.d("FriendsScreen", "FragmentManager 획득")

                val fragment = CommunityFragment()
                Log.d("FriendsScreen", "CommunityFragment 생성")

                fragmentManager.beginTransaction()
                    .replace(fragmentContainer.id, fragment)
                    .commit()
                Log.d("FriendsScreen", "Fragment 트랜잭션 커밋 완료")
            } catch (e: Exception) {
                Log.e("FriendsScreen", "Fragment 연결 에러: ${e.message}", e)
            }
        }
    )
}
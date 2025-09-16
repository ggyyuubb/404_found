package com.example.wearther

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wearther.setting.auth.GoogleSignInHelper
import com.example.wearther.navigation.NavGraph
import com.example.wearther.theme.WeartherTheme

class MainActivity : FragmentActivity() {

    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkLocationPermission()

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            GoogleSignInHelper.handleSignInResult(this, result) {
                Log.d("GoogleLogin", "🎯 JWT 저장 이후 후처리 실행됨")
                recreate()
            }
        }

        // ✅ 반드시 있어야 앱이 화면을 띄움
        setContent {
            WeartherTheme {
                NavGraph()
            }
        }
    }

    private fun checkLocationPermission() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
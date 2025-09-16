package com.example.wearther.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionUtils {
    
    // 필요한 권한들
    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    
    // 권한 설명들
    val PERMISSION_DESCRIPTIONS = mapOf(
        Manifest.permission.ACCESS_FINE_LOCATION to "정확한 위치 정보를 사용하여 날씨 기반 옷차림을 추천해드립니다.",
        Manifest.permission.ACCESS_COARSE_LOCATION to "대략적인 위치 정보를 사용하여 날씨 기반 옷차림을 추천해드립니다.",
        Manifest.permission.CAMERA to "사진 촬영을 통해 옷장에 옷을 추가할 수 있습니다.",
        Manifest.permission.READ_EXTERNAL_STORAGE to "갤러리에서 사진을 선택하여 옷장에 추가할 수 있습니다.",
        Manifest.permission.WRITE_EXTERNAL_STORAGE to "촬영한 사진을 갤러리에 저장할 수 있습니다."
    )
    
    // 권한이 이미 허용되었는지 확인
    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    // 특정 권한이 허용되었는지 확인
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    // 권한 그룹별로 분류
    fun getPermissionGroups(): Map<String, List<String>> {
        return mapOf(
            "위치 정보" to listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            "카메라" to listOf(Manifest.permission.CAMERA),
            "앨범" to listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }
} 
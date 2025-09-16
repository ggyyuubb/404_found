package com.example.wearther.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.wearther.R
import com.example.wearther.dialog.PermissionRequestDialog

class PermissionManager(private val activity: FragmentActivity) {
    
    private var permissionCallback: ((Map<String, Boolean>) -> Unit)? = null
    private val permissionResults = mutableMapOf<String, Boolean>()
    
    // 권한 요청 런처
    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { (permission, isGranted) ->
            permissionResults[permission] = isGranted
        }
        permissionCallback?.invoke(permissionResults.toMap())
    }
    
    // 권한 그룹별 설명
    private val permissionGroupDescriptions = mapOf(
        "위치 정보" to "정확한 위치 정보를 사용하여 날씨 기반 옷차림을 추천해드립니다.",
        "카메라" to "사진 촬영을 통해 옷장에 옷을 추가할 수 있습니다.",
        "앨범" to "갤러리에서 사진을 선택하여 옷장에 추가할 수 있습니다."
    )
    
    // 권한 그룹별 아이콘
    private val permissionGroupIcons = mapOf(
        "위치 정보" to R.drawable.ic_permission_location,
        "카메라" to R.drawable.ic_permission_camera,
        "앨범" to R.drawable.ic_permission_gallery
    )
    
    /**
     * 모든 권한을 순차적으로 요청
     */
    fun requestAllPermissions(callback: (Map<String, Boolean>) -> Unit) {
        permissionCallback = callback
        permissionResults.clear()
        
        val permissionGroups = PermissionUtils.getPermissionGroups()
        requestPermissionsSequentially(permissionGroups.keys.toList(), permissionGroups)
    }
    
    /**
     * 권한을 순차적으로 요청
     */
    private fun requestPermissionsSequentially(
        remainingGroups: List<String>,
        permissionGroups: Map<String, List<String>>
    ) {
        if (remainingGroups.isEmpty()) {
            permissionCallback?.invoke(permissionResults.toMap())
            return
        }
        
        val currentGroup = remainingGroups.first()
        val permissions = permissionGroups[currentGroup] ?: emptyList()
        
        // 이미 모든 권한이 허용된 경우 다음 그룹으로
        if (permissions.all { PermissionUtils.hasPermission(activity, it) }) {
            permissions.forEach { permission ->
                permissionResults[permission] = true
            }
            requestPermissionsSequentially(remainingGroups.drop(1), permissionGroups)
            return
        }
        
        // 권한 요청 다이얼로그 표시
        showPermissionDialog(currentGroup) { isAllowed ->
            if (isAllowed) {
                // 시스템 권한 요청
                permissionLauncher.launch(permissions.toTypedArray())
            } else {
                // 권한 거부 처리
                permissions.forEach { permission ->
                    permissionResults[permission] = false
                }
                requestPermissionsSequentially(remainingGroups.drop(1), permissionGroups)
            }
        }
    }
    
    /**
     * 권한 요청 다이얼로그 표시
     */
    private fun showPermissionDialog(
        permissionGroup: String,
        onResult: (Boolean) -> Unit
    ) {
        val description = permissionGroupDescriptions[permissionGroup] ?: ""
        
        val dialog = PermissionRequestDialog.newInstance(
            permissionGroup = permissionGroup,
            permissionDescription = description,
            onResult = onResult
        )
        
        dialog.show(activity.supportFragmentManager, "PermissionDialog")
    }
    
    /**
     * 특정 권한이 거부되었는지 확인
     */
    fun shouldShowPermissionRationale(permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    /**
     * 설정 화면으로 이동하는 다이얼로그 표시
     */
    fun showSettingsDialog(permissionGroup: String) {
        AlertDialog.Builder(activity)
            .setTitle("$permissionGroup 권한 필요")
            .setMessage("이 기능을 사용하려면 설정에서 권한을 허용해주세요.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    /**
     * 앱 설정 화면 열기
     */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }
    
    /**
     * 권한 상태 확인
     */
    fun getPermissionStatus(): Map<String, Boolean> {
        val status = mutableMapOf<String, Boolean>()
        PermissionUtils.REQUIRED_PERMISSIONS.forEach { permission ->
            status[permission] = PermissionUtils.hasPermission(activity, permission)
        }
        return status
    }
} 
package com.example.wearther.closet.upload

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun PhotoSourceSelectionDialog(
    onDismiss: () -> Unit,
    onImageSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // 카메라 런처
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            onImageSelected(cameraImageUri!!)
            onDismiss()
        }
    }

    // 갤러리 런처
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onImageSelected(it)
            onDismiss()
        }
    }

    // 카메라 실행 함수
    fun launchCamera() {
        try {
            val photoFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "IMG_${System.currentTimeMillis()}.jpg"
            )

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )

            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "카메라 실행 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 카메라 권한 런처
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(context, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 갤러리 권한 런처
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "갤러리 접근 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 갤러리 실행 함수
    fun launchGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            galleryLauncher.launch("image/*")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 🔹 헤더 아이콘
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // 🔹 타이틀
                Text(
                    "사진 추가",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    "사진을 불러올 방법을 선택하세요",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                // 🔹 버튼 영역
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 카메라 버튼
                    ElevatedButton(
                        onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("카메라로 촬영하기")
                    }

                    // 갤러리 버튼
                    OutlinedButton(
                        onClick = { launchGallery() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("갤러리에서 선택하기")
                    }
                }

                Divider(modifier = Modifier.padding(top = 8.dp))

                // 🔹 팁 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "촬영 TIP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "• 옷을 평평하게 펼쳐서 촬영해주세요\n• 옷을 정중앙에 배치하면 정확도가 높아집니다\n• 사람이 입지 않은 상태가 더 정확합니다",
                                fontSize = 11.sp,
                                color = Color.DarkGray,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // 🔹 취소 버튼
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("취소", color = Color.Gray)
                }
            }
        }
    }
}
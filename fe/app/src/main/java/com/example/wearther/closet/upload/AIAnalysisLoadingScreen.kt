package com.example.wearther.closet.upload

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wearther.remote.BASE_URL
import com.example.wearther.remote.getStoredJwtToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

@Composable
fun AIAnalysisLoadingScreen(
    imageUri: Uri,
    onAnalysisComplete: (AIAnalysisResult) -> Unit
) {
    val context = LocalContext.current

    // 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // ✅ 실제 AI 분석 호출 (더미 제거)
    LaunchedEffect(imageUri) {
        try {
            val jwtToken = getStoredJwtToken(context)
            if (jwtToken == null) {
                Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                return@LaunchedEffect
            }

            val retrofit = Retrofit.Builder()
                .baseUrl(if (BASE_URL.endsWith("/")) BASE_URL else "$BASE_URL/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(UploadApi::class.java)

            val file = uriToFile(context, imageUri)
            val imagePart = MultipartBody.Part.createFormData(
                "image",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )

            val resp = withContext(Dispatchers.IO) {
                api.analyzeOnly("Bearer $jwtToken", imagePart)
            }
            file.delete()

            if (resp.success != true) {
                Toast.makeText(context, resp.error ?: "AI 분석 실패", Toast.LENGTH_LONG).show()
                return@LaunchedEffect
            }

            val result = AIAnalysisResult(
                type = resp.type ?: "",        // ⬅️ "상의" 대신 ""
                category = resp.category ?: "",  // ⬅️ "긴소매" 대신 ""
                colors = resp.colors ?: emptyList(),
                material = resp.material,
                confidence = 0.9f
            )

            Log.d("AIAnalysis", "AI 분석 완료: $result")
            onAnalysisComplete(result)

        } catch (e: Exception) {
            Log.e("AIAnalysis", "분석 오류", e)
            Toast.makeText(context, "분석 중 오류: ${e.message}", Toast.LENGTH_LONG).show()
            // ❗️오류 발생 시에도 기본값으로라도 콜백을 호출할지 결정 필요
            // 예: onAnalysisComplete(AIAnalysisResult("상의", "긴소매", ...))
        }
    }

    // --- UI 그대로 유지 ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF283593),
                        Color(0xFF3949AB)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .rotate(rotation)
                    .background(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(60.dp)
                        .rotate(-rotation)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("AI가 옷을 분석하고 있어요",
                    fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("잠시만 기다려주세요...",
                    fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.alpha(alpha))
            }

            LinearProgressIndicator(
                modifier = Modifier.width(200.dp).height(4.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 24.dp)
            ) {
                AnalysisStep("옷 종류 분석 중", alpha)
                AnalysisStep("색상 인식 중", alpha)
                AnalysisStep("소재 분석 중", alpha)
            }
        }
    }
}

@Composable
fun AnalysisStep(text: String, alpha: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(8.dp)
                .background(color = Color.White.copy(alpha = alpha), shape = CircleShape)
        )
        Text(text, fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
    }
}

// AI 분석 결과 데이터 클래스
data class AIAnalysisResult(
    val type: String,           // "상의", "하의", "아우터", "원피스"
    val category: String,       // "반소매", "긴소매" 등
    val colors: List<String>,   // ["Blue", "White"]
    val material: String?,      // "면", "폴리" 등
    val confidence: Float
)

private fun uriToFile(context: android.content.Context, uri: Uri): File {
    val contentResolver = context.contentResolver
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output -> input.copyTo(output) }
    }
    return file
}
package com.example.wearther.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 앱의 다크 모드 색상 정의
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,        // 주요 색상
    secondary = PurpleGrey80,  // 보조 색상
    tertiary = Pink80          // 강조 색상
)

// 앱의 라이트 모드 색상 정의
private val LightColorScheme = lightColorScheme(
    primary = Purple40,        // 주요 색상
    secondary = PurpleGrey40,  // 보조 색상
    tertiary = Pink40          // 강조 색상

    /* 필요 시 다른 색상들도 오버라이딩 가능
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * Wearther 앱의 전체 테마를 구성하는 Composable 함수
 * - 시스템 다크모드 여부에 따라 테마를 적용
 * - 안드로이드 12 이상에서는 다이나믹 컬러도 지원
 */
@Composable
fun WeartherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // 시스템 설정에 따라 기본 다크 모드 여부 결정
    dynamicColor: Boolean = true,               // 동적 색상 적용 여부 (Android 12+)
    content: @Composable () -> Unit             // 테마가 적용될 Composable 콘텐츠
) {
    // 사용할 colorScheme을 조건에 따라 선택
    val colorScheme = when {
        // 동적 색상 허용 + 안드로이드 12 이상일 경우
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current // 현재 context 가져오기
            if (darkTheme) dynamicDarkColorScheme(context) // 다크 테마용 동적 색상
            else dynamicLightColorScheme(context)          // 라이트 테마용 동적 색상
        }

        // 일반 다크 테마
        darkTheme -> DarkColorScheme

        // 일반 라이트 테마
        else -> LightColorScheme
    }

    // 현재 화면의 뷰 참조
    val view = LocalView.current
    // Edit Mode가 아닐 경우 (디자인 프리뷰가 아닌 실제 화면일 때만)
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window // 현재 액티비티의 윈도우 가져오기
            window.statusBarColor = colorScheme.primary.toArgb() // 상태바 색상을 테마의 primary로 설정
            // 상태바 아이콘 색상 조정 (true면 어두운 아이콘 = 밝은 배경용)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    // 전체 Material3 테마 적용
    MaterialTheme(
        colorScheme = colorScheme,  // 위에서 결정된 색상 테마
        typography = Typography,    // 텍스트 스타일 정의
        content = content           // 이 테마가 적용될 하위 Composable들
    )
}

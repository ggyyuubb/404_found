package com.example.wearther.closet.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 옷장 상단 헤더 (닉네임 + 삭제 버튼)
 */
@Composable
fun ClosetTopBar(
    nickname: String = "닉네임",  // TODO: 실제 닉네임으로 교체
    isDeleteMode: Boolean,
    selectedCount: Int,
    onDeleteModeToggle: () -> Unit,
    onDeleteClick: () -> Unit,
    onCancelDeleteMode: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),  // ⭐️ 12dp → 8dp로 축소
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 닉네임의 옷장
            Text(
                text = "${nickname}의 옷장",
                fontSize = 18.sp,  // ⭐️ 20sp → 18sp로 축소
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 삭제 관련 버튼
            if (isDeleteMode) {
                DeleteModeActions(
                    selectedCount = selectedCount,
                    onDeleteClick = onDeleteClick,
                    onCancelClick = onCancelDeleteMode
                )
            } else {
                // ⭐️ 삭제 버튼 디자인 개선
                OutlinedButton(
                    onClick = onDeleteModeToggle,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("삭제", fontSize = 14.sp)
                }
            }
        }

        Divider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            thickness = 1.dp
        )
    }
}

/**
 * 삭제 모드일 때 표시되는 액션 버튼들
 */
@Composable
private fun DeleteModeActions(
    selectedCount: Int,
    onDeleteClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 선택된 개수 표시
        if (selectedCount > 0) {
            Text(
                text = "${selectedCount}개 선택",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // 삭제 실행 버튼
        if (selectedCount > 0) {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "선택 항목 삭제",
                    tint = Color.Red
                )
            }
        }

        // 취소 버튼
        IconButton(onClick = onCancelClick) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "취소"
            )
        }
    }
}

/**
 * 삭제 확인 다이얼로그
 */
@Composable
fun DeleteConfirmDialog(
    selectedCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "삭제 확인",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("선택한 ${selectedCount}개의 아이템을 삭제하시겠습니까?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("삭제", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
// closet/utils/ClosetSortUtils.kt
package com.example.wearther.closet.screen

import android.util.Log
import com.example.wearther.closet.data.ClosetImage
import com.example.wearther.ui.screens.closet.components.SortOption

object ClosetSortUtils {

    // Firebase 실제 카테고리명에 맞춘 우선순위 정의
    private val categoryOrder = mapOf(
        // 상의 카테고리
        "민소매" to 1, "반소매" to 2, "반팔티" to 2, "긴소매" to 3, "긴팔티" to 3,
        "후드" to 4, "후드티" to 4, "셔츠" to 5, "스웨터" to 6, "니트" to 6,
        // 하의 카테고리  
        "데님" to 101, "청바지" to 101, "트레이닝" to 102, "트레이닝팬츠" to 102,
        "슬랙스" to 103, "정장바지" to 103, "숏팬츠" to 104, "반바지" to 104, "스커트" to 105,
        // 아우터 카테고리
        "후드집업" to 201, "후드" to 201, "자켓" to 202, "블레이저" to 202, "코트" to 203,
        "무스탕&퍼" to 204, "가죽자켓" to 204, "플리스" to 205, "패딩" to 206, "다운자켓" to 206,
        // 원피스 카테고리
        "맥시" to 301, "맥시원피스" to 301, "나시원피스" to 302, "셔츠원피스" to 303, "원피스" to 304
    )

    /**
     * 주어진 정렬 옵션에 따라 옷 아이템들을 정렬합니다.
     *
     * @param items 정렬할 ClosetImage 리스트
     * @param sortOption 정렬 방식
     * @return 정렬된 ClosetImage 리스트
     */
    fun sortItems(items: List<ClosetImage>, sortOption: SortOption): List<ClosetImage> {
        return when (sortOption) {
            SortOption.CATEGORY -> sortByCategory(items)
            SortOption.NEWEST_FIRST -> sortByNewestFirst(items)
            SortOption.OLDEST_FIRST -> sortByOldestFirst(items)
            SortOption.MANUAL -> sortByManual(items)
        }
    }

    /**
     * 카테고리 순으로 정렬
     */
    private fun sortByCategory(items: List<ClosetImage>): List<ClosetImage> {
        return items.sortedWith(compareBy { item ->
            val category = item.category.trim()
            val priority = getCategoryPriority(category)

            Log.d("ClosetSort", "아이템 ${item.id}: category='$category' -> priority=$priority")
            priority
        })
    }

    /**
     * 최신순으로 정렬 (큰 ID = 최신)
     */
    private fun sortByNewestFirst(items: List<ClosetImage>): List<ClosetImage> {
        return try {
            items.sortedByDescending {
                it.id.toLongOrNull() ?: 0L
            }
        } catch (e: Exception) {
            Log.w("ClosetSort", "숫자 ID 정렬 실패, 문자열 정렬로 대체: ${e.message}")
            items.sortedByDescending { it.id }
        }
    }

    /**
     * 오래된순으로 정렬 (작은 ID = 오래된)
     */
    private fun sortByOldestFirst(items: List<ClosetImage>): List<ClosetImage> {
        return try {
            items.sortedBy {
                it.id.toLongOrNull() ?: 0L
            }
        } catch (e: Exception) {
            Log.w("ClosetSort", "숫자 ID 정렬 실패, 문자열 정렬로 대체: ${e.message}")
            items.sortedBy { it.id }
        }
    }

    /**
     * 직접정렬순 (추후 구현 예정)
     */
    private fun sortByManual(items: List<ClosetImage>): List<ClosetImage> {
        // TODO: manual_order 필드 추가되면 구현
        Log.d("ClosetSort", "직접정렬순 - 현재는 기본 순서 유지")
        return items
    }

    /**
     * 카테고리명에 해당하는 우선순위를 반환
     *
     * @param category 카테고리명
     * @return 우선순위 (낮을수록 앞에 배치, 없으면 9999)
     */
    private fun getCategoryPriority(category: String): Int {
        return categoryOrder[category] ?: 9999
    }

    /**
     * 새로운 카테고리를 우선순위 맵에 추가
     * (동적으로 카테고리를 추가해야 할 경우 사용)
     */
    fun addCategoryMapping(category: String, priority: Int) {
        Log.d("ClosetSort", "새 카테고리 매핑 추가: $category -> $priority")
        // 현재는 immutable map이므로 런타임에 추가 불가
        // 필요시 mutable map으로 변경 가능
    }

    /**
     * 현재 등록된 모든 카테고리와 우선순위를 반환
     */
    fun getAllCategoryMappings(): Map<String, Int> {
        return categoryOrder.toMap()
    }

    /**
     * 특정 카테고리의 우선순위를 확인
     */
    fun getCategoryPriorityPublic(category: String): Int {
        return getCategoryPriority(category)
    }
}
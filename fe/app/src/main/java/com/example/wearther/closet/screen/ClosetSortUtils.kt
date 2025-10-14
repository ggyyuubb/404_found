// closet/utils/ClosetSortUtils.kt
package com.example.wearther.closet.screen

import android.util.Log
import com.example.wearther.closet.data.ClosetImage
import com.example.wearther.ui.screens.closet.components.CategoryMapper
import com.example.wearther.ui.screens.closet.components.SortOption

object ClosetSortUtils {

    // AI 카테고리 (한국어)에 맞춘 우선순위 정의
    private val categoryOrder = mapOf(
        // 상의 카테고리
        "민소매" to 1,
        "반소매" to 2,
        "긴소매" to 3,
        "후드" to 4,
        "셔츠" to 5,
        "스웨터" to 6,
        "점퍼" to 7,

        // 하의 카테고리
        "면바지" to 101,
        "데님" to 102,
        "트레이닝팬츠" to 103,
        "슬랙스" to 104,
        "반바지" to 105,
        "스커트" to 106,

        // 아우터 카테고리
        "블레이저" to 201,
        "가디건" to 202,
        "코트" to 203,
        "롱패딩" to 204,
        "숏패딩" to 205,
        "후드집업" to 206,
        "플리스" to 207,

        // 원피스 카테고리
        "원피스" to 301
    )

    /**
     * 주어진 정렬 옵션에 따라 옷 아이템들을 정렬합니다.
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
     * 카테고리 순으로 정렬 (AI 영어 카테고리를 한국어로 변환 후 정렬)
     */
    private fun sortByCategory(items: List<ClosetImage>): List<ClosetImage> {
        return items.sortedWith(compareBy { item ->
            // AI가 반환한 영어 카테고리를 한국어로 변환
            val englishCategory = item.clothing_type?.trim() ?: ""
            val koreanCategory = CategoryMapper.toKorean(englishCategory)
            val priority = getCategoryPriority(koreanCategory)

            Log.d("ClosetSort", "아이템 ${item.id}: '$englishCategory' -> '$koreanCategory' -> priority=$priority")
            priority
        })
    }

    /**
     * 최신순으로 정렬 (uploaded_at 기준)
     */
    private fun sortByNewestFirst(items: List<ClosetImage>): List<ClosetImage> {
        return items.sortedByDescending {
            it.uploaded_at ?: ""
        }
    }

    /**
     * 오래된순으로 정렬 (uploaded_at 기준)
     */
    private fun sortByOldestFirst(items: List<ClosetImage>): List<ClosetImage> {
        return items.sortedBy {
            it.uploaded_at ?: ""
        }
    }

    /**
     * 직접정렬순 (추후 구현 예정)
     */
    private fun sortByManual(items: List<ClosetImage>): List<ClosetImage> {
        Log.d("ClosetSort", "직접정렬순 - 현재는 기본 순서 유지")
        return items
    }

    /**
     * 카테고리명에 해당하는 우선순위를 반환
     */
    private fun getCategoryPriority(category: String): Int {
        return categoryOrder[category] ?: 9999
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
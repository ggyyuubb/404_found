package com.example.wearther.closet.data

import android.util.Log
import com.example.wearther.ui.screens.closet.components.SortOption

object ClosetSortUtils {

    // ✅ 세부 카테고리 → 대분류 매핑
    private val mainCategoryMap = mapOf(
        // 상의 (6개)
        "민소매" to "상의",
        "반소매" to "상의",
        "긴소매" to "상의",
        "후드" to "상의",
        "셔츠" to "상의",
        "스웨터" to "상의",

        // 하의 (6개)
        "반바지" to "하의",
        "면바지" to "하의",
        "데님" to "하의",
        "트레이닝바지" to "하의",
        "슬랙스" to "하의",
        "스커트" to "하의",

        // 아우터 (9개)
        "점퍼" to "아우터",
        "블레이저" to "아우터",
        "가디건" to "아우터",
        "코트" to "아우터",
        "롱패딩" to "아우터",
        "숏패딩" to "아우터",
        "후드집업" to "아우터",
        "플리스" to "아우터",

        // 원피스 (1개)
        "원피스" to "원피스"
    )

    // ✅ 백엔드 영어 → 한국어 세부 카테고리 매핑 (총 22개)
    private val subCategoryMap = mapOf(
        // 상의 (6개)
        "sleeveless" to "민소매",
        "shortsleeve" to "반소매",
        "longsleeve" to "긴소매",
        "hood" to "후드",
        "shirt" to "셔츠",
        "sweater" to "스웨터",

        // 하의 (6개)
        "shorts" to "반바지",
        "cotton pants" to "면바지",
        "cottonpants" to "면바지",  // 공백 없는 버전
        "denim" to "데님",
        "trainingpants" to "트레이닝바지",
        "slacks" to "슬랙스",
        "skirt" to "스커트",

        // 아우터 (9개)
        "jumper" to "점퍼",
        "blazer" to "블레이저",
        "cardigan" to "가디건",
        "coat" to "코트",
        "longpedding" to "롱패딩",
        "shortpedding" to "숏패딩",
        "hoodzip" to "후드집업",
        "fleece" to "플리스",

        // 원피스 (1개)
        "dress" to "원피스"
    )

    // ✅ 색상 매핑 (소문자 + 대문자 모두 지원)
    private val colorMap = mapOf(
        // 소문자 (하위호환성)
        "beige" to "베이지",
        "black" to "블랙",
        "blue" to "블루",
        "brown" to "브라운",
        "gray" to "그레이",
        "grey" to "그레이",
        "green" to "그린",
        "orange" to "오렌지",
        "pink" to "핑크",
        "purple" to "퍼플",
        "red" to "레드",
        "white" to "화이트",
        "yellow" to "옐로우",
        "navy" to "네이비",
        "khaki" to "카키",
        "ivory" to "아이보리",

        // ✅ 대문자 (서버 데이터 표준)
        "Beige" to "베이지",
        "Black" to "블랙",
        "Blue" to "블루",
        "Brown" to "브라운",
        "Gray" to "그레이",
        "Grey" to "그레이",
        "Green" to "그린",
        "Orange" to "오렌지",
        "Pink" to "핑크",
        "Purple" to "퍼플",
        "Red" to "레드",
        "White" to "화이트",
        "Yellow" to "옐로우",
        "Navy" to "네이비",
        "Khaki" to "카키",
        "Ivory" to "아이보리"
    )

    // ✅ 대분류 우선순위
    private val mainCategoryOrder = mapOf(
        "상의" to 1,
        "하의" to 2,
        "아우터" to 3,
        "원피스" to 4,
        "기타" to 9
    )

    // ✅ 세부 카테고리 우선순위 (앱에서 보이는 순서대로)
    private val subCategoryOrder = mapOf(
        // 상의 (6개)
        "민소매" to 1,
        "반소매" to 2,
        "긴소매" to 3,
        "후드" to 4,
        "셔츠" to 5,
        "스웨터" to 6,

        // 하의 (6개)
        "반바지" to 1,
        "면바지" to 2,
        "데님" to 3,
        "트레이닝바지" to 4,
        "슬랙스" to 5,
        "스커트" to 6,

        // 아우터 (9개)
        "점퍼" to 1,
        "블레이저" to 2,
        "가디건" to 3,
        "코트" to 4,
        "롱패딩" to 5,
        "숏패딩" to 6,
        "후드집업" to 7,
        "플리스" to 8,

        // 원피스 (1개)
        "원피스" to 1
    )

    // ========== CategoryMapper 기능 ==========

    /**
     * 영어 카테고리 → 한국어 세부 카테고리 변환
     */
    fun toKorean(english: String?): String {
        if (english.isNullOrBlank()) return "미분류"

        val normalized = english.trim().lowercase()

        // 1차: 원본 그대로 매칭
        subCategoryMap[normalized]?.let { return it }

        // 2차: 공백 제거 후 매칭
        val noSpace = normalized.replace(" ", "")
        subCategoryMap[noSpace]?.let { return it }

        // 3차: 하이픈 제거 후 매칭
        val noHyphen = normalized.replace("-", "")
        subCategoryMap[noHyphen]?.let { return it }

        // 매칭 실패 시
        return "미분류"
    }

    /**
     * 세부 카테고리 → 대분류 변환
     */
    fun getMainCategory(subCategory: String): String {
        return mainCategoryMap[subCategory] ?: "기타"
    }

    /**
     * 영어 카테고리 → 대분류 변환 (한 번에)
     */
    fun toMainCategory(english: String?): String {
        val subCategory = toKorean(english)
        return getMainCategory(subCategory)
    }

    /**
     * 한국어 세부 카테고리 → 영어 변환
     */
    fun toEnglish(korean: String?): String? {
        if (korean.isNullOrBlank()) return null
        return subCategoryMap.entries.find { it.value == korean }?.key
    }

    /**
     * 영어 색상 → 한국어 변환
     * ✅ 대소문자 모두 처리 (Blue, blue, BLUE 등)
     */
    fun colorToKorean(englishColor: String?): String {
        if (englishColor.isNullOrBlank()) return "정보 없음"

        val trimmed = englishColor.trim()

        // ✅ 1차: 원본 그대로 (Blue 처리)
        colorMap[trimmed]?.let { return it }

        // ✅ 2차: 소문자 변환 (blue 처리)
        val normalized = trimmed.lowercase()
        return colorMap[normalized] ?: englishColor
    }

    /**
     * 영어 색상 리스트 → 한국어 문자열
     */
    fun colorsToKorean(englishColors: List<String>?): String {
        if (englishColors.isNullOrEmpty()) return "정보 없음"
        return englishColors.joinToString(", ") { colorToKorean(it) }
    }

    /**
     * 대분류별 세부 카테고리 리스트 반환
     */
    fun getSubCategories(mainCategory: String): List<String> {
        return mainCategoryMap.entries
            .filter { it.value == mainCategory }
            .map { it.key }
    }

    /**
     * 전체 매핑 개수 확인
     */
    fun getTotalCategoryCount(): Int {
        return subCategoryMap.values.toSet().size
    }

    // ========== 정렬 기능 ==========

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
     * 카테고리 순으로 정렬
     * 1. 대분류(상의/하의/아우터/원피스) 우선순위
     * 2. 세부 카테고리 우선순위
     */
    private fun sortByCategory(items: List<ClosetImage>): List<ClosetImage> {
        return items.sortedWith(compareBy(
            // 1차: 대분류 정렬
            { item ->
                val englishCategory = item.category?.trim() ?: ""
                val koreanSubCategory = toKorean(englishCategory)
                val mainCategory = getMainCategory(koreanSubCategory)
                val mainPriority = mainCategoryOrder[mainCategory] ?: 999

                Log.d("ClosetSort", "아이템 ${item.id}: '$englishCategory' -> '$koreanSubCategory' -> '$mainCategory' (우선순위: $mainPriority)")
                mainPriority
            },
            // 2차: 세부 카테고리 정렬
            { item ->
                val englishCategory = item.category?.trim() ?: ""
                val koreanSubCategory = toKorean(englishCategory)
                val subPriority = subCategoryOrder[koreanSubCategory] ?: 999

                Log.d("ClosetSort", "  └ 세부 우선순위: $subPriority")
                subPriority
            }
        ))
    }

    /**
     * 최신순으로 정렬
     */
    private fun sortByNewestFirst(items: List<ClosetImage>): List<ClosetImage> {
        return items.sortedByDescending {
            it.uploaded_at ?: ""
        }
    }

    /**
     * 오래된순으로 정렬
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
     * 디버깅용: 모든 카테고리 매핑 정보 출력
     */
    fun printCategoryMappings() {
        Log.d("ClosetSort", "=== 대분류 우선순위 ===")
        mainCategoryOrder.forEach { (cat, priority) ->
            Log.d("ClosetSort", "$cat: $priority")
        }

        Log.d("ClosetSort", "\n=== 세부 카테고리 우선순위 ===")
        subCategoryOrder.forEach { (cat, priority) ->
            val mainCat = getMainCategory(cat)
            Log.d("ClosetSort", "$cat ($mainCat): $priority")
        }

        Log.d("ClosetSort", "\n총 세부 카테고리 개수: ${getTotalCategoryCount()}개")
    }
}
package com.example.wearther.closet.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearther.closet.data.ClosetApi
import com.example.wearther.closet.data.ClosetImage
import com.example.wearther.closet.data.ClosetSortUtils
import com.example.wearther.ui.screens.closet.components.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClosetViewModel(
    private val closetApi: ClosetApi,
    private val tokenHeader: String
) : ViewModel() {

    // ========== State 관리 ==========
    private val _allItems = MutableStateFlow<List<ClosetImage>>(emptyList())

    private val _filteredItems = MutableStateFlow<List<ClosetImage>>(emptyList())
    val filteredItems: StateFlow<List<ClosetImage>> = _filteredItems.asStateFlow()

    private val _activeCategory = MutableStateFlow("전체")
    val activeCategory: StateFlow<String> = _activeCategory.asStateFlow()

    private val _activeSubCategory = MutableStateFlow("전체")
    val activeSubCategory: StateFlow<String> = _activeSubCategory.asStateFlow()

    private val _currentSortOption = MutableStateFlow(SortOption.CATEGORY)
    val currentSortOption: StateFlow<SortOption> = _currentSortOption.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ========== 초기화 ==========
    init {
        fetchClosetImages()
    }

    // ========== 데이터 로드 ==========
    fun fetchClosetImages() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                Log.d("ClosetViewModel", "========== API 호출 시작 ==========")
                Log.d("ClosetViewModel", "토큰 헤더: $tokenHeader")

                val response = closetApi.getMyImages(token = tokenHeader)

                Log.d("ClosetViewModel", "받아온 이미지 개수: ${response.images.size}")

                // ✅ 전체 응답 JSON 출력
                Log.d("ClosetViewModel", "========== 전체 응답 데이터 ==========")
                response.images.forEachIndexed { index, item ->
                    Log.d("ClosetViewModel", "\n[$index] 아이템 전체 정보:")
                    Log.d("ClosetViewModel", "  - id: '${item.id}'")
                    Log.d("ClosetViewModel", "  - filename: '${item.filename}'")
                    Log.d("ClosetViewModel", "  - url: '${item.url}'")
                    Log.d("ClosetViewModel", "  - uploaded_at: '${item.uploaded_at}'")
                    Log.d("ClosetViewModel", "  - type(원본): '${item.type}'")
                    Log.d("ClosetViewModel", "  - category(원본): '${item.category}'")
                    Log.d("ClosetViewModel", "  - colors(원본): ${item.colors}")
                    Log.d("ClosetViewModel", "  - material(원본): '${item.material}'")
                    Log.d("ClosetViewModel", "  - suitable_temperature(원본): '${item.suitable_temperature}'")

                    // ✅ 변환 후
                    val koreanType = item.type ?: "null"
                    val koreanCategory = ClosetSortUtils.toKorean(item.category)
                    val koreanColors = ClosetSortUtils.colorsToKorean(item.colors)
                    val mainCategory = ClosetSortUtils.getMainCategory(koreanCategory)

                    Log.d("ClosetViewModel", "  → type(한글): '$koreanType'")
                    Log.d("ClosetViewModel", "  → category(원본 영어): '${item.category}'")
                    Log.d("ClosetViewModel", "  → category(한글 세부): '$koreanCategory'")
                    Log.d("ClosetViewModel", "  → category(한글 대분류): '$mainCategory'")
                    Log.d("ClosetViewModel", "  → colors(한글): '$koreanColors'")
                }
                Log.d("ClosetViewModel", "========== 응답 데이터 끝 ==========\n")

                _allItems.value = response.images
                applyFilters()

            } catch (e: Exception) {
                Log.e("ClosetViewModel", "옷장 로드 실패", e)
                Log.e("ClosetViewModel", "에러 상세: ${e.message}")
                e.printStackTrace()
                _error.value = "옷장을 불러오는 데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== 카테고리 변경 ==========
    fun setCategory(category: String) {
        Log.d("ClosetViewModel", "대분류 변경: $category")
        _activeCategory.value = category
        _activeSubCategory.value = "전체"  // 서브카테고리 초기화
        applyFilters()
    }

    fun setSubCategory(subCategory: String) {
        Log.d("ClosetViewModel", "서브카테고리 변경: $subCategory")
        _activeSubCategory.value = subCategory
        applyFilters()
    }

    // ========== 정렬 변경 ==========
    fun setSortOption(sortOption: SortOption) {
        Log.d("ClosetViewModel", "정렬 옵션 변경: $sortOption")
        _currentSortOption.value = sortOption
        applyFilters()
    }

    // ========== 필터링 로직 (핵심!) ==========
    private fun applyFilters() {
        val category = _activeCategory.value
        val subCategory = _activeSubCategory.value

        Log.d("ClosetViewModel", "========== 필터링 시작 ==========")
        Log.d("ClosetViewModel", "대분류: $category, 서브: $subCategory")
        Log.d("ClosetViewModel", "전체 아이템: ${_allItems.value.size}개")

        // ✅ 1단계: 대분류 필터링
        val categoryFiltered = if (category == "전체") {
            _allItems.value
        } else {
            _allItems.value.filter { item ->
                // category(영어) → 한글 세부 카테고리 → 대분류로 변환하여 비교
                val koreanSubCategory = ClosetSortUtils.toKorean(item.category)
                val mainCategory = ClosetSortUtils.getMainCategory(koreanSubCategory)

                Log.d("ClosetViewModel", "아이템 ${item.id}: category='${item.category}' → 세부='$koreanSubCategory' → 대분류='$mainCategory'")

                mainCategory == category
            }
        }

        Log.d("ClosetViewModel", "대분류 필터 후: ${categoryFiltered.size}개")

        // ✅ 2단계: 서브카테고리 필터링
        val subCategoryFiltered = if (subCategory == "전체") {
            categoryFiltered
        } else {
            categoryFiltered.filter { item ->
                val koreanCategory = ClosetSortUtils.toKorean(item.category)
                Log.d("ClosetViewModel", "아이템 ${item.id}: category='${item.category}' → 한글='$koreanCategory', 비교='$subCategory'")
                koreanCategory == subCategory
            }
        }

        Log.d("ClosetViewModel", "서브카테고리 필터 후: ${subCategoryFiltered.size}개")

        // ✅ 3단계: 정렬 적용
        val sorted = ClosetSortUtils.sortItems(subCategoryFiltered, _currentSortOption.value)

        _filteredItems.value = sorted

        Log.d("ClosetViewModel", "최종 결과: ${sorted.size}개")
        Log.d("ClosetViewModel", "========== 필터링 완료 ==========")
    }

    // ========== 수정 ==========
    fun updateImage(
        imageId: String,
        category: String,           // 영문 세부 카테고리
        colors: List<String>,       // 영문 색상 배열 (첫글자 대문자)
        material: String,
        temperature: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                Log.d("ClosetViewModel", "========== 이미지 수정 시작 ==========")
                Log.d("ClosetViewModel", "imageId: $imageId")
                Log.d("ClosetViewModel", "category(영문): $category")
                Log.d("ClosetViewModel", "colors: $colors")
                Log.d("ClosetViewModel", "material: $material")
                Log.d("ClosetViewModel", "temperature: $temperature")

                // ✅ 영문 세부 카테고리 → 한글 대분류 계산
                val subKo = ClosetSortUtils.toKorean(category)
                val typeKo = ClosetSortUtils.getMainCategory(subKo)

                Log.d("ClosetViewModel", "→ 계산된 type(한글 대분류): $typeKo")

                val updateData = UpdateImageRequest(
                    type = typeKo,              // ✅ 한글 대분류: "상의"
                    category = category,        // 영문 세부: "longsleeve"
                    colors = colors,            // ["Blue", "Gray"]
                    material = material.takeIf { it.isNotBlank() },
                    suitable_temperature = temperature.takeIf { it.isNotBlank() }
                )

                closetApi.updateImage(tokenHeader, imageId, updateData)

                Log.d("ClosetViewModel", "이미지 수정 성공!")

                // ✅ 성공 시 목록 새로고침
                fetchClosetImages()
                onSuccess()

            } catch (e: Exception) {
                Log.e("ClosetViewModel", "이미지 수정 실패", e)
                Log.e("ClosetViewModel", "에러 상세: ${e.message}")
                e.printStackTrace()
                onError("수정에 실패했습니다: ${e.message}")
            }
        }
    }

    // ========== 삭제 ==========
    fun deleteImage(imageId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                Log.d("ClosetViewModel", "이미지 삭제 시작: $imageId")
                closetApi.deleteImage(tokenHeader, imageId)

                // ✅ 성공 시 목록 새로고침
                fetchClosetImages()
                onSuccess()

            } catch (e: Exception) {
                Log.e("ClosetViewModel", "이미지 삭제 실패", e)
                onError("삭제에 실패했습니다: ${e.message}")
            }
        }
    }

    // ========== 에러 클리어 ==========
    fun clearError() {
        _error.value = null
    }
}
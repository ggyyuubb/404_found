package com.example.wearther.closet.data

// -------------------------------------
// 공통 베이스: type=대분류 / category=세부분류
// -------------------------------------
interface ClothingBase {
    val id: String
    val type: String?                 // 상의 / 하의 / 아우터 / 원피스
    val category: String?             // 예: 티셔츠, 셔츠, 니트, 데님, 슬랙스, 코트 ...
    val colors: List<String>?
    val material: String?             // 예: 면, 폴리에스터, 울, 데님 ... (소재로 '니트' 사용 금지)
    val suitable_temperature: String?
}

// -------------------------------------
// 백엔드 DTO (Firestore/서버 스키마와 일치)
// -------------------------------------
data class ClosetImage(
    override val id: String,
    val filename: String? = "",
    val url: String,
    val uploaded_at: String? = "",
    override val type: String? = "",
    override val category: String? = "",
    override val colors: List<String>? = emptyList(),
    override val material: String? = "",
    override val suitable_temperature: String? = ""
) : ClothingBase

// -------------------------------------
// UI 아이템 (화면 표시에 최적화)
// -------------------------------------
data class FashionItem(
    override val id: String,
    override val type: String? = "",
    override val category: String? = "",
    val image: String,
    val isLiked: Boolean = false,
    override val colors: List<String>? = emptyList(),
    override val material: String? = "",
    override val suitable_temperature: String? = ""
) : ClothingBase

// -------------------------------------
// 매퍼 (백엔드 DTO ↔ UI 변환)
// -------------------------------------
fun ClosetImage.toFashionItem(): FashionItem = FashionItem(
    id = id,
    type = type ?: "",
    category = category ?: "",
    image = url,
    isLiked = false,
    colors = colors ?: emptyList(),
    material = material ?: "",
    suitable_temperature = suitable_temperature ?: ""
)

fun FashionItem.toClosetImage(): ClosetImage = ClosetImage(
    id = id,
    filename = null,
    url = image,
    uploaded_at = null,
    type = type ?: "",
    category = category ?: "",
    colors = colors ?: emptyList(),
    material = material ?: "",
    suitable_temperature = suitable_temperature ?: ""
)

// -------------------------------------
// 오타 방지 상수
// -------------------------------------
object ClothingTypes {
    const val TOP = "상의"
    const val BOTTOM = "하의"
    const val OUTER = "아우터"
    const val DRESS = "원피스"
}

object TopCategories {
    val ALL = listOf("티셔츠", "맨투맨", "후드", "셔츠", "블라우스", "니트", "스웨터", "긴소매", "반소매", "민소매")
}

object BottomCategories {
    val ALL = listOf("데님", "트레이닝", "코튼", "슬랙스", "레깅스", "숏팬츠", "스커트")
}

object OuterCategories {
    val ALL = listOf("후드집업", "자켓", "코트", "패딩", "플리스")
}

object DressCategories {
    val ALL = listOf("숏원피스", "미디움원피스", "롱원피스", "점프슈트")
}

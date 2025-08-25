package com.example.wearther.home.recommendation

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class RecommendationResponse(
    // 서버가 recommended를 아예 빼거나 null/문자열로 줄 수도 있으므로 안전하게 Nullable + 기본값 권장
    @SerializedName("recommended") val recommended: JsonElement? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("temp") val temp: Int = 0,
    @SerializedName("weather_code") val weather_code: Int = 0
) {
    companion object {
        private val gson = Gson()
        private const val TAG = "RecoParse"
    }

    /** element가 객체면 그대로, 문자열로 JSON이면 파싱해서 객체로, 아니면 null */
    private fun asJsonObjectOrNull(element: JsonElement?): JsonObject? {
        if (element == null || element.isJsonNull) return null
        return when {
            element.isJsonObject -> element.asJsonObject
            // 서버가 문자열로 내려보내는 케이스 대응: "{...}" 또는 "" 등
            element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
                val raw = element.asString
                runCatching { gson.fromJson(raw, JsonObject::class.java) }
                    .onFailure { Log.e(TAG, "string->json parse fail: $raw / ${it.message}") }
                    .getOrNull()
            }
            else -> null
        }
    }

    /** recommended 루트에서 key 가져오기 (객체/문자열 모두 대응) */
    private fun getRecommendedObj(key: String): JsonObject? {
        val rootObj = asJsonObjectOrNull(recommended)
        val child = rootObj?.get(key)
        if (child == null || child.isJsonNull) return null

        val obj = asJsonObjectOrNull(child)
        if (obj == null) {
            // 문자열/HTML 등 객체가 아닌 경우 로깅
            Log.e(TAG, "$key not object. raw=$child")
        }
        return obj
    }

    private fun parseRecommended(obj: JsonObject?): RecommendedClothing? {
        if (obj == null) return null
        return runCatching { gson.fromJson(obj, RecommendedClothing::class.java) }
            .onFailure { Log.e(TAG, "RecommendedClothing parse fail: ${it.message}") }
            .getOrNull()
    }

    fun getTop(): RecommendedClothing?    = parseRecommended(getRecommendedObj("top"))
    fun getBottom(): RecommendedClothing? = parseRecommended(getRecommendedObj("bottom"))
    fun getOuter(): RecommendedClothing?  = parseRecommended(getRecommendedObj("outer"))
}

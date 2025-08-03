package com.example.wearther.home.recommendation

import com.google.gson.Gson
import com.google.gson.JsonElement

data class RecommendationResponse(
    val recommended: JsonElement?,
    val message: String?,
    val temp: Int,
    val weather_code: Int
) {
    companion object {
        private val gson = Gson()
    }

    fun getTop(): RecommendedClothing? {
        if (recommended == null || recommended.isJsonNull || !recommended.isJsonObject) return null
        val jsonObject = recommended.asJsonObject
        val element = if (jsonObject.has("top")) jsonObject.get("top") else null
        if (element == null || element.isJsonNull) return null
        return gson.fromJson(element, RecommendedClothing::class.java)
    }

    fun getBottom(): RecommendedClothing? {
        if (recommended == null || recommended.isJsonNull || !recommended.isJsonObject) return null
        val jsonObject = recommended.asJsonObject
        val element = if (jsonObject.has("bottom")) jsonObject.get("bottom") else null
        if (element == null || element.isJsonNull) return null
        return gson.fromJson(element, RecommendedClothing::class.java)
    }

    fun getOuter(): RecommendedClothing? {
        if (recommended == null || recommended.isJsonNull || !recommended.isJsonObject) return null
        val jsonObject = recommended.asJsonObject
        val element = if (jsonObject.has("outer")) jsonObject.get("outer") else null
        if (element == null || element.isJsonNull) return null
        return gson.fromJson(element, RecommendedClothing::class.java)
    }
}
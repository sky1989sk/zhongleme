package com.lottery.app.infra.local

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.lottery.app.domain.model.*

object ResultJsonConverter {

    private val gson = Gson()

    fun toJson(result: GenerateResult): String {
        val wrapper = JsonObject()
        when (result) {
            is GenerateResult.StandardResult -> {
                wrapper.addProperty("type", "standard")
                wrapper.add("numbers", gson.toJsonTree(result.numbers))
            }
            is GenerateResult.MultipleResult -> {
                wrapper.addProperty("type", "multiple")
                wrapper.add("numbers", gson.toJsonTree(result.numbers))
            }
            is GenerateResult.DanTuoResult -> {
                wrapper.addProperty("type", "dantuo")
                wrapper.add("danTuo", gson.toJsonTree(result.danTuo))
            }
        }
        return gson.toJson(wrapper)
    }

    fun fromJson(json: String): GenerateResult {
        val obj = JsonParser.parseString(json).asJsonObject
        return when (obj.get("type").asString) {
            "standard" -> {
                val listType = object : TypeToken<List<LotteryNumber>>() {}.type
                val numbers: List<LotteryNumber> = gson.fromJson(obj.get("numbers"), listType)
                GenerateResult.StandardResult(numbers)
            }
            "multiple" -> {
                val numbers: LotteryNumber = gson.fromJson(obj.get("numbers"), LotteryNumber::class.java)
                GenerateResult.MultipleResult(numbers)
            }
            "dantuo" -> {
                val danTuo: DanTuoNumber = gson.fromJson(obj.get("danTuo"), DanTuoNumber::class.java)
                GenerateResult.DanTuoResult(danTuo)
            }
            else -> throw IllegalArgumentException("Unknown result type in JSON: $json")
        }
    }
}

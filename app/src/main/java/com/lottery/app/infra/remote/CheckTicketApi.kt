package com.lottery.app.infra.remote

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lottery.app.domain.model.LotteryType
import com.lottery.app.domain.model.LotteryNumber
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class CheckTicketApi(
    private val gson: Gson = Gson(),
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun buildRequestBody(
        lotteryType: LotteryType,
        period: String,
        tickets: List<LotteryNumber>
    ): String {
        return when (lotteryType) {
            LotteryType.SSQ -> SsqCheckRequest(
                period = period,
                ssqTickets = tickets.map { num ->
                    SsqTicket(
                        redBalls = num.frontNumbers,
                        blueBall = num.backNumbers.firstOrNull() ?: 0,
                        multiple = 1
                    )
                }
            ).let { gson.toJson(it) }
            LotteryType.DLT -> DltCheckRequest(
                period = period,
                dltTickets = tickets.map { num ->
                    DltTicket(
                        frontBalls = num.frontNumbers,
                        backBalls = num.backNumbers,
                        multiple = 1
                    )
                }
            ).let { gson.toJson(it) }
        }
    }

    fun checkTickets(
        baseUrl: String?,
        lotteryType: LotteryType,
        period: String,
        tickets: List<LotteryNumber>
    ): CheckTicketResult {
        val effectiveBase = baseUrl?.takeIf { it.isNotBlank() } ?: return CheckTicketResult.Error("未配置查询服务器地址")
        val url = effectiveBase.trimEnd('/') + "/api/check-ticket"

        val requestBody = buildRequestBody(lotteryType, period, tickets)

        val request = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody(jsonMediaType))
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val body = try {
                    response.body?.string()
                } catch (e: Exception) {
                    return CheckTicketResult.Error("响应体读取异常: ${e.message ?: "unknown"}")
                }
                if (body.isNullOrBlank()) {
                    return CheckTicketResult.Error("服务器响应为空")
                }
                if (!response.isSuccessful) {
                    val detail = runCatching {
                        gson.fromJson(body, ErrorResponse::class.java)?.detail
                    }.getOrNull()
                    return CheckTicketResult.Error(detail ?: "查询失败 (HTTP ${response.code})")
                }
                val resp = gson.fromJson(body, CheckTicketResponse::class.java)
                val results = resp?.results?.map { r ->
                    CheckTicketResult.Item(
                        index = r.index,
                        isWin = r.isWin,
                        level = r.level,
                        amount = r.amount,
                        hitRed = r.hitRed,
                        hitBlue = r.hitBlue,
                        remark = r.remark
                    )
                } ?: emptyList()
                CheckTicketResult.Success(results)
            }
        } catch (e: Exception) {
            CheckTicketResult.Error(e.message ?: "网络请求失败")
        }
    }

    // --- Request bodies ---

    private data class SsqCheckRequest(
        @SerializedName("lottery_type") val lotteryType: String = "ssq",
        @SerializedName("period") val period: String,
        @SerializedName("ssq_tickets") val ssqTickets: List<SsqTicket>
    )

    private data class SsqTicket(
        @SerializedName("red_balls") val redBalls: List<Int>,
        @SerializedName("blue_ball") val blueBall: Int,
        @SerializedName("multiple") val multiple: Int
    )

    private data class DltCheckRequest(
        @SerializedName("lottery_type") val lotteryType: String = "dlt",
        @SerializedName("period") val period: String,
        @SerializedName("dlt_tickets") val dltTickets: List<DltTicket>
    )

    private data class DltTicket(
        @SerializedName("front_balls") val frontBalls: List<Int>,
        @SerializedName("back_balls") val backBalls: List<Int>,
        @SerializedName("multiple") val multiple: Int
    )

    // --- Response bodies ---

    private data class CheckTicketResponse(
        @SerializedName("lottery_type") val lotteryType: String?,
        @SerializedName("period") val period: String?,
        @SerializedName("results") val results: List<TicketResultDto>?
    )

    private data class TicketResultDto(
        @SerializedName("index") val index: Int,
        @SerializedName("is_win") val isWin: Boolean,
        @SerializedName("level") val level: String?,
        @SerializedName("amount") val amount: Long?,
        @SerializedName("hit_red") val hitRed: Int,
        @SerializedName("hit_blue") val hitBlue: Int,
        @SerializedName("remark") val remark: String?
    )

    private data class ErrorResponse(
        @SerializedName("detail") val detail: String?
    )

    sealed class CheckTicketResult {
        data class Success(val items: List<Item>) : CheckTicketResult()
        data class Error(val message: String) : CheckTicketResult()

        data class Item(
            val index: Int,
            val isWin: Boolean,
            val level: String?,
            val amount: Long?,
            val hitRed: Int,
            val hitBlue: Int,
            val remark: String?
        )
    }
}

package com.genxsolutions.growwealth.data.remote

import com.google.gson.annotations.SerializedName

data class SnapshotLatestResponse(
    @SerializedName("has_data") val hasData: Boolean,
    @SerializedName("latest_date") val latestDate: String?,
    @SerializedName("age_days") val ageDays: Int?,
    @SerializedName("freshness_label") val freshnessLabel: String?
)

data class HomeSummaryResponse(
    @SerializedName("snapshot_date") val snapshotDate: String?,
    @SerializedName("age_days") val ageDays: Int?,
    @SerializedName("freshness_label") val freshnessLabel: String?,
    @SerializedName("market_mood") val marketMood: MarketMood
)

data class MarketMood(
    @SerializedName("up_count") val upCount: Int,
    @SerializedName("down_count") val downCount: Int,
    @SerializedName("neutral_count") val neutralCount: Int,
    @SerializedName("total_sectors") val totalSectors: Int,
    @SerializedName("avg_confidence") val avgConfidence: Double,
    @SerializedName("avg_sentiment_score") val avgSentimentScore: Double
)

data class SectorSignalsResponse(
    @SerializedName("has_data") val hasData: Boolean,
    @SerializedName("snapshot_date") val snapshotDate: String?,
    @SerializedName("age_days") val ageDays: Int?,
    @SerializedName("freshness_label") val freshnessLabel: String?,
    @SerializedName("pagination") val pagination: Pagination,
    @SerializedName("items") val items: List<SectorSignal>
)

data class Pagination(
    @SerializedName("limit") val limit: Int,
    @SerializedName("offset") val offset: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("count") val count: Int
)

data class SectorSignal(
    @SerializedName("sector_id") val sectorId: Int,
    @SerializedName("sector_name") val sectorName: String,
    @SerializedName("signal") val signal: String,
    @SerializedName("confidence") val confidence: Double,
    @SerializedName("sentiment_score") val sentimentScore: Double,
    @SerializedName("article_count") val articleCount: Int,
    @SerializedName("snapshot_date") val snapshotDate: String
)

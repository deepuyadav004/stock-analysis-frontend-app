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

data class SectorTrendsResponse(
    @SerializedName("has_data") val hasData: Boolean,
    @SerializedName("snapshot_date") val snapshotDate: String?,
    @SerializedName("days") val days: Int,
    @SerializedName("items") val items: List<SectorTrend>
)

data class SectorTrend(
    @SerializedName("sector_id") val sectorId: Int,
    @SerializedName("sector_name") val sectorName: String,
    @SerializedName("points") val points: List<SectorTrendPoint>
)

data class SectorTrendPoint(
    @SerializedName("date") val date: String,
    @SerializedName("signal") val signal: String,
    @SerializedName("confidence") val confidence: Double,
    @SerializedName("sentiment_score") val sentimentScore: Double
)

data class SectorDetailResponse(
    @SerializedName("has_data") val hasData: Boolean,
    @SerializedName("sector") val sector: SectorIdentity?,
    @SerializedName("range") val range: SectorDetailRange?,
    @SerializedName("timeline") val timeline: List<SectorDetailPoint>
)

data class SectorIdentity(
    @SerializedName("sector_id") val sectorId: Int,
    @SerializedName("sector_name") val sectorName: String
)

data class SectorDetailRange(
    @SerializedName("from") val from: String,
    @SerializedName("to") val to: String
)

data class SectorDetailPoint(
    @SerializedName("date") val date: String,
    @SerializedName("signal") val signal: String,
    @SerializedName("confidence") val confidence: Double,
    @SerializedName("sentiment_score") val sentimentScore: Double,
    @SerializedName("article_count") val articleCount: Int
)

data class InsightsCompareResponse(
    @SerializedName("has_data") val hasData: Boolean,
    @SerializedName("snapshot_date") val snapshotDate: String?,
    @SerializedName("days") val days: Int,
    @SerializedName("market") val market: InsightsMarket?,
    @SerializedName("leaders") val leaders: InsightsLeaders?
)

data class InsightsMarket(
    @SerializedName("up_count") val upCount: Int,
    @SerializedName("down_count") val downCount: Int,
    @SerializedName("neutral_count") val neutralCount: Int,
    @SerializedName("total_sectors") val totalSectors: Int,
    @SerializedName("avg_confidence") val avgConfidence: Double,
    @SerializedName("avg_sentiment") val avgSentiment: Double,
    @SerializedName("sentiment_change_vs_previous") val sentimentChangeVsPrevious: Double
)

data class InsightsLeaders(
    @SerializedName("strongest") val strongest: InsightsLeaderItem?,
    @SerializedName("weakest") val weakest: InsightsLeaderItem?
)

data class InsightsLeaderItem(
    @SerializedName("sector_id") val sectorId: Int,
    @SerializedName("sector_name") val sectorName: String,
    @SerializedName("avg_sentiment") val avgSentiment: Double,
    @SerializedName("avg_confidence") val avgConfidence: Double
)

data class InsightsStabilityResponse(
    @SerializedName("has_data") val hasData: Boolean,
    @SerializedName("snapshot_date") val snapshotDate: String?,
    @SerializedName("days") val days: Int,
    @SerializedName("summary") val summary: InsightsStabilitySummary?,
    @SerializedName("items") val items: List<InsightsStabilityItem>
)

data class InsightsStabilitySummary(
    @SerializedName("stable_sector_count") val stableSectorCount: Int,
    @SerializedName("total_sectors") val totalSectors: Int,
    @SerializedName("avg_stability_ratio") val avgStabilityRatio: Double,
    @SerializedName("avg_confidence") val avgConfidence: Double
)

data class InsightsStabilityItem(
    @SerializedName("sector_id") val sectorId: Int,
    @SerializedName("sector_name") val sectorName: String,
    @SerializedName("latest_signal") val latestSignal: String,
    @SerializedName("total_days") val totalDays: Int,
    @SerializedName("flips") val flips: Int,
    @SerializedName("stability_ratio") val stabilityRatio: Double,
    @SerializedName("avg_confidence") val avgConfidence: Double
)

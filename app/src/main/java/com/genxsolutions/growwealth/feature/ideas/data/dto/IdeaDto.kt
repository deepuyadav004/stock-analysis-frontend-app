package com.genxsolutions.growwealth.feature.ideas.data.dto

import com.google.gson.annotations.SerializedName

data class IdeaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("ticker") val ticker: String,
    @SerializedName("company_name") val companyName: String,
    @SerializedName("call_type") val callType: String?,
    @SerializedName("target_price") val targetPrice: Double?,
    @SerializedName("source") val source: String,
    @SerializedName("brief_rationale") val briefRationale: String?,
    @SerializedName("recommendation_date") val recommendationDate: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class IdeasListResponseDto(
    @SerializedName("has_data") val hasData: Boolean,
    @SerializedName("pagination") val pagination: IdeasPaginationDto,
    @SerializedName("items") val items: List<IdeaDto>
)

data class IdeasPaginationDto(
    @SerializedName("limit") val limit: Int,
    @SerializedName("offset") val offset: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("count") val count: Int
)

data class IdeaDetailResponseDto(
    @SerializedName("has_data") val hasData: Boolean,
    @SerializedName("item") val item: IdeaDto
)

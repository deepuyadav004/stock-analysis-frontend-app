package com.genxsolutions.growwealth.feature.news.data.dto

import com.google.gson.annotations.SerializedName

data class NewsItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("source") val source: String,
    @SerializedName("headline") val headline: String,
    @SerializedName("article_url") val articleUrl: String,
    @SerializedName("published_at") val publishedAt: String?
)

data class NewsListResponseDto(
    @SerializedName("has_data") val hasData: Boolean,
    @SerializedName("pagination") val pagination: NewsPaginationDto,
    @SerializedName("items") val items: List<NewsItemDto>
)

data class NewsPaginationDto(
    @SerializedName("limit") val limit: Int,
    @SerializedName("offset") val offset: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("count") val count: Int
)

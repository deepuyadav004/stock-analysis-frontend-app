package com.genxsolutions.growwealth.feature.news.data.mapper

import com.genxsolutions.growwealth.feature.news.data.dto.NewsItemDto
import com.genxsolutions.growwealth.feature.news.domain.NewsItem

fun NewsItemDto.toDomain(): NewsItem {
    return NewsItem(
        id = id,
        source = source,
        headline = headline,
        articleUrl = articleUrl,
        publishedAt = publishedAt
    )
}

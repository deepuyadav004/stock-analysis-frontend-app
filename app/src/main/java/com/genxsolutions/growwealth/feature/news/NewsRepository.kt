package com.genxsolutions.growwealth.feature.news

import com.genxsolutions.growwealth.data.remote.GrowWealthApi
import com.genxsolutions.growwealth.feature.news.data.mapper.toDomain
import com.genxsolutions.growwealth.feature.news.domain.NewsItem

data class NewsPage(
    val items: List<NewsItem>,
    val totalCount: Int,
    val hasMore: Boolean
)

class NewsRepository(private val api: GrowWealthApi) {
    suspend fun fetchNews(
        source: String?,
        limit: Int,
        offset: Int
    ): NewsPage {
        val response = api.getNewsList(source = source, limit = limit, offset = offset)
        val items = response.items.map { it.toDomain() }
        val hasMore = response.pagination.offset + response.pagination.count < response.pagination.total

        return NewsPage(
            items = items,
            totalCount = response.pagination.total,
            hasMore = hasMore
        )
    }
}

package com.genxsolutions.growwealth.feature.news.domain

data class NewsItem(
    val id: Int,
    val source: String,
    val headline: String,
    val articleUrl: String,
    val publishedAt: String?
)

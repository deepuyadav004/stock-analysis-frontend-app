package com.genxsolutions.growwealth.feature.ideas.domain

data class Idea(
    val id: Int,
    val ticker: String,
    val companyName: String,
    val callType: CallType,
    val targetPrice: Double?,
    val horizon: Horizon,
    val source: Source,
    val briefRationale: String?,
    val recommendationDate: String?,
    val createdAt: String?
)

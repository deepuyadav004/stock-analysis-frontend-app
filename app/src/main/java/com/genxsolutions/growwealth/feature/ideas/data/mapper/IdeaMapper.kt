package com.genxsolutions.growwealth.feature.ideas.data.mapper

import com.genxsolutions.growwealth.feature.ideas.data.dto.IdeaDto
import com.genxsolutions.growwealth.feature.ideas.domain.CallType
import com.genxsolutions.growwealth.feature.ideas.domain.Horizon
import com.genxsolutions.growwealth.feature.ideas.domain.Idea
import com.genxsolutions.growwealth.feature.ideas.domain.Source

fun IdeaDto.toDomain(): Idea {
    return Idea(
        id = id,
        ticker = ticker,
        companyName = companyName,
        callType = callType.toCallType(),
        targetPrice = targetPrice,
        horizon = recommendationDate.toHorizon(),
        source = source.toSource(),
        briefRationale = briefRationale,
        recommendationDate = recommendationDate,
        createdAt = createdAt
    )
}

private fun String?.toCallType(): CallType {
    return when (this?.trim()?.uppercase()) {
        "BUY" -> CallType.BUY
        "SELL" -> CallType.SELL
        "HOLD" -> CallType.HOLD
        else -> CallType.UNKNOWN
    }
}

private fun String.toSource(): Source {
    return when (trim().lowercase()) {
        "moneycontrol" -> Source.MONEYCONTROL
        "kotakneo" -> Source.KOTAKNEO
        "lemonn" -> Source.LEMONN
        else -> Source.UNKNOWN
    }
}

private fun String?.toHorizon(): Horizon {
    if (this.isNullOrBlank()) return Horizon.UNKNOWN
    val normalized = this.trim().lowercase()
    return when {
        normalized.contains("short") || normalized.contains("1m") || normalized.contains("3m") -> Horizon.SHORT
        normalized.contains("medium") || normalized.contains("6m") -> Horizon.MEDIUM
        normalized.contains("long") || normalized.contains("1y") || normalized.contains("12m") -> Horizon.LONG
        else -> Horizon.UNKNOWN
    }
}

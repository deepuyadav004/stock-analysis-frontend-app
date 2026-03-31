package com.genxsolutions.growwealth.feature.ideas

import com.genxsolutions.growwealth.data.remote.GrowWealthApi
import com.genxsolutions.growwealth.feature.ideas.data.mapper.toDomain
import com.genxsolutions.growwealth.feature.ideas.domain.CallType
import com.genxsolutions.growwealth.feature.ideas.domain.Idea

data class IdeasPage(
    val items: List<Idea>,
    val totalCount: Int,
    val hasMore: Boolean
)

class IdeasRepository(private val api: GrowWealthApi) {
    suspend fun fetchIdeas(
        callType: CallType?,
        limit: Int,
        offset: Int
    ): IdeasPage {
        val response = api.getIdeasList(
            callType = callType.toApiValue(),
            limit = limit,
            offset = offset
        )

        val items = response.items.map { it.toDomain() }
        val hasMore = response.pagination.offset + response.pagination.count < response.pagination.total

        return IdeasPage(
            items = items,
            totalCount = response.pagination.total,
            hasMore = hasMore
        )
    }

    suspend fun fetchIdeaDetail(ideaId: Int): Idea = api.getIdeaDetail(ideaId).item.toDomain()
}

private fun CallType?.toApiValue(): String? {
    return when (this) {
        null, CallType.UNKNOWN -> null
        CallType.BUY -> "BUY"
        CallType.SELL -> "SELL"
        CallType.HOLD -> "HOLD"
    }
}
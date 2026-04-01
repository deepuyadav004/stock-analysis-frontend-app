package com.genxsolutions.growwealth.feature.news

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.genxsolutions.growwealth.core.SkeletonListLoader
import com.genxsolutions.growwealth.feature.news.domain.NewsItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
fun NewsScreen(
    state: NewsUiState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onArticleClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState, state.hasMore, state.isLoadingMore, state.items.size) {
        snapshotFlow {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val thresholdIndex = (state.items.lastIndex - 3).coerceAtLeast(0)
            val reachedListEnd = lastVisibleIndex >= thresholdIndex
            reachedListEnd && (listState.isScrollInProgress || !listState.canScrollForward)
        }
            .map { shouldLoad ->
                shouldLoad &&
                    state.hasMore &&
                    !state.isLoadingMore &&
                    state.items.isNotEmpty()
            }
            .distinctUntilChanged()
            .filter { it }
            .collectLatest { onLoadMore() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "News & Blogs",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 10.dp)
            )

            when {
                state.isLoading -> {
                    SkeletonListLoader(
                        contentPadding = PaddingValues(top = 2.dp),
                        verticalSpacing = 8.dp,
                        cardShape = MaterialTheme.shapes.medium,
                        cardColor = Color.White,
                        titleWidthStart = 0.62f,
                        titleWidthStep = 0.04f,
                        metaBlockWidths = listOf(90.dp, 70.dp)
                    )
                }

                state.errorMessage != null && state.items.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(state.errorMessage)
                        TextButton(onClick = onRefresh) {
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(state.items, key = { _, item -> item.id }) { _, item ->
                            NewsCard(item = item, onClick = { onArticleClick(item.articleUrl) })
                        }

                        if (state.isLoadingMore) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsCard(
    item: NewsItem,
    onClick: () -> Unit
) {
    val sourceAccent = sourceColor(item.source)
    val cardBackground = Color(0xFF162338)
    val primaryText = Color(0xFFF5F8FF)
    val secondaryText = Color(0xFFB7C4D8)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(1.dp, sourceAccent.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = Color(0xFFEAF2FF),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = sourceLabel(item.source),
                        color = Color(0xFF1F4B99),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            text = "NEWS",
                            fontWeight = FontWeight.Bold,
                            color = sourceAccent
                        )
                    }
                )
            }

            Text(
                text = item.headline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = primaryText,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.12f))
            )

            Text(
                text = item.publishedAt?.let { "Scraped ${formatDate(it)}" } ?: "Scraped recently",
                style = MaterialTheme.typography.labelMedium,
                color = secondaryText
            )
        }
    }
}

private fun sourceLabel(value: String): String {
    return when (value.lowercase()) {
        "icicidirect" -> "ICICI Direct"
        "economictimes" -> "Economic Times"
        "moneycontrol" -> "Moneycontrol"
        else -> value
    }
}

private fun sourceColor(value: String): Color {
    return when (value.lowercase()) {
        "icicidirect" -> Color(0xFF0B4C8C)
        "economictimes" -> Color(0xFFEF6C00)
        "moneycontrol" -> Color(0xFF00796B)
        else -> Color(0xFF334155)
    }
}

private fun formatDate(value: String): String {
    return if (value.length >= 10) value.substring(0, 10) else value
}

package com.genxsolutions.growwealth.feature.home

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.genxsolutions.growwealth.core.SkeletonListLoader
import com.genxsolutions.growwealth.data.remote.SectorDetailResponse
import com.genxsolutions.growwealth.data.remote.SectorSignal
import com.genxsolutions.growwealth.data.remote.SectorTrend

private val homeBackground = Color(0xFFECEFF4)
private val freshnessBackground = Color(0xFF102A4D)
private val freshnessAccent = Color(0xFF1F4F86)
private val moodBackground = Color(0xFFFBFCFE)
private val upColor = Color(0xFF1B8A5A)
private val downColor = Color(0xFFD64545)
private val neutralColor = Color(0xFF607D8B)
private val cardBorder = Color(0xFFE3E7EA)
private val headingColor = Color(0xFF0D2438)
private val sectorCardBackground = Color(0xFFFDFEFF)

@Composable
fun HomeScreen(
    state: HomeUiState,
    onRetry: () -> Unit,
    onSectorClick: (Int) -> Unit,
    onToggleWatchlist: (SectorSignal) -> Unit,
    watchlistSectorIds: Set<Int>,
    onCloseDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading && state.sectors.isEmpty() -> {
            HomeLoadingPlaceholder(modifier = modifier)
        }

        state.errorMessage != null && state.sectors.isEmpty() -> {
            CenterMessage(
                modifier = modifier,
                title = "Something went wrong",
                subtitle = state.errorMessage,
                actionLabel = "Retry",
                onAction = onRetry
            )
        }

        state.snapshot?.hasData == false || state.sectors.isEmpty() -> {
            CenterMessage(
                modifier = modifier,
                title = "No snapshot data available",
                subtitle = "Run the backend pipeline and try again.",
                actionLabel = "Refresh",
                onAction = onRetry
            )
        }

        else -> {
            HomeContent(
                state = state,
                onRetry = onRetry,
                onSectorClick = onSectorClick,
                onToggleWatchlist = onToggleWatchlist,
                watchlistSectorIds = watchlistSectorIds,
                onCloseDetail = onCloseDetail,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun HomeLoadingPlaceholder(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize(), color = homeBackground) {
        SkeletonListLoader(
            itemCount = 4,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalSpacing = 12.dp,
            cardShape = RoundedCornerShape(18.dp),
            cardColor = Color.White.copy(alpha = 0.86f),
            titleWidthStart = 0.55f,
            titleWidthStep = 0.08f,
            metaBlockWidths = listOf(120.dp),
            placeholderColorPrimary = Color(0xFFCAD7E5),
            placeholderColorSecondary = Color(0xFFD8E2EE),
            pulseMinAlpha = 0.45f,
            pulseDurationMs = 950
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeUiState,
    onRetry: () -> Unit,
    onSectorClick: (Int) -> Unit,
    onToggleWatchlist: (SectorSignal) -> Unit,
    watchlistSectorIds: Set<Int>,
    onCloseDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pullState = rememberPullToRefreshState()

    Surface(modifier = modifier.fillMaxSize(), color = homeBackground) {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRetry,
            state = pullState,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    FreshnessCard(
                        latestDate = state.snapshot?.latestDate.orEmpty(),
                        ageDays = state.snapshot?.ageDays ?: 0,
                        freshnessLabel = state.snapshot?.freshnessLabel.orEmpty()
                    )
                }
                item {
                    val mood = state.summary?.marketMood
                    MarketMoodCard(
                        upCount = mood?.upCount ?: 0,
                        downCount = mood?.downCount ?: 0,
                        neutralCount = mood?.neutralCount ?: 0,
                        avgConfidence = mood?.avgConfidence ?: 0.0,
                        avgSentiment = mood?.avgSentimentScore ?: 0.0
                    )
                }
                if (state.trends.isNotEmpty()) {
                    item {
                        MiniTrendStripCard(trends = state.trends)
                    }
                }
                item {
                    Text(
                        text = "Top Sector Signals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = headingColor,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                items(state.sectors) { sector ->
                    SectorSignalRow(
                        sector = sector,
                        isWatchlisted = watchlistSectorIds.contains(sector.sectorId),
                        onWatchlistClick = { onToggleWatchlist(sector) },
                        onClick = { onSectorClick(sector.sectorId) }
                    )
                }
            }

            if (state.selectedSectorDetail != null) {
                SectorDetailOverlay(
                    detail = state.selectedSectorDetail,
                    onClose = onCloseDetail,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun FreshnessCard(latestDate: String, ageDays: Int, freshnessLabel: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = freshnessBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(freshnessAccent.copy(alpha = 0.35f))
            )
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "NSE Snapshot",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "As of $latestDate",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2EEF8)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SoftBadge(text = "$ageDays days old", background = Color(0xFF214E82), textColor = Color.White)
                    SoftBadge(text = freshnessLabel, background = Color(0xFF3A7FC8), textColor = Color.White)
                }
            }
        }
    }
}

@Composable
private fun MarketMoodCard(
    upCount: Int,
    downCount: Int,
    neutralCount: Int,
    avgConfidence: Double,
    avgSentiment: Double
) {
    val marketReliability = confidenceLevel(avgConfidence)
    val marketMood = sentimentLabel(avgSentiment)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = moodBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Market Mood", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MoodChip(label = "Improving", value = upCount, color = upColor, modifier = Modifier.weight(1f))
                MoodChip(label = "Weakening", value = downCount, color = downColor, modifier = Modifier.weight(1f))
                MoodChip(label = "Stable", value = neutralCount, color = neutralColor, modifier = Modifier.weight(1f))
            }
            Divider(color = cardBorder)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Today signals look", color = Color(0xFF4B5B67))
                Text(marketReliability, fontWeight = FontWeight.SemiBold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("News tone is", color = Color(0xFF4B5B67))
                Text(marketMood, fontWeight = FontWeight.SemiBold)
            }
            Text(
                "Quick guidance only, not a guarantee.",
                color = Color(0xFF738592),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MiniTrendStripCard(trends: List<SectorTrend>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = moodBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "7-Day Sector Pulse",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = headingColor
            )
            trends.take(5).forEach { trend ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trend.sectorName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2F4654)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        trend.points.takeLast(7).forEach { point ->
                            val color = when {
                                point.sentimentScore > 0.05 -> upColor
                                point.sentimentScore < -0.05 -> downColor
                                else -> neutralColor
                            }
                            Box(
                                modifier = Modifier
                                    .size(width = 10.dp, height = 14.dp)
                                    .background(color = color.copy(alpha = 0.8f), shape = RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectorSignalRow(
    sector: SectorSignal,
    isWatchlisted: Boolean,
    onWatchlistClick: () -> Unit,
    onClick: () -> Unit
) {
    val signalColor = when (sector.signal) {
        "UP" -> upColor
        "DOWN" -> downColor
        else -> neutralColor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = sectorCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(120.dp)
                    .background(signalColor)
            )
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(signalColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        sector.sectorName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onWatchlistClick) {
                        if (isWatchlisted) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Remove from watchlist",
                                tint = Color(0xFFF39C12)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.StarOutline,
                                contentDescription = "Add to watchlist",
                                tint = Color(0xFF738592)
                            )
                        }
                    }
                    SoftBadge(
                        text = signalText(sector.signal),
                        background = signalColor.copy(alpha = 0.14f),
                        textColor = signalColor
                    )
                }
                Text(
                    text = shortSectorSummary(sector),
                    color = Color(0xFF2F4654),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("News tone", color = Color(0xFF4B5B67))
                    Text(sentimentLabel(sector.sentimentScore), fontWeight = FontWeight.SemiBold)
                }
                Text(
                    text = "Snapshot ${sector.snapshotDate}",
                    color = Color(0xFF738592),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun SectorDetailOverlay(
    detail: SectorDetailResponse,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xA6000000))
            .padding(18.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = detail.sector?.sectorName ?: "Sector Detail",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = headingColor
                )
                Text(
                    text = "${detail.range?.from.orEmpty()} to ${detail.range?.to.orEmpty()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF607380)
                )
                Divider(color = cardBorder)
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(detail.timeline) { point ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(point.date, fontWeight = FontWeight.SemiBold)
                                    SoftBadge(
                                        text = signalText(point.signal),
                                        background = signalColor(point.signal).copy(alpha = 0.14f),
                                        textColor = signalColor(point.signal)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Confidence", color = Color(0xFF4B5B67))
                                    Text(confidenceLevel(point.confidence), fontWeight = FontWeight.SemiBold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("News tone", color = Color(0xFF4B5B67))
                                    Text(sentimentLabel(point.sentimentScore), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun MoodChip(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(value.toString(), color = color, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

private fun signalText(signal: String): String {
    return when (signal) {
        "UP" -> "Improving"
        "DOWN" -> "Weakening"
        else -> "Stable"
    }
}

private fun signalColor(signal: String): Color {
    return when (signal) {
        "UP" -> upColor
        "DOWN" -> downColor
        else -> neutralColor
    }
}

private fun confidenceLevel(value: Double): String {
    return when {
        value >= 0.75 -> "Very Strong"
        value >= 0.55 -> "Strong"
        value >= 0.35 -> "Moderate"
        value >= 0.2 -> "Low"
        else -> "Very Low"
    }
}

private fun sentimentLabel(value: Double): String {
    return when {
        value >= 0.2 -> "Positive"
        value >= 0.05 -> "Slightly Positive"
        value <= -0.2 -> "Negative"
        value <= -0.05 -> "Slightly Negative"
        else -> "Balanced"
    }
}

private fun shortSectorSummary(sector: SectorSignal): String {
    val direction = when (sector.signal) {
        "UP" -> "Looks improving"
        "DOWN" -> "Looks weakening"
        else -> "Looks stable"
    }
    return "$direction | Confidence ${confidenceLevel(sector.confidence)}"
}

@Composable
private fun SoftBadge(text: String, background: Color, textColor: Color) {
    Surface(color = background, shape = RoundedCornerShape(999.dp)) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun CenterMessage(
    modifier: Modifier,
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        if (subtitle != null) {
            Text(subtitle, modifier = Modifier.padding(top = 8.dp), color = Color(0xFF5D6D77))
        }
        if (actionLabel != null && onAction != null) {
            Button(onClick = onAction, modifier = Modifier.padding(top = 16.dp), shape = RoundedCornerShape(12.dp)) {
                Text(actionLabel)
            }
        }
    }
}

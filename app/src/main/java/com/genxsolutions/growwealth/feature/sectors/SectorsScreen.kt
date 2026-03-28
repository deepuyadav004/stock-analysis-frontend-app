package com.genxsolutions.growwealth.feature.sectors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.genxsolutions.growwealth.data.remote.SectorSignal

private val sectorCardBackground = Color(0xFFFDFEFF)
private val upColor = Color(0xFF1B8A5A)
private val downColor = Color(0xFFD64545)
private val neutralColor = Color(0xFF607D8B)
private val background = Color(0xFFECEFF4)
private val headingColor = Color(0xFF0D2438)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorsScreen(
    state: SectorsUiState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onFilterChange: (SectorFilter) -> Unit,
    onSectorClick: (Int) -> Unit,
    onToggleWatchlist: (SectorSignal) -> Unit,
    watchlistSectorIds: Set<Int>,
    modifier: Modifier = Modifier
) {
    var showFilterDialog by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    Surface(modifier = modifier.fillMaxSize(), color = background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Sectors") },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = headingColor
                )
            )

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                state = pullState,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    state.isLoading && state.items.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Loading sectors...")
                        }
                    }

                    state.errorMessage != null && state.items.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Unable to load sectors")
                                Text(state.errorMessage, style = MaterialTheme.typography.bodySmall)
                                Button(onClick = onRefresh, modifier = Modifier.padding(top = 16.dp)) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    state.items.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No sectors match your filters")
                        }
                    }

                    else -> {
                        SectorsList(
                            items = state.items,
                            isLoadingMore = state.isLoadingMore,
                            hasMore = state.hasMorePages,
                            onLoadMore = onLoadMore,
                            onSectorClick = onSectorClick,
                            onToggleWatchlist = onToggleWatchlist,
                            watchlistSectorIds = watchlistSectorIds,
                            totalCount = state.totalCount
                        )
                    }
                }
            }
        }

        if (showFilterDialog) {
            FilterDialog(
                filter = state.filter,
                onApply = { newFilter ->
                    onFilterChange(newFilter)
                    showFilterDialog = false
                },
                onDismiss = { showFilterDialog = false }
            )
        }
    }
}

@Composable
private fun SectorsList(
    items: List<SectorSignal>,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onSectorClick: (Int) -> Unit,
    onToggleWatchlist: (SectorSignal) -> Unit,
    watchlistSectorIds: Set<Int>,
    totalCount: Int
) {
    val listState = rememberLazyListState()
    val isNearBottom by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisibleItem >= items.size - 3
        }
    }

    LaunchedEffect(isNearBottom) {
        if (isNearBottom && hasMore && !isLoadingMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Showing ${items.size} of $totalCount sectors",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF607380)
            )
        }

        items(items) { sector ->
            SectorCard(
                sector = sector,
                isWatchlisted = watchlistSectorIds.contains(sector.sectorId),
                onWatchlistClick = { onToggleWatchlist(sector) },
                onClick = { onSectorClick(sector.sectorId) }
            )
        }

        if (isLoadingMore) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Loading...", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SectorCard(
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = sectorCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(100.dp)
                    .background(signalColor)
            )
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(signalColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        sector.sectorName,
                        style = MaterialTheme.typography.labelLarge,
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
                        text = signalLabel(sector.signal),
                        background = signalColor.copy(alpha = 0.14f),
                        textColor = signalColor
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Confidence", color = Color(0xFF4B5B67), style = MaterialTheme.typography.bodySmall)
                    Text(confidenceLevel(sector.confidence), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("News tone", color = Color(0xFF4B5B67), style = MaterialTheme.typography.bodySmall)
                    Text(sentimentLabel(sector.sentimentScore), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun FilterDialog(
    filter: SectorFilter,
    onApply: (SectorFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var signals by remember { mutableStateOf(filter.signals) }
    var minConfidence by remember { mutableStateOf(filter.confidenceMin) }
    var maxConfidence by remember { mutableStateOf(filter.confidenceMax) }
    var sort by remember { mutableStateOf(filter.sort) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(640.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Prevent dismissal when clicking inside card
                ),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Filter Sectors", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Divider()

                    Text("Signal", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        listOf("UP", "DOWN", "NEUTRAL").forEach { sig ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    signals = if (signals.contains(sig)) signals - sig else signals + sig
                                }
                            ) {
                                Checkbox(
                                    checked = signals.contains(sig),
                                    onCheckedChange = { checked ->
                                        signals = if (checked) signals + sig else signals - sig
                                    }
                                )
                                Text(sig, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Divider()

                    Text("Confidence Range", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Min: ${"%.2f".format(minConfidence)}", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = minConfidence.toFloat(),
                            onValueChange = { minConfidence = it.toDouble() },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Max: ${"%.2f".format(maxConfidence)}", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = maxConfidence.toFloat(),
                            onValueChange = { maxConfidence = it.toDouble() },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Divider()

                    Text("Sort By", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val sortOptions = listOf(
                        "confidence_desc" to "Highest Confidence",
                        "confidence_asc" to "Lowest Confidence",
                        "sentiment_desc" to "Most Positive",
                        "sentiment_asc" to "Most Negative",
                        "name_asc" to "Name (A-Z)",
                        "name_desc" to "Name (Z-A)"
                    )

                    sortOptions.forEach { option ->
                        val key = option.first
                        val label = option.second
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { sort = key }
                        ) {
                            RadioButton(
                                selected = sort == key,
                                onClick = { sort = key }
                            )
                            Text(label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onApply(
                                SectorFilter(
                                    signals = signals,
                                    confidenceMin = minConfidence,
                                    confidenceMax = maxConfidence,
                                    sort = sort
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun SoftBadge(text: String, background: Color, textColor: Color) {
    Surface(color = background, shape = RoundedCornerShape(999.dp)) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun signalLabel(signal: String): String {
    return when (signal) {
        "UP" -> "Improving"
        "DOWN" -> "Weakening"
        else -> "Stable"
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

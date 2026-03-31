package com.genxsolutions.growwealth.feature.companies

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.input.pointer.pointerInput
import com.genxsolutions.growwealth.core.SkeletonListLoader
import com.genxsolutions.growwealth.data.remote.CompanyListItem
import com.genxsolutions.growwealth.data.remote.CompanyPerformancePoint
import com.genxsolutions.growwealth.data.remote.CompanyPerformanceResponse
import kotlin.math.roundToInt

private val background = Color(0xFFECEFF4)
private val cardBg = Color(0xFFFFFFFF)
private val headingColor = Color(0xFF0D2438)
private val upColor = Color(0xFF1B8A5A)
private val downColor = Color(0xFFD64545)
private val neutralColor = Color(0xFF607D8B)
private val filterCardBg = Color(0xFFF8FBFF)
private val filterCardBorder = Color(0xFFD9E4F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompaniesScreen(
    state: CompaniesUiState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onApplyFilters: (String, String?) -> Unit,
    onCompanyClick: (Int) -> Unit,
    onToggleWatchlist: (CompanyListItem) -> Unit,
    watchlistCompanyIds: Set<Int>,
    onSelectRange: (String) -> Unit,
    onCloseDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pullState = rememberPullToRefreshState()
    var showFilterDialog by remember { mutableStateOf(false) }

    Surface(modifier = modifier.fillMaxSize(), color = background) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchAndFilterBar(
                filter = state.filter,
                onOpenFilter = { showFilterDialog = true },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
            )

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                state = pullState,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    state.isLoading && state.items.isEmpty() -> {
                        CompaniesLoadingPlaceholder()
                    }

                    state.errorMessage != null && state.items.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Unable to load companies")
                                Text(state.errorMessage, style = MaterialTheme.typography.bodySmall)
                                Button(onClick = onRefresh, modifier = Modifier.padding(top = 14.dp)) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    state.items.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No companies found for selected filters")
                        }
                    }

                    else -> {
                        CompaniesList(
                            items = state.items,
                            totalCount = state.totalCount,
                            hasMore = state.hasMore,
                            isLoadingMore = state.isLoadingMore,
                            onLoadMore = onLoadMore,
                            onCompanyClick = onCompanyClick,
                            onToggleWatchlist = onToggleWatchlist,
                            watchlistCompanyIds = watchlistCompanyIds
                        )
                    }
                }
            }
        }

        if (showFilterDialog) {
            CompanyFilterDialog(
                filter = state.filter,
                onApply = { query, selectedSignal ->
                    onApplyFilters(query, selectedSignal)
                    showFilterDialog = false
                },
                onDismiss = { showFilterDialog = false }
            )
        }

        if (state.selectedCompanySummary != null && state.selectedPerformance != null) {
            CompanyDetailOverlay(
                state = state,
                performance = state.selectedPerformance,
                onSelectRange = onSelectRange,
                onClose = onCloseDetail
            )
        }
    }
}

@Composable
private fun CompaniesLoadingPlaceholder() {
    SkeletonListLoader()
}

@Composable
private fun SearchAndFilterBar(
    filter: CompanyFilter,
    onOpenFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = filterCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, filterCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenFilter),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD8E6))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Open search and filters", color = Color(0xFF3D566B), fontWeight = FontWeight.SemiBold)
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Open filters",
                        tint = Color(0xFF2E5C92)
                    )
                }
            }

            val activeFilterText = when (filter.signal) {
                null -> "All signals"
                else -> "Signal: ${filter.signal}"
            }
            Text(
                text = activeFilterText,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5A7083)
            )
        }
    }
}

@Composable
private fun CompanyFilterDialog(
    filter: CompanyFilter,
    onApply: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf(filter.query) }
    var selectedSignal by remember { mutableStateOf(filter.signal) }

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
                .height(430.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filter Companies", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text("Signal", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    label = { Text("Search company or ticker") }
                )

                listOf<String?>(null, "UP", "DOWN", "NEUTRAL").forEach { signal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSignal = signal }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            when (signal) {
                                null -> "All"
                                else -> signal
                            }
                        )
                        RadioButton(
                            selected = selectedSignal == signal,
                            onClick = { selectedSignal = signal }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { selectedSignal = null },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear")
                    }
                    Button(
                        onClick = { onApply(query, selectedSignal) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun CompaniesList(
    items: List<CompanyListItem>,
    totalCount: Int,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    onCompanyClick: (Int) -> Unit,
    onToggleWatchlist: (CompanyListItem) -> Unit,
    watchlistCompanyIds: Set<Int>
) {
    val listState = rememberLazyListState()
    val isNearBottom by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= items.size - 3
        }
    }

    LaunchedEffect(isNearBottom, hasMore, isLoadingMore) {
        if (isNearBottom && hasMore && !isLoadingMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Showing ${items.size} of $totalCount companies",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF607380)
            )
        }

        items(items) { company ->
            CompanyRow(
                company = company,
                isWatchlisted = watchlistCompanyIds.contains(company.companyId),
                onWatchlistClick = { onToggleWatchlist(company) },
                onClick = { onCompanyClick(company.companyId) }
            )
        }

        if (isLoadingMore) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Loading more...")
                }
            }
        }
    }
}

@Composable
private fun CompanyRow(
    company: CompanyListItem,
    isWatchlisted: Boolean,
    onWatchlistClick: () -> Unit,
    onClick: () -> Unit
) {
    val signalColor = when (company.signal) {
        "UP" -> upColor
        "DOWN" -> downColor
        else -> neutralColor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(96.dp)
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = company.companyName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = company.ticker,
                        color = Color(0xFF4C6475),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
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
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Close", color = Color(0xFF4B5B67), style = MaterialTheme.typography.bodySmall)
                    Text("${formatPrice(company.latestClose)}", fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Daily move", color = Color(0xFF4B5B67), style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${formatSignedPercent(company.dayChangePct)}%",
                        fontWeight = FontWeight.SemiBold,
                        color = signalColor
                    )
                }
            }
        }
    }
}

@Composable
private fun CompanyDetailOverlay(
    state: CompaniesUiState,
    performance: CompanyPerformanceResponse,
    onSelectRange: (String) -> Unit,
    onClose: () -> Unit
) {
    val summary = state.selectedCompanySummary ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(summary.companyName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${summary.ticker} • ${summary.exchangeCode}", color = Color(0xFF607380))
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                val moveColor = when {
                    summary.dayChangePct > 0.0 -> upColor
                    summary.dayChangePct < 0.0 -> downColor
                    else -> neutralColor
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Latest close", color = Color(0xFF4B5B67))
                    Text(formatPrice(summary.latestClose), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Day change", color = Color(0xFF4B5B67))
                    Text(
                        "${formatSignedPercent(summary.dayChangePct)}%",
                        color = moveColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("1W", "1M", "1Y", "3Y", "5Y", "10Y").forEach { range ->
                        FilterChip(
                            selected = state.selectedRange == range,
                            onClick = { onSelectRange(range) },
                            label = { Text(range) }
                        )
                    }
                }

                if (state.isDetailLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Loading performance...")
                    }
                } else {
                    PerformanceChart(
                        points = performance.points,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )

                    val periodColor = when {
                        performance.periodChangePct > 0.0 -> upColor
                        performance.periodChangePct < 0.0 -> downColor
                        else -> neutralColor
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Period return", color = Color(0xFF4B5B67))
                        Text(
                            "${formatSignedPercent(performance.periodChangePct)}%",
                            color = periodColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("From", color = Color(0xFF4B5B67))
                        Text(performance.fromDate ?: "-")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("To", color = Color(0xFF4B5B67))
                        Text(performance.toDate ?: "-")
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceChart(points: List<CompanyPerformancePoint>, modifier: Modifier = Modifier) {
    if (points.size < 2) {
        Box(modifier = modifier.background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Text("Not enough points for chart", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    var selectedIndex by remember(points) { mutableStateOf(points.lastIndex) }

    val min = points.minOfOrNull { it.close } ?: 0.0
    val max = points.maxOfOrNull { it.close } ?: 0.0
    val range = (max - min).takeIf { it > 0.0 } ?: 1.0
    val lineColor = when {
        points.last().close > points.first().close -> upColor
        points.last().close < points.first().close -> downColor
        else -> neutralColor
    }

    val selectedPoint = points[selectedIndex]

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedPoint.date,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5A7083)
            )
            Text(
                text = formatPrice(selectedPoint.close),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF21384D)
            )
        }

        Canvas(
            modifier = modifier
                .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                .pointerInput(points) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val width = size.width.toFloat().coerceAtLeast(1f)
                            val rawIndex = ((offset.x / width) * (points.size - 1)).roundToInt()
                            selectedIndex = rawIndex.coerceIn(0, points.lastIndex)
                        },
                        onDrag = { change, _ ->
                            val width = size.width.toFloat().coerceAtLeast(1f)
                            val rawIndex = ((change.position.x / width) * (points.size - 1)).roundToInt()
                            selectedIndex = rawIndex.coerceIn(0, points.lastIndex)
                            change.consume()
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val stepX = width / (points.size - 1)

            val path = Path()
            points.forEachIndexed { index, point ->
                val x = index * stepX
                val normalized = ((point.close - min) / range).toFloat()
                val y = height - normalized * (height - 20f) - 10f
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawLine(
                color = Color(0xFFE1E8EE),
                start = Offset(0f, height - 12f),
                end = Offset(width, height - 12f),
                strokeWidth = 2f
            )
            drawPath(path = path, color = lineColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))

            val selectedX = selectedIndex * stepX
            val selectedNormalized = ((points[selectedIndex].close - min) / range).toFloat()
            val selectedY = height - selectedNormalized * (height - 20f) - 10f

            // Crosshair line from top to bottom at touched point.
            drawLine(
                color = Color(0xFF8AA2BA),
                start = Offset(selectedX, 0f),
                end = Offset(selectedX, height),
                strokeWidth = 2f
            )

            drawCircle(
                color = lineColor,
                radius = 7f,
                center = Offset(selectedX, selectedY)
            )
            drawCircle(
                color = Color.White,
                radius = 3f,
                center = Offset(selectedX, selectedY)
            )
        }
    }
}

private fun formatPrice(value: Double): String = "₹" + String.format("%,.2f", value)

private fun formatSignedPercent(value: Double): String {
    val formatted = String.format("%.2f", kotlin.math.abs(value))
    return if (value >= 0) "+$formatted" else "-$formatted"
}

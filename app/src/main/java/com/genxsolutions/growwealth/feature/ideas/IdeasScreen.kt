package com.genxsolutions.growwealth.feature.ideas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.genxsolutions.growwealth.feature.ideas.domain.CallType
import com.genxsolutions.growwealth.feature.ideas.domain.Idea
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
fun IdeasScreen(
    state: IdeasUiState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onSelectCallType: (CallType?) -> Unit,
    onIdeaClick: (Int) -> Unit,
    onSaveToWatchlist: (Idea) -> Unit,
    onCloseDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val premiumBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF4EEE3),
            Color(0xFFE7EEF9),
            Color(0xFFE3ECFA)
        )
    )
    var showHeader by remember { mutableStateOf(true) }
    var lastScrollPosition by remember { mutableIntStateOf(0) }
    var scrollDeltaAccumulator by remember { mutableIntStateOf(0) }

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

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collectLatest { (index, offset) ->
            val currentPosition = index * 100000 + offset
            val delta = currentPosition - lastScrollPosition
            val visibilityThresholdPx = 28

            when {
                index == 0 && offset < 24 -> {
                    showHeader = true
                    scrollDeltaAccumulator = 0
                }
                delta > 0 -> {
                    scrollDeltaAccumulator += delta
                    if (showHeader && scrollDeltaAccumulator > visibilityThresholdPx) {
                        showHeader = false
                        scrollDeltaAccumulator = 0
                    }
                }
                delta < 0 -> {
                    scrollDeltaAccumulator += -delta
                    if (!showHeader && scrollDeltaAccumulator > visibilityThresholdPx) {
                        showHeader = true
                        scrollDeltaAccumulator = 0
                    }
                }
            }
            lastScrollPosition = currentPosition
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(premiumBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = showHeader,
                enter =
                    fadeIn(animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)) +
                    slideInVertically(
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                        initialOffsetY = { -it / 3 }
                    ) +
                    expandVertically(animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)),
                exit =
                    fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)) +
                    slideOutVertically(
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                        targetOffsetY = { -it / 3 }
                    ) +
                    shrinkVertically(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CallTypeFilterRow(selected = state.selectedCallType, onSelectCallType = onSelectCallType)
                }
            }

            when {
                state.isLoading -> {
                    IdeasLoadingPlaceholder()
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
                        itemsIndexed(state.items, key = { _, item -> item.id }) { _, idea ->
                            IdeaCard(idea = idea, onClick = { onIdeaClick(idea.id) })
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

        AnimatedVisibility(
            visible = !showHeader,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
            enter = fadeIn(animationSpec = tween(220)) + slideInVertically(initialOffsetY = { -it / 2 }),
            exit = fadeOut(animationSpec = tween(180)) + slideOutVertically(targetOffsetY = { -it / 2 })
        ) {
            Surface(
                color = Color(0xCC142238),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
            ) {
                Text(
                    text = "Ideas • ${state.totalCount}",
                    color = Color(0xFFF5F8FF),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }

    if (state.selectedIdea != null || state.isDetailLoading || state.detailErrorMessage != null) {
        IdeaDetailSheet(
            idea = state.selectedIdea,
            isLoading = state.isDetailLoading,
            error = state.detailErrorMessage,
            onSaveToWatchlist = onSaveToWatchlist,
            onDismiss = onCloseDetail
        )
    }
}

@Composable
private fun IdeasLoadingPlaceholder() {
    val pulse = rememberInfiniteTransition(label = "ideas-loader")
    val animatedAlpha = pulse.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ideas-loader-alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(6) { index ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((0.58f + index * 0.05f).coerceAtMost(0.9f))
                            .height(14.dp)
                            .background(
                                Color(0xFFCFDBE9).copy(alpha = animatedAlpha.value),
                                MaterialTheme.shapes.small
                            )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .width(86.dp)
                                .height(10.dp)
                                .background(
                                    Color(0xFFDEE7F1).copy(alpha = animatedAlpha.value),
                                    MaterialTheme.shapes.small
                                )
                        )
                        Box(
                            modifier = Modifier
                                .width(98.dp)
                                .height(10.dp)
                                .background(
                                    Color(0xFFDEE7F1).copy(alpha = animatedAlpha.value),
                                    MaterialTheme.shapes.small
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CallTypeFilterRow(
    selected: CallType?,
    onSelectCallType: (CallType?) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = selected == null, onClick = { onSelectCallType(null) }, label = { Text("All Calls") })
        FilterChip(
            selected = selected == CallType.BUY,
            onClick = { onSelectCallType(CallType.BUY) },
            label = { Text("BUY") }
        )
        FilterChip(
            selected = selected == CallType.HOLD,
            onClick = { onSelectCallType(CallType.HOLD) },
            label = { Text("HOLD") }
        )
        FilterChip(
            selected = selected == CallType.SELL,
            onClick = { onSelectCallType(CallType.SELL) },
            label = { Text("SELL") }
        )
    }
}

@Composable
private fun IdeaCard(
    idea: Idea,
    onClick: () -> Unit
) {
    val callAccent = callTypeColor(idea.callType)
    val cardBackground = Color(0xFF162338)
    val primaryText = Color(0xFFF5F8FF)
    val secondaryText = Color(0xFFB7C4D8)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(1.dp, callAccent.copy(alpha = 0.45f)),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = idea.companyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = idea.ticker,
                        style = MaterialTheme.typography.labelMedium,
                        color = secondaryText
                    )
                }

                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            text = idea.callType.name,
                            fontWeight = FontWeight.Bold,
                            color = callAccent
                        )
                    }
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetaBadge(label = sourceLabel(idea), background = Color(0xFFEAF2FF), content = Color(0xFF1F4B99))
                if (idea.targetPrice != null) {
                    MetaBadge(
                        label = "Target ${idea.targetPrice}",
                        background = callAccent.copy(alpha = 0.14f),
                        content = Color(0xFFF7FAFF)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.12f))
            )

            if (!idea.briefRationale.isNullOrBlank()) {
                Text(
                    text = idea.briefRationale,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tap for full analysis",
                    style = MaterialTheme.typography.labelSmall,
                    color = callAccent,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = idea.recommendationDate ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = secondaryText
                )
            }
        }
    }
}

@Composable
private fun MetaBadge(
    label: String,
    background: Color,
    content: Color
) {
    Surface(
        color = background,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = content,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun callTypeColor(callType: CallType): Color {
    return when (callType) {
        CallType.BUY -> Color(0xFF0B8F55)
        CallType.SELL -> Color(0xFFC62828)
        CallType.HOLD -> Color(0xFFD68910)
        CallType.UNKNOWN -> Color(0xFF546E7A)
    }
}

private fun sourceLabel(idea: Idea): String {
    return when (idea.source.name) {
        "KOTAKNEO" -> "Kotak"
        "MONEYCONTROL" -> "Moneycontrol"
        "LEMONN" -> "Lemonn"
        else -> idea.source.name
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun IdeaDetailSheet(
    idea: Idea?,
    isLoading: Boolean,
    error: String?,
    onSaveToWatchlist: (Idea) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121D30)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                isLoading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator()
                    }
                    Text(
                        text = "Fetching recommendation detail...",
                        color = Color(0xFFE3EBF7),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                error != null -> {
                    Text(
                        text = "Unable to load detail",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFFD7D7)
                    )
                    Text(
                        text = error,
                        color = Color(0xFFE3EBF7),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                idea != null -> {
                    Text(
                        text = idea.companyName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFFF6F9FF)
                    )

                    Text(
                        text = idea.ticker,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFFB7C4D8)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetaBadge(
                            label = idea.callType.name,
                            background = callTypeColor(idea.callType).copy(alpha = 0.25f),
                            content = Color(0xFFF5F8FF)
                        )
                        MetaBadge(
                            label = sourceLabel(idea),
                            background = Color(0xFF1E2E48),
                            content = Color(0xFFDCE8FF)
                        )
                        MetaBadge(
                            label = idea.horizon.name,
                            background = Color(0xFF27395A),
                            content = Color(0xFFD9E6FF)
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

                    DetailRow(label = "Target", value = idea.targetPrice?.toString() ?: "-")
                    DetailRow(label = "Date", value = idea.recommendationDate ?: "-")

                    Text(
                        text = "Rationale",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFFE6EEFB)
                    )

                    Text(
                        text = idea.briefRationale ?: "No rationale provided.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCEDAF0)
                    )

                    TextButton(onClick = { onSaveToWatchlist(idea) }) {
                        Text("Save to Watchlist", color = Color(0xFF8ED5A3), fontWeight = FontWeight.SemiBold)
                    }
                }

                else -> {
                    Text(
                        text = "No detail available.",
                        color = Color(0xFFE3EBF7),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("Close", color = Color(0xFFD8E7FF))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF9FB0CA)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFF2F7FF),
            fontWeight = FontWeight.SemiBold
        )
    }
}

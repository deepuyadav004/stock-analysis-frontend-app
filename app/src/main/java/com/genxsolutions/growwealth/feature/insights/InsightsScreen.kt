package com.genxsolutions.growwealth.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.genxsolutions.growwealth.data.remote.InsightsCompareResponse
import com.genxsolutions.growwealth.data.remote.InsightsStabilityResponse

private val background = Color(0xFFF1F5F8)
private val card = Color(0xFFFFFFFF)
private val heading = Color(0xFF0D2438)
private val subtle = Color(0xFF5C6F7C)
private val positive = Color(0xFF1B8A5A)
private val negative = Color(0xFFD64545)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    state: InsightsUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pullState = rememberPullToRefreshState()

    Surface(modifier = modifier.fillMaxSize(), color = background) {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            state = pullState,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    CenterMessage(title = "Loading insights...")
                }

                state.errorMessage != null && state.compare7 == null && state.compare30 == null -> {
                    CenterMessage(
                        title = "Unable to load insights",
                        subtitle = state.errorMessage,
                        actionLabel = "Retry",
                        onAction = onRefresh
                    )
                }

                else -> {
                    InsightsContent(state)
                }
            }
        }
    }
}

@Composable
private fun InsightsContent(state: InsightsUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Insights",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = heading
            )
        }

        item {
            CompareCard(
                title = "7-Day Snapshot",
                data = state.compare7,
                emptyText = "Not enough 7-day history yet."
            )
        }

        item {
            CompareCard(
                title = "30-Day Snapshot",
                data = state.compare30,
                emptyText = "Not enough 30-day history yet."
            )
        }

        item {
            StabilitySummaryCard(state.stability30)
        }

        if (state.stability30?.items?.isNotEmpty() == true) {
            item {
                Text(
                    text = "Most Stable Sectors",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = heading
                )
            }
            items(state.stability30.items.take(5)) { item ->
                StabilityRow(item.sectorName, item.latestSignal, item.stabilityRatio, item.avgConfidence)
            }
        }
    }
}

@Composable
private fun CompareCard(title: String, data: InsightsCompareResponse?, emptyText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = card),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (data == null || !data.hasData || data.market == null) {
                Text(emptyText, color = subtle)
                return@Column
            }

            val market = data.market
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("UP / DOWN / NEUTRAL", color = subtle)
                Text("${market.upCount} / ${market.downCount} / ${market.neutralCount}", fontWeight = FontWeight.SemiBold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Avg confidence", color = subtle)
                Text("${(market.avgConfidence * 100).toInt()}%", fontWeight = FontWeight.SemiBold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Avg sentiment", color = subtle)
                Text(String.format("%.2f", market.avgSentiment), fontWeight = FontWeight.SemiBold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Change vs previous", color = subtle)
                Text(
                    text = String.format("%+.2f", market.sentimentChangeVsPrevious),
                    color = if (market.sentimentChangeVsPrevious >= 0) positive else negative,
                    fontWeight = FontWeight.Bold
                )
            }

            data.leaders?.strongest?.let { strongest ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Strongest sector", color = subtle)
                    Text(strongest.sectorName, fontWeight = FontWeight.SemiBold)
                }
            }
            data.leaders?.weakest?.let { weakest ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Weakest sector", color = subtle)
                    Text(weakest.sectorName, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun StabilitySummaryCard(data: InsightsStabilityResponse?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = card),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Signal Stability (30 Days)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (data == null || !data.hasData || data.summary == null || data.items.isEmpty()) {
                Text("Not enough history for stability analysis yet.", color = subtle)
                return@Column
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Stable sectors", color = subtle)
                Text("${data.summary.stableSectorCount}/${data.summary.totalSectors}", fontWeight = FontWeight.SemiBold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Avg stability", color = subtle)
                Text("${(data.summary.avgStabilityRatio * 100).toInt()}%", fontWeight = FontWeight.SemiBold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Avg confidence", color = subtle)
                Text("${(data.summary.avgConfidence * 100).toInt()}%", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StabilityRow(name: String, signal: String, stability: Double, confidence: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = card),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(name, fontWeight = FontWeight.SemiBold)
                Text("Signal: $signal", color = subtle, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("${(stability * 100).toInt()}% stable", fontWeight = FontWeight.Bold)
                Text("${(confidence * 100).toInt()}% conf", color = subtle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CenterMessage(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) {
                Text(subtitle, color = subtle, modifier = Modifier.padding(top = 6.dp))
            }
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction, modifier = Modifier.padding(top = 12.dp)) {
                    Text(actionLabel)
                }
            }
        }
    }
}

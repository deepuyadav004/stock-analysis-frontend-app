package com.genxsolutions.growwealth.feature.watchlist

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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val background = Color(0xFF000000)
private val cardBg = Color(0xFF162338)
private val primaryText = Color(0xFFF5F8FF)
private val secondaryText = Color(0xFFB7C4D8)
private val upColor = Color(0xFF1B8A5A)
private val downColor = Color(0xFFD64545)
private val neutralColor = Color(0xFF607D8B)

@Composable
fun WatchlistScreen(
    state: WatchlistUiState,
    onRemoveSector: (Int) -> Unit,
    onRemoveCompany: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = background) {
        when {
            state.sectorItems.isEmpty() && state.companyItems.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Your watchlist is empty. Add sectors from Home or companies from Companies tab.",
                        color = primaryText
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            "Watchlist",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }

                    if (state.companyItems.isNotEmpty()) {
                        item {
                            Text(
                                text = "Companies",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryText
                            )
                        }
                    }

                    items(state.companyItems) { item ->
                        val moveColor = when {
                            item.dayChangePct > 0.0 -> upColor
                            item.dayChangePct < 0.0 -> downColor
                            else -> neutralColor
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, moveColor.copy(alpha = 0.45f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(item.companyName, fontWeight = FontWeight.SemiBold, color = primaryText)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Ticker", color = secondaryText)
                                    Text("${item.ticker} • ${item.exchangeCode}", fontWeight = FontWeight.SemiBold, color = primaryText)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Latest close", color = secondaryText)
                                    Text("₹${String.format("%,.2f", item.latestClose)}", fontWeight = FontWeight.SemiBold, color = primaryText)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Daily move", color = secondaryText)
                                    Text(
                                        "${formatSignedPercent(item.dayChangePct)}%",
                                        fontWeight = FontWeight.SemiBold,
                                        color = moveColor
                                    )
                                }
                                Button(onClick = { onRemoveCompany(item.companyId) }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Remove")
                                }
                            }
                        }
                    }

                    if (state.sectorItems.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        item {
                            Text(
                                text = "Sectors",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryText
                            )
                        }
                    }

                    items(state.sectorItems) { item ->
                        val signalColor = when (item.signal.uppercase()) {
                            "UP" -> upColor
                            "DOWN" -> downColor
                            else -> neutralColor
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, signalColor.copy(alpha = 0.45f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(item.sectorName, fontWeight = FontWeight.SemiBold, color = primaryText)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Signal", color = secondaryText)
                                    Text(item.signal, fontWeight = FontWeight.SemiBold, color = signalColor)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Confidence", color = secondaryText)
                                    Text("${(item.confidence * 100).toInt()}%", fontWeight = FontWeight.SemiBold, color = primaryText)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Snapshot", color = secondaryText)
                                    Text(item.snapshotDate, fontWeight = FontWeight.SemiBold, color = primaryText)
                                }
                                Button(onClick = { onRemoveSector(item.sectorId) }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Remove")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatSignedPercent(value: Double): String {
    val formatted = String.format("%.2f", kotlin.math.abs(value))
    return if (value >= 0) "+$formatted" else "-$formatted"
}

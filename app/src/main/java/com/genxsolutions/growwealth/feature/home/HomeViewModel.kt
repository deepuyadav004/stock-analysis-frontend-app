package com.genxsolutions.growwealth.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.genxsolutions.growwealth.data.remote.HomeSummaryResponse
import com.genxsolutions.growwealth.data.remote.MarketMood
import com.genxsolutions.growwealth.data.remote.SectorSignal
import com.genxsolutions.growwealth.data.remote.SectorSignalsResponse
import com.genxsolutions.growwealth.data.remote.SnapshotLatestResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val snapshot: SnapshotLatestResponse? = null,
    val summary: HomeSummaryResponse? = null,
    val sectors: List<SectorSignal> = emptyList()
)

class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val hasExistingContent = _uiState.value.sectors.isNotEmpty()
            _uiState.value = if (hasExistingContent) {
                _uiState.value.copy(isRefreshing = true, errorMessage = null)
            } else {
                _uiState.value.copy(isLoading = true, errorMessage = null)
            }

            runCatching {
                val signals = repository.fetchSectorSignals(date = null)
                val fallbackSnapshot = SnapshotLatestResponse(
                    hasData = signals.hasData,
                    latestDate = signals.snapshotDate,
                    ageDays = signals.ageDays,
                    freshnessLabel = signals.freshnessLabel
                )
                val fallbackSummary = buildSummaryFromSignals(signals)

                // Tunnel can intermittently timeout for snapshot/summary endpoints.
                // We keep home functional using sectors/signals and only enrich if available.
                val snapshot = runCatching {
                    repository.fetchSnapshotLatest()
                }.getOrDefault(fallbackSnapshot)

                val summary = runCatching {
                    repository.fetchHomeSummary(snapshot.latestDate ?: signals.snapshotDate)
                }.getOrDefault(fallbackSummary)

                HomeUiState(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null,
                    snapshot = snapshot,
                    summary = summary,
                    sectors = signals.items
                )
            }.onSuccess { state ->
                _uiState.value = state
            }.onFailure { error ->
                if (hasExistingContent) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = error.message ?: "Unable to refresh data."
                    )
                } else {
                    _uiState.value = HomeUiState(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = error.message ?: "Unable to load home data."
                    )
                }
            }
        }
    }

    private fun buildSummaryFromSignals(signals: SectorSignalsResponse): HomeSummaryResponse {
        val up = signals.items.count { it.signal == "UP" }
        val down = signals.items.count { it.signal == "DOWN" }
        val neutral = signals.items.count { it.signal == "NEUTRAL" }
        val total = signals.items.size

        val avgConfidence = if (total > 0) {
            signals.items.map { it.confidence }.average()
        } else {
            0.0
        }

        val avgSentiment = if (total > 0) {
            signals.items.map { it.sentimentScore }.average()
        } else {
            0.0
        }

        return HomeSummaryResponse(
            snapshotDate = signals.snapshotDate,
            ageDays = signals.ageDays,
            freshnessLabel = signals.freshnessLabel,
            marketMood = MarketMood(
                upCount = up,
                downCount = down,
                neutralCount = neutral,
                totalSectors = total,
                avgConfidence = avgConfidence,
                avgSentimentScore = avgSentiment
            )
        )
    }

    class Factory(private val repository: HomeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}

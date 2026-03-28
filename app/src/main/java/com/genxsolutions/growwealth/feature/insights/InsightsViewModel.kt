package com.genxsolutions.growwealth.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.genxsolutions.growwealth.core.ErrorMapper
import com.genxsolutions.growwealth.data.remote.InsightsCompareResponse
import com.genxsolutions.growwealth.data.remote.InsightsStabilityResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InsightsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val compare7: InsightsCompareResponse? = null,
    val compare30: InsightsCompareResponse? = null,
    val stability30: InsightsStabilityResponse? = null
)

class InsightsViewModel(private val repository: InsightsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val previous = _uiState.value
            val hasContent = previous.compare7 != null || previous.compare30 != null || previous.stability30 != null
            _uiState.value = if (hasContent) {
                previous.copy(isRefreshing = true, errorMessage = null)
            } else {
                previous.copy(isLoading = true, errorMessage = null)
            }

            val compare7 = fetchWithRetry { repository.fetchCompare(days = 7) }
            val stability30 = fetchWithRetry { repository.fetchStability(days = 30) }

            val initialHasData = (compare7?.hasData == true) || (stability30?.hasData == true)
            _uiState.value = InsightsUiState(
                isLoading = false,
                isRefreshing = false,
                errorMessage = if (!initialHasData && !hasContent) {
                    ErrorMapper.toUserMessage(null, "Insights are temporarily unavailable. Pull to refresh.")
                } else {
                    null
                },
                compare7 = compare7 ?: previous.compare7,
                compare30 = previous.compare30,
                stability30 = stability30 ?: previous.stability30,
            )

            // Load 30-day comparison separately so one slow API does not block the full screen.
            val compare30 = fetchWithRetry { repository.fetchCompare(days = 30) }
            if (compare30 != null) {
                _uiState.value = _uiState.value.copy(compare30 = compare30, errorMessage = null)
            }
        }
    }

    private suspend fun <T> fetchWithRetry(block: suspend () -> T): T? {
        return runCatching { block() }
            .getOrElse {
                delay(1200)
                runCatching { block() }.getOrNull()
            }
    }

    class Factory(private val repository: InsightsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InsightsViewModel(repository) as T
        }
    }
}

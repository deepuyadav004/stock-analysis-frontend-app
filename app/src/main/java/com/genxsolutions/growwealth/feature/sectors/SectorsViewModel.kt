package com.genxsolutions.growwealth.feature.sectors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.genxsolutions.growwealth.core.ErrorMapper
import com.genxsolutions.growwealth.data.remote.SectorSignal
import com.genxsolutions.growwealth.data.remote.SnapshotLatestResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SectorFilter(
    val signals: Set<String> = emptySet(),
    val confidenceMin: Double = 0.0,
    val confidenceMax: Double = 1.0,
    val sort: String = "confidence_desc"
)

data class SectorsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val snapshot: SnapshotLatestResponse? = null,
    val items: List<SectorSignal> = emptyList(),
    val hasMorePages: Boolean = true,
    val filter: SectorFilter = SectorFilter(),
    val totalCount: Int = 0
)

class SectorsViewModel(
    private val repository: SectorsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SectorsUiState())
    val uiState: StateFlow<SectorsUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private val pageSize = 20

    init {
        loadInitial()
    }

    fun loadInitial() {
        viewModelScope.launch {
            val hasExistingContent = _uiState.value.items.isNotEmpty()
            currentPage = 0
            _uiState.value = if (hasExistingContent) {
                _uiState.value.copy(isRefreshing = true, errorMessage = null)
            } else {
                _uiState.value.copy(isLoading = true, errorMessage = null)
            }

            runCatching {
                val response = repository.fetchSectorSignals(
                    limit = pageSize,
                    offset = 0,
                    sort = _uiState.value.filter.sort,
                    signal = buildSignalParam(),
                    confidenceMin = _uiState.value.filter.confidenceMin,
                    confidenceMax = _uiState.value.filter.confidenceMax
                )

                val snapshot = SnapshotLatestResponse(
                    hasData = response.hasData,
                    latestDate = response.snapshotDate,
                    ageDays = response.ageDays,
                    freshnessLabel = response.freshnessLabel
                )

                val hasMorePages = (0 + pageSize) < (response.pagination.total)

                SectorsUiState(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    errorMessage = null,
                    snapshot = snapshot,
                    items = response.items,
                    hasMorePages = hasMorePages,
                    filter = _uiState.value.filter,
                    totalCount = response.pagination.total
                )
            }.onSuccess { state ->
                _uiState.value = state
            }.onFailure { error ->
                if (hasExistingContent) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = ErrorMapper.toUserMessage(error, "Unable to refresh sectors.")
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = ErrorMapper.toUserMessage(error, "Unable to load sectors.")
                    )
                }
            }
        }
    }

    fun refresh() {
        loadInitial()
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMorePages) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)

            val nextPage = currentPage + 1
            val offset = nextPage * pageSize

            runCatching {
                val response = repository.fetchSectorSignals(
                    limit = pageSize,
                    offset = offset,
                    sort = _uiState.value.filter.sort,
                    signal = buildSignalParam(),
                    confidenceMin = _uiState.value.filter.confidenceMin,
                    confidenceMax = _uiState.value.filter.confidenceMax
                )

                val hasMorePages = (offset + pageSize) < (response.pagination.total)
                val newItems = _uiState.value.items + response.items

                SectorsUiState(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    errorMessage = null,
                    snapshot = _uiState.value.snapshot,
                    items = newItems,
                    hasMorePages = hasMorePages,
                    filter = _uiState.value.filter,
                    totalCount = response.pagination.total
                )
            }.onSuccess { state ->
                currentPage = nextPage
                _uiState.value = state
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    errorMessage = ErrorMapper.toUserMessage(error, "Unable to load more sectors.")
                )
            }
        }
    }

    fun updateFilter(newFilter: SectorFilter) {
        _uiState.value = _uiState.value.copy(filter = newFilter)
        loadInitial()
    }

    fun toggleSignal(signal: String) {
        val currentSignals = _uiState.value.filter.signals.toMutableSet()
        if (currentSignals.contains(signal)) {
            currentSignals.remove(signal)
        } else {
            currentSignals.add(signal)
        }
        val newFilter = _uiState.value.filter.copy(signals = currentSignals)
        updateFilter(newFilter)
    }

    fun updateConfidenceRange(min: Double, max: Double) {
        val newFilter = _uiState.value.filter.copy(confidenceMin = min, confidenceMax = max)
        updateFilter(newFilter)
    }

    fun updateSort(sort: String) {
        val newFilter = _uiState.value.filter.copy(sort = sort)
        updateFilter(newFilter)
    }

    private fun buildSignalParam(): String? {
        return if (_uiState.value.filter.signals.isEmpty()) {
            null
        } else {
            _uiState.value.filter.signals.joinToString(",")
        }
    }

    class Factory(private val repository: SectorsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SectorsViewModel(repository) as T
        }
    }
}

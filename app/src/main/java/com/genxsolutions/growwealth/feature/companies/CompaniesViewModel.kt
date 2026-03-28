package com.genxsolutions.growwealth.feature.companies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.genxsolutions.growwealth.core.ErrorMapper
import com.genxsolutions.growwealth.data.remote.CompanyListItem
import com.genxsolutions.growwealth.data.remote.CompanyPerformanceResponse
import com.genxsolutions.growwealth.data.remote.CompanySummaryResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class CompanyFilter(
    val query: String = "",
    val signal: String? = null,
    val sort: String = "name_asc"
)

data class CompaniesUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val items: List<CompanyListItem> = emptyList(),
    val hasMore: Boolean = true,
    val totalCount: Int = 0,
    val filter: CompanyFilter = CompanyFilter(),
    val selectedCompanySummary: CompanySummaryResponse? = null,
    val selectedPerformance: CompanyPerformanceResponse? = null,
    val selectedRange: String = "1Y",
    val isDetailLoading: Boolean = false
)

class CompaniesViewModel(private val repository: CompaniesRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CompaniesUiState())
    val uiState: StateFlow<CompaniesUiState> = _uiState.asStateFlow()

    private val pageSize = 10
    private var searchJob: Job? = null
    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val hasContent = _uiState.value.items.isNotEmpty()
            _uiState.value = if (hasContent) {
                _uiState.value.copy(isRefreshing = true, errorMessage = null)
            } else {
                _uiState.value.copy(isLoading = true, errorMessage = null)
            }
            runCatching {
                repository.fetchCompanies(
                    query = _uiState.value.filter.query.ifBlank { null },
                    signal = _uiState.value.filter.signal,
                    limit = pageSize,
                    offset = 0,
                    sort = _uiState.value.filter.sort
                )
            }.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    errorMessage = null,
                    items = response.items,
                    hasMore = response.pagination.offset + response.pagination.count < response.pagination.total,
                    totalCount = response.pagination.total
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    errorMessage = ErrorMapper.toUserMessage(error, "Unable to load companies.")
                )
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            val nextOffset = _uiState.value.items.size

            runCatching {
                repository.fetchCompanies(
                    query = _uiState.value.filter.query.ifBlank { null },
                    signal = _uiState.value.filter.signal,
                    limit = pageSize,
                    offset = nextOffset,
                    sort = _uiState.value.filter.sort
                )
            }.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    errorMessage = null,
                    items = _uiState.value.items + response.items,
                    hasMore = response.pagination.offset + response.pagination.count < response.pagination.total,
                    totalCount = response.pagination.total
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    errorMessage = ErrorMapper.toUserMessage(error, "Unable to load more companies.")
                )
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(filter = _uiState.value.filter.copy(query = query))

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(450)
            refresh()
        }
    }

    fun applySearch() {
        refresh()
    }

    fun applyFilters(query: String, signal: String?) {
        searchJob?.cancel()
        _uiState.value = _uiState.value.copy(
            filter = _uiState.value.filter.copy(query = query, signal = signal)
        )
        refresh()
    }

    fun applyFilter(signal: String?) {
        _uiState.value = _uiState.value.copy(filter = _uiState.value.filter.copy(signal = signal))
        refresh()
    }

    fun openCompany(companyId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDetailLoading = true)
            val range = _uiState.value.selectedRange

            runCatching {
                val summary = repository.fetchSummary(companyId)
                val performance = repository.fetchPerformance(companyId, range)
                summary to performance
            }.onSuccess { pair ->
                _uiState.value = _uiState.value.copy(
                    isDetailLoading = false,
                    selectedCompanySummary = pair.first,
                    selectedPerformance = pair.second,
                    errorMessage = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isDetailLoading = false,
                    errorMessage = ErrorMapper.toUserMessage(error, "Unable to load company detail.")
                )
            }
        }
    }

    fun selectRange(range: String) {
        val summary = _uiState.value.selectedCompanySummary ?: return
        _uiState.value = _uiState.value.copy(selectedRange = range, isDetailLoading = true)

        viewModelScope.launch {
            runCatching { repository.fetchPerformance(summary.companyId, range) }
                .onSuccess { perf ->
                    _uiState.value = _uiState.value.copy(
                        selectedPerformance = perf,
                        isDetailLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isDetailLoading = false,
                        errorMessage = ErrorMapper.toUserMessage(error, "Unable to load selected range.")
                    )
                }
        }
    }

    fun closeDetail() {
        _uiState.value = _uiState.value.copy(
            selectedCompanySummary = null,
            selectedPerformance = null,
            isDetailLoading = false
        )
    }

    class Factory(private val repository: CompaniesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CompaniesViewModel(repository) as T
        }
    }
}

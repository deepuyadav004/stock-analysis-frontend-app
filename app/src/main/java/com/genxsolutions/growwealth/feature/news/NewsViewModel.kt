package com.genxsolutions.growwealth.feature.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.genxsolutions.growwealth.core.ErrorMapper
import com.genxsolutions.growwealth.feature.news.domain.NewsItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NewsUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val items: List<NewsItem> = emptyList(),
    val totalCount: Int = 0,
    val hasMore: Boolean = true
)

class NewsViewModel(private val repository: NewsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private val pageSize = 20

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            runCatching {
                repository.fetchNews(source = null, limit = pageSize, offset = 0)
            }.onSuccess { page ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    errorMessage = null,
                    items = page.items,
                    totalCount = page.totalCount,
                    hasMore = page.hasMore
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    errorMessage = ErrorMapper.toUserMessage(error, "Unable to load latest news.")
                )
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoading || _uiState.value.isLoadingMore || !_uiState.value.hasMore) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            val nextOffset = _uiState.value.items.size

            runCatching {
                repository.fetchNews(source = null, limit = pageSize, offset = nextOffset)
            }.onSuccess { page ->
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    errorMessage = null,
                    items = _uiState.value.items + page.items,
                    totalCount = page.totalCount,
                    hasMore = page.hasMore
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    errorMessage = ErrorMapper.toUserMessage(error, "Unable to load more news.")
                )
            }
        }
    }

    class Factory(private val repository: NewsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NewsViewModel(repository) as T
        }
    }
}

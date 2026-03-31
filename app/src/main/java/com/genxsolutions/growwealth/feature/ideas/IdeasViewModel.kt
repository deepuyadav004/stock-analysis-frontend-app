package com.genxsolutions.growwealth.feature.ideas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.genxsolutions.growwealth.core.ErrorMapper
import com.genxsolutions.growwealth.feature.ideas.domain.CallType
import com.genxsolutions.growwealth.feature.ideas.domain.Idea
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IdeasUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val items: List<Idea> = emptyList(),
    val totalCount: Int = 0,
    val hasMore: Boolean = true,
    val selectedCallType: CallType? = null,
    val selectedIdea: Idea? = null,
    val isDetailLoading: Boolean = false,
    val detailErrorMessage: String? = null
)

class IdeasViewModel(private val repository: IdeasRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(IdeasUiState())
    val uiState: StateFlow<IdeasUiState> = _uiState.asStateFlow()

    private val pageSize = 20

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
                repository.fetchIdeas(
                    callType = _uiState.value.selectedCallType,
                    limit = pageSize,
                    offset = 0
                )
            }.onSuccess { page ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    errorMessage = null,
                    items = page.items,
                    totalCount = page.totalCount,
                    hasMore = page.hasMore
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    errorMessage = ErrorMapper.toUserMessage(error, "Unable to load stock ideas.")
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
                repository.fetchIdeas(
                    callType = _uiState.value.selectedCallType,
                    limit = pageSize,
                    offset = nextOffset
                )
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
                    errorMessage = ErrorMapper.toUserMessage(error, "Unable to load more ideas.")
                )
            }
        }
    }

    fun selectCallType(callType: CallType?) {
        _uiState.value = _uiState.value.copy(selectedCallType = callType)
        refresh()
    }

    fun openIdea(ideaId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDetailLoading = true, detailErrorMessage = null)

            runCatching { repository.fetchIdeaDetail(ideaId) }
                .onSuccess { idea ->
                    _uiState.value = _uiState.value.copy(
                        isDetailLoading = false,
                        selectedIdea = idea,
                        detailErrorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isDetailLoading = false,
                        detailErrorMessage = ErrorMapper.toUserMessage(error, "Unable to load idea detail.")
                    )
                }
        }
    }

    fun closeDetail() {
        _uiState.value = _uiState.value.copy(selectedIdea = null, detailErrorMessage = null)
    }

    class Factory(private val repository: IdeasRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return IdeasViewModel(repository) as T
        }
    }
}
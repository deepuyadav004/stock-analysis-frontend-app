package com.genxsolutions.growwealth.feature.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.genxsolutions.growwealth.core.ErrorMapper
import com.genxsolutions.growwealth.data.local.WatchlistCompanyEntity
import com.genxsolutions.growwealth.data.local.WatchlistSectorEntity
import com.genxsolutions.growwealth.data.remote.CompanyListItem
import com.genxsolutions.growwealth.data.remote.SectorSignal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WatchlistUiState(
    val sectorItems: List<WatchlistSectorEntity> = emptyList(),
    val companyItems: List<WatchlistCompanyEntity> = emptyList(),
    val errorMessage: String? = null
)

class WatchlistViewModel(private val repository: WatchlistRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    val watchlistSectorIds: StateFlow<Set<Int>> = repository.observeSectorIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val watchlistCompanyIds: StateFlow<Set<Int>> = repository.observeCompanyIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    init {
        observeWatchlist()
    }

    private fun observeWatchlist() {
        viewModelScope.launch {
            combine(
                repository.observeSectorItems(),
                repository.observeCompanyItems()
            ) { sectors, companies ->
                sectors to companies
            }.collect { (sectors, companies) ->
                _uiState.value = _uiState.value.copy(
                    sectorItems = sectors,
                    companyItems = companies,
                    errorMessage = null
                )
            }
        }
    }

    fun toggleSector(sector: SectorSignal) {
        viewModelScope.launch {
            runCatching { repository.toggle(sector) }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = ErrorMapper.toUserMessage(error, "Unable to update watchlist.")
                    )
                }
        }
    }

    fun removeSector(sectorId: Int) {
        viewModelScope.launch {
            runCatching { repository.remove(sectorId) }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = ErrorMapper.toUserMessage(error, "Unable to remove from watchlist.")
                    )
                }
        }
    }

    fun toggleCompany(company: CompanyListItem) {
        viewModelScope.launch {
            runCatching { repository.toggle(company) }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = ErrorMapper.toUserMessage(error, "Unable to update watchlist.")
                    )
                }
        }
    }

    fun removeCompany(companyId: Int) {
        viewModelScope.launch {
            runCatching { repository.removeCompany(companyId) }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = ErrorMapper.toUserMessage(error, "Unable to remove from watchlist.")
                    )
                }
        }
    }

    class Factory(private val repository: WatchlistRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WatchlistViewModel(repository) as T
        }
    }
}

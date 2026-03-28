package com.genxsolutions.growwealth.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.genxsolutions.growwealth.data.local.LocalDatabaseModule
import com.genxsolutions.growwealth.data.remote.NetworkModule
import com.genxsolutions.growwealth.feature.companies.CompaniesRepository
import com.genxsolutions.growwealth.feature.companies.CompaniesScreen
import com.genxsolutions.growwealth.feature.companies.CompaniesViewModel
import com.genxsolutions.growwealth.feature.home.HomeRepository
import com.genxsolutions.growwealth.feature.home.HomeScreen
import com.genxsolutions.growwealth.feature.home.HomeViewModel
import com.genxsolutions.growwealth.feature.watchlist.WatchlistRepository
import com.genxsolutions.growwealth.feature.watchlist.WatchlistScreen
import com.genxsolutions.growwealth.feature.watchlist.WatchlistViewModel

enum class AppTab(val label: String) {
    Home("Home"),
    Companies("Companies"),
    Watchlist("Watchlist")
}

@Composable
fun GrowWealthApp() {
    var currentTab by remember { mutableStateOf(AppTab.Home) }
    val context = LocalContext.current
    val watchlistViewModel: WatchlistViewModel = viewModel(
        factory = WatchlistViewModel.Factory(
            WatchlistRepository(LocalDatabaseModule.database(context).watchlistSectorDao())
        )
    )
    val watchlistState by watchlistViewModel.uiState.collectAsState()
    val watchlistSectorIds by watchlistViewModel.watchlistSectorIds.collectAsState()

    MaterialTheme {
        Scaffold(
            containerColor = Color(0xFFF4F6F8),
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFFFFFFFF),
                    tonalElevation = 8.dp
                ) {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = {
                                when (tab) {
                                    AppTab.Home -> Icon(Icons.Default.Home, contentDescription = tab.label)
                                    AppTab.Companies -> Icon(Icons.Default.Business, contentDescription = tab.label)
                                    AppTab.Watchlist -> Icon(Icons.Default.Star, contentDescription = tab.label)
                                }
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            when (currentTab) {
                AppTab.Home -> {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModel.Factory(HomeRepository(NetworkModule.api))
                    )
                    val state by homeViewModel.uiState.collectAsState()
                    HomeScreen(
                        state = state,
                        onRetry = homeViewModel::refresh,
                        onSectorClick = homeViewModel::openSectorDetail,
                        onToggleWatchlist = watchlistViewModel::toggleSector,
                        watchlistSectorIds = watchlistSectorIds,
                        onCloseDetail = homeViewModel::closeSectorDetail,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                AppTab.Companies -> {
                    val companiesViewModel: CompaniesViewModel = viewModel(
                        factory = CompaniesViewModel.Factory(CompaniesRepository(NetworkModule.api))
                    )
                    val state by companiesViewModel.uiState.collectAsState()
                    CompaniesScreen(
                        state = state,
                        onRefresh = companiesViewModel::refresh,
                        onLoadMore = companiesViewModel::loadMore,
                        onQueryChange = companiesViewModel::updateQuery,
                        onApplySearch = companiesViewModel::applySearch,
                        onSignalFilter = companiesViewModel::applyFilter,
                        onCompanyClick = companiesViewModel::openCompany,
                        onSelectRange = companiesViewModel::selectRange,
                        onCloseDetail = companiesViewModel::closeDetail,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                AppTab.Watchlist -> {
                    WatchlistScreen(
                        state = watchlistState,
                        onRemove = watchlistViewModel::removeSector,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

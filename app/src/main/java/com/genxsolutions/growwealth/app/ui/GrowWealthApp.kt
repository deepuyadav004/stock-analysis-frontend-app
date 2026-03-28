package com.genxsolutions.growwealth.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.List
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.genxsolutions.growwealth.data.remote.NetworkModule
import com.genxsolutions.growwealth.feature.home.HomeRepository
import com.genxsolutions.growwealth.feature.home.HomeScreen
import com.genxsolutions.growwealth.feature.home.HomeViewModel
import com.genxsolutions.growwealth.feature.sectors.SectorsRepository
import com.genxsolutions.growwealth.feature.sectors.SectorsScreen
import com.genxsolutions.growwealth.feature.sectors.SectorsViewModel

enum class AppTab(val label: String) {
    Home("Home"),
    Sectors("Sectors"),
    Insights("Insights"),
    Watchlist("Watchlist")
}

@Composable
fun GrowWealthApp() {
    var currentTab by remember { mutableStateOf(AppTab.Home) }

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
                                    AppTab.Sectors -> Icon(Icons.Default.List, contentDescription = tab.label)
                                    AppTab.Insights -> Icon(Icons.Default.Insights, contentDescription = tab.label)
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
                        onCloseDetail = homeViewModel::closeSectorDetail,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                AppTab.Sectors -> {
                    val sectorsViewModel: SectorsViewModel = viewModel(
                        factory = SectorsViewModel.Factory(SectorsRepository(NetworkModule.api))
                    )
                    val state by sectorsViewModel.uiState.collectAsState()
                    SectorsScreen(
                        state = state,
                        onRefresh = sectorsViewModel::refresh,
                        onLoadMore = sectorsViewModel::loadMore,
                        onFilterChange = sectorsViewModel::updateFilter,
                        onSectorClick = { /* TODO: navigate to sector detail or use shared detail modal */ },
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                AppTab.Insights,
                AppTab.Watchlist -> {
                    PlaceholderScreen(
                        title = currentTab.label,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    Text(
        text = "$title screen will be implemented in next chunks.",
        modifier = modifier.padding(24.dp)
    )
}

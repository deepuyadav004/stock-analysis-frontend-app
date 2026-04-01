package com.genxsolutions.growwealth.app.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.genxsolutions.growwealth.data.local.LocalDatabaseModule
import com.genxsolutions.growwealth.data.remote.NetworkModule
import com.genxsolutions.growwealth.feature.companies.CompaniesRepository
import com.genxsolutions.growwealth.feature.companies.CompaniesScreen
import com.genxsolutions.growwealth.feature.companies.CompaniesViewModel
import com.genxsolutions.growwealth.feature.ideas.domain.CallType
import com.genxsolutions.growwealth.feature.home.HomeRepository
import com.genxsolutions.growwealth.feature.home.HomeScreen
import com.genxsolutions.growwealth.feature.home.HomeViewModel
import com.genxsolutions.growwealth.feature.ideas.IdeasRepository
import com.genxsolutions.growwealth.feature.ideas.IdeasScreen
import com.genxsolutions.growwealth.feature.ideas.IdeasViewModel
import com.genxsolutions.growwealth.feature.news.NewsRepository
import com.genxsolutions.growwealth.feature.news.NewsScreen
import com.genxsolutions.growwealth.feature.news.NewsViewModel
import com.genxsolutions.growwealth.feature.news.NewsWebViewScreen
import com.genxsolutions.growwealth.feature.watchlist.WatchlistRepository
import com.genxsolutions.growwealth.feature.watchlist.WatchlistScreen
import com.genxsolutions.growwealth.feature.watchlist.WatchlistViewModel

enum class AppTab(val label: String) {
    Home("Home"),
    Companies("Companies"),
    News("News"),
    Ideas("Ideas"),
    Watchlist("Watchlist")
}

@Composable
fun GrowWealthApp() {
    var currentTab by remember { mutableStateOf(AppTab.Home) }
    var newsArticleUrl by remember { mutableStateOf<String?>(null) }
    var isBottomBarVisible by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val bottomBarScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput) {
                    when {
                        available.y < -1f -> isBottomBarVisible = false
                        available.y > 1f -> isBottomBarVisible = true
                    }
                }
                return Offset.Zero
            }
        }
    }
    val watchlistViewModel: WatchlistViewModel = viewModel(
        factory = WatchlistViewModel.Factory(
            WatchlistRepository(
                LocalDatabaseModule.database(context).watchlistSectorDao(),
                LocalDatabaseModule.database(context).watchlistCompanyDao()
            )
        )
    )
    val watchlistState by watchlistViewModel.uiState.collectAsState()
    val watchlistSectorIds by watchlistViewModel.watchlistSectorIds.collectAsState()
    val watchlistCompanyIds by watchlistViewModel.watchlistCompanyIds.collectAsState()

    MaterialTheme {
        Scaffold(
            modifier = Modifier.nestedScroll(bottomBarScrollConnection),
            containerColor = Color.Black,
            bottomBar = {
                AnimatedVisibility(
                    visible = isBottomBarVisible,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    NavigationBar(
                        containerColor = Color.Black.copy(alpha = 0.5f),
                        tonalElevation = 8.dp
                    ) {
                        AppTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    unselectedIconColor = Color.White.copy(alpha = 0.78f),
                                    selectedTextColor = Color.White,
                                    unselectedTextColor = Color.White.copy(alpha = 0.78f),
                                    indicatorColor = Color(0xFF1A1A1A)
                                ),
                                icon = {
                                    when (tab) {
                                        AppTab.Home -> Icon(Icons.Default.Home, contentDescription = tab.label)
                                        AppTab.Companies -> Icon(Icons.Default.Business, contentDescription = tab.label)
                                        AppTab.News -> Icon(Icons.Default.Article, contentDescription = tab.label)
                                        AppTab.Ideas -> Icon(Icons.Default.TrendingUp, contentDescription = tab.label)
                                        AppTab.Watchlist -> Icon(Icons.Default.Star, contentDescription = tab.label)
                                    }
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
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
                        onApplyFilters = companiesViewModel::applyFilters,
                        onCompanyClick = companiesViewModel::openCompany,
                        onToggleWatchlist = watchlistViewModel::toggleCompany,
                        watchlistCompanyIds = watchlistCompanyIds,
                        onSelectRange = companiesViewModel::selectRange,
                        onCloseDetail = companiesViewModel::closeDetail,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                AppTab.Watchlist -> {
                    WatchlistScreen(
                        state = watchlistState,
                        onRemoveSector = watchlistViewModel::removeSector,
                        onRemoveCompany = watchlistViewModel::removeCompany,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                AppTab.News -> {
                    val newsViewModel: NewsViewModel = viewModel(
                        factory = NewsViewModel.Factory(NewsRepository(NetworkModule.api))
                    )
                    val state by newsViewModel.uiState.collectAsState()
                    if (newsArticleUrl == null) {
                        NewsScreen(
                            state = state,
                            onRefresh = newsViewModel::refresh,
                            onLoadMore = newsViewModel::loadMore,
                            onArticleClick = { articleUrl -> newsArticleUrl = articleUrl },
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        NewsWebViewScreen(
                            url = newsArticleUrl.orEmpty(),
                            onClose = { newsArticleUrl = null },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }

                AppTab.Ideas -> {
                    val ideasViewModel: IdeasViewModel = viewModel(
                        factory = IdeasViewModel.Factory(IdeasRepository(NetworkModule.api))
                    )
                    val state by ideasViewModel.uiState.collectAsState()
                    IdeasScreen(
                        state = state,
                        onRefresh = ideasViewModel::refresh,
                        onLoadMore = ideasViewModel::loadMore,
                        onSelectCallType = ideasViewModel::selectCallType,
                        onIdeaClick = ideasViewModel::openIdea,
                        onSaveToWatchlist = { idea ->
                            // Use a stable negative id to avoid collision with API company ids.
                            val hash = idea.ticker.uppercase().hashCode()
                            val stableId = if (hash == Int.MIN_VALUE) Int.MAX_VALUE else kotlin.math.abs(hash)
                            watchlistViewModel.toggleCompany(
                                com.genxsolutions.growwealth.data.remote.CompanyListItem(
                                    companyId = -(stableId + 1),
                                    companyName = idea.companyName,
                                    ticker = idea.ticker,
                                    exchangeCode = "NSE",
                                    latestDate = idea.recommendationDate,
                                    latestClose = idea.targetPrice ?: 0.0,
                                    dayChangePct = 0.0,
                                    signal = when (idea.callType) {
                                        CallType.BUY -> "BUY"
                                        CallType.SELL -> "SELL"
                                        CallType.HOLD -> "HOLD"
                                        CallType.UNKNOWN -> "UNKNOWN"
                                    }
                                )
                            )
                            Toast.makeText(context, "Saved to Watchlist", Toast.LENGTH_SHORT).show()
                        },
                        onCloseDetail = ideasViewModel::closeDetail,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

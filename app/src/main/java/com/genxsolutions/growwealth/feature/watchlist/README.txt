Module: Watchlist Feature

1. Purpose
Provide local-first watchlist persistence so users can save sectors and companies for later review.

2. Problem Solved
Users can add/remove sectors from Home and companies from Companies tab, then view both lists in a dedicated Watchlist tab.

3. File Responsibilities
- WatchlistRepository.kt: Local persistence operations for sector and company watchlist DAOs.
- WatchlistViewModel.kt: Exposes watched sector/company IDs and combined watchlist UI state.
- WatchlistScreen.kt: Renders company + sector sections with remove actions.

4. Step-by-Step Flow
1. User taps watchlist action on a sector from Home.
2. User taps watchlist action on a company from Companies.
3. ViewModel toggles sector/company rows in Room database.
4. Watchlist tab observes DB changes and updates instantly.
5. User can remove saved items directly from Watchlist tab.

5. Interactions
- Uses data/local Room database layer.
- Shared by Home, Companies, and Watchlist tabs through app shell.

6. Assumptions
- Watchlist is local-only and stores both sectors and companies.
- Snapshot values are latest known values when user adds/toggles.

7. Future Improvements
- Sync latest values on app refresh.
- Add notes/alerts per watchlist item.
- Support backend watchlist sync in later chunk.

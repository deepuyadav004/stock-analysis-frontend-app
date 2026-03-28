Module: Watchlist Feature

1. Purpose
Provide local-first watchlist persistence so users can save sectors for later review.

2. Problem Solved
Users can add/remove sectors from Home and view a dedicated Watchlist snapshot tab.

3. File Responsibilities
- WatchlistRepository.kt: Local persistence operations via Room DAO.
- WatchlistViewModel.kt: Exposes watched IDs and watchlist items as UI state.
- WatchlistScreen.kt: Renders watchlist list and remove actions.

4. Step-by-Step Flow
1. User taps watchlist action on a sector from Home.
2. ViewModel toggles sector in Room database.
3. Watchlist tab observes DB changes and updates instantly.
4. User can remove saved sectors directly from Watchlist tab.

5. Interactions
- Uses data/local Room database layer.
- Shared by Home and Watchlist tabs through app shell.

6. Assumptions
- Watchlist stores sectors (not individual stocks) for MVP.
- Snapshot values are latest known values when user adds/toggles.

7. Future Improvements
- Sync latest values on app refresh.
- Add notes/alerts per watchlist item.
- Support backend watchlist sync in later chunk.

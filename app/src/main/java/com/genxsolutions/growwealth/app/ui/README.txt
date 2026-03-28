Module: App UI Shell

1. Purpose
Host top-level app scaffold and bottom navigation structure.

2. Problem Solved
Defines stable navigation shell so feature screens can be delivered incrementally.

3. File Responsibilities
- GrowWealthApp.kt: Root composable scaffold, top bar, bottom tabs, and screen routing placeholders.

4. Step-by-Step Flow
1. App launches MainActivity.
2. GrowWealthApp renders scaffold.
3. Shared WatchlistViewModel initializes and observes local Room watchlist state.
4. User switches tabs.
5. Home tab initializes Home ViewModel and renders Home feature.
6. Sectors tab initializes Sectors ViewModel and supports filters/pagination.
7. Insights tab initializes Insights ViewModel and renders 7/30-day compare + stability widgets.
8. Watchlist tab renders saved sectors from local Room storage.

5. Interactions
- Uses feature/home module for Home tab.
- Uses feature/sectors module for Sectors tab.
- Uses feature/insights module for Insights tab.
- Uses feature/watchlist + data/local modules for watchlist persistence and tab rendering.

6. Assumptions
- Chunk-based rollout keeps non-home tabs as placeholders initially.

7. Future Improvements
- Replace tab switch with Navigation Compose graph.
- Add saved-state support for selected tab.
- Integrate app-wide design system theme.
- Route sector taps to a shared detail destination.
- Add confirmation/snackbar feedback on watchlist add/remove actions.

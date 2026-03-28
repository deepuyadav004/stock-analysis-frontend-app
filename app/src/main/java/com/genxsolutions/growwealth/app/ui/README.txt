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
6. Companies tab initializes Companies ViewModel and supports search/filter/pagination and detail ranges.
7. Watchlist tab renders saved sectors from local Room storage.

5. Interactions
- Uses feature/home module for Home tab.
- Uses feature/companies module for Companies tab.
- Uses feature/watchlist + data/local modules for watchlist persistence and tab rendering.

6. Assumptions
- Bottom navigation is intentionally kept to three sections for focused flow.

7. Future Improvements
- Replace tab switch with Navigation Compose graph.
- Add saved-state support for selected tab.
- Integrate app-wide design system theme.
- Add deep links from Home sectors to related companies.
- Add confirmation/snackbar feedback on watchlist add/remove actions.

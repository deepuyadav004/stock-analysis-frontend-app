Module: Companies Feature

1. Purpose
Provide a company-centric browse and drilldown experience with paginated discovery and multi-range performance review.

2. Problem Solved
Replaces sector-heavy exploration with direct company list access, making stock lookup and performance checks faster for users.

3. File Responsibilities
- CompaniesRepository.kt: Calls companies list, summary, and performance backend endpoints.
- CompaniesViewModel.kt: Manages list pagination, search/filter state, and selected company detail/range loading.
- CompaniesScreen.kt: Renders list UI, search/filter controls, company watchlist toggles, and company detail overlay with performance chart.

4. Step-by-Step Flow
1. Companies tab opens and ViewModel loads first list page.
2. User searches and/or applies signal filters.
3. User can toggle company watchlist directly from list cards.
4. List refreshes and paginates while scrolling.
5. User taps a company card.
6. App fetches summary + default range performance.
7. User switches ranges (1W, 1M, 1Y, 3Y, 5Y, 10Y) to compare returns.

5. Interactions
- Uses data/remote API contracts via GrowWealthApi.
- Routed from app/ui shell as a top-level tab.
- Shares common error mapping through core/ErrorMapper.

6. Assumptions
- Company IDs are sourced from list endpoint and reused for detail calls.
- Range values are restricted to backend-supported windows.

7. Future Improvements
- Add candlestick chart and selectable data points.
- Add company-level watchlist support.
- Add sort options in UI (name, move, volume) with persistent preference.

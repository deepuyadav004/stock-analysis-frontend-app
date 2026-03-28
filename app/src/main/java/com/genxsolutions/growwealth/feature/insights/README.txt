Module: Insights Feature

1. Purpose
Show historical context for sector signals using 7-day and 30-day comparisons plus stability trends.

2. Problem Solved
Provides non-real-time market understanding in a concise way: what changed vs prior window and which sectors are stable/unstable.

3. File Responsibilities
- InsightsRepository.kt: Calls insights compare and stability APIs.
- InsightsViewModel.kt: Coordinates loading/refresh and screen state.
- InsightsScreen.kt: Renders comparison cards, stability widgets, and empty/error states.

4. Step-by-Step Flow
1. Insights tab opens and ViewModel requests 7-day compare, 30-day compare, and 30-day stability.
2. UI renders comparison cards and stability summary.
3. Pull-to-refresh triggers a fresh fetch for all three payloads.
4. If historical data is insufficient, an empty-state message is shown.

5. Interactions
- Uses data/remote GrowWealthApi and ApiModels.
- Wired from app/ui GrowWealthApp tab routing.

6. Assumptions
- Backend endpoints /v1/insights/sector-compare and /v1/insights/signal-stability are available.
- days supports 7 and 30 for compare, and 30 for stability.

7. Future Improvements
- Add lightweight sparkline charts for sentiment and confidence trends.
- Add per-sector drilldown from stability list to sector detail.
- Cache last insights payload for offline view.

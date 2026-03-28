Module: Home Feature

1. Purpose
Render the snapshot-based Home screen for NSE sector insights.

2. Problem Solved
Shows freshness, market mood, and top sector signals using non-real-time backend data.

3. File Responsibilities
- HomeViewModel.kt: Orchestrates Home data loading and UI state.
- HomeRepository.kt: Fetches Home APIs.
- HomeScreen.kt: Composable UI with loading/error/empty/data states.

4. Step-by-Step Flow
1. ViewModel starts refresh.
2. Repository calls snapshot/latest, home/summary, sectors/signals.
3. UI state updates.
4. Composable renders cards/list or fallback states.

5. Interactions
- Uses data/remote API and models.
- Hosted by app/ui GrowWealthApp scaffold.

6. Assumptions
- Backend base URL is reachable from emulator.
- Response fields follow Chunk 1 contracts.

7. Future Improvements
- Add pull-to-refresh indicator.
- Add sector row navigation to detail screen (Chunk 2).
- Add unit tests for ViewModel state transitions.

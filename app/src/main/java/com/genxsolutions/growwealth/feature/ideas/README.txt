Module: Ideas Feature (Model Layer)

1. Module Purpose
Provide a complete Ideas feature slice for the app: model mapping, data access, state management, and first screen rendering.

2. Problem Solved
Introduces a normalized model contract and connects it to Retrofit, repository, and ViewModel so users can browse stock ideas from multiple sources.

3. File Responsibilities
- data/dto/IdeaDto.kt: Network DTOs for list and detail responses.
- data/mapper/IdeaMapper.kt: Conversion helpers from DTO to domain model.
- domain/Idea.kt: Domain entity used by repository/viewmodel layers.
- domain/CallType.kt: Recommendation call enum (BUY, SELL, HOLD, UNKNOWN).
- domain/Horizon.kt: Horizon enum used for UI filtering.
- domain/Source.kt: Source enum for Moneycontrol, Kotak Neo, Lemonn.
- IdeasRepository.kt: Calls API list/detail endpoints and converts DTOs to domain entities.
- IdeasViewModel.kt: Handles loading, pagination, filters, and detail state for UI.
- IdeasScreen.kt: Renders call-type chips, idea cards, automatic pagination, scroll-hide header, rich detail bottom sheet, and Save to Watchlist action.

4. Step-by-Step Flow
1. IdeasScreen requests data through IdeasViewModel.
2. IdeasViewModel calls IdeasRepository with selected call-type filter.
3. Repository invokes GrowWealthApi list/detail endpoints.
4. DTO models are converted to domain Idea objects via mapper extensions.
5. ViewModel publishes state updates for loading, list, auto-pagination, and detail panel.
6. Ideas header section auto-hides while scrolling down and reappears on upward scroll with eased slide/fade transitions and threshold-based smoothing (also always visible near top).
7. From idea detail bottom sheet, tapping Save to Watchlist invokes app-level callback and persists an item in watchlist companies.

5. Interactions with Other Modules
- data/remote/GrowWealthApi.kt now exposes `/v1/ideas/list` and `/v1/ideas/{ideaId}`.
- app/ui/GrowWealthApp.kt wires the Ideas tab and lifecycle-aware ViewModel.
- app/ui/GrowWealthApp.kt maps Idea selections to watchlist company entries and shows save confirmation toast.
- Ideas module shares ErrorMapper behavior with other feature modules.

6. Assumptions
- Backend sends call_type values as BUY/SELL/HOLD (nullable allowed).
- Backend currently does not provide a dedicated horizon field; mapper defaults Horizon.UNKNOWN.
- recommendation_date and created_at are currently kept as strings.
- List pagination contract follows `limit/offset/total/count`.

7. Future Improvements
- Parse recommendation_date into strong date types for sort/formatting.
- Add an explicit horizon field when backend starts sending it.
- Add mapper unit tests covering unknown/invalid enum values.
- Replace detail dialog with a richer bottom sheet when full Ideas UX polish starts.

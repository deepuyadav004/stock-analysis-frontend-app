Module: News Feature

1. Module Purpose
Render latest headline links in app and redirect users to original source sites.

2. Problem Solved
Adds a dedicated News bottom tab that shows source + headline + published time from backend news API.

3. File Responsibilities
- NewsRepository.kt: Fetches paginated news list from API.
- NewsViewModel.kt: Manages loading, load-more, error, and UI state.
- NewsScreen.kt: Displays list UI and handles article click callback.
- data/dto/NewsDto.kt: DTO models for news list API response.
- data/mapper/NewsMapper.kt: Maps DTOs to domain models.
- domain/NewsItem.kt: Domain model for UI rendering.

4. Step-by-Step Flow
1. News tab opens NewsScreen.
2. ViewModel requests first page from repository.
3. Screen shows SkeletonListLoader while loading.
4. List rows render source, headline, published date.
5. Row tap triggers callback that opens original article URL in browser.
6. Scrolling near end triggers loadMore for pagination.

5. Interactions with Other Modules
- data/remote/GrowWealthApi.kt for backend endpoint call.
- app/ui/GrowWealthApp.kt for tab wiring and browser redirection.
- core/SkeletonListLoader.kt for loading state.

6. Assumptions
- Backend endpoint /v1/news/list is deployed.
- API item field article_url is a valid absolute URL.

7. Future Improvements
- Add source filters and pull-to-refresh.
- Add richer relative-time formatting for published_at.

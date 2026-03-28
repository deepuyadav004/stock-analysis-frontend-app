Module: Sectors Feature

1. Purpose
Provide a full-featured Sectors explorer with filtering, sorting, and infinite-scroll pagination.

2. Problem Solved
Users can browse all sectors, not just the top signals shown on Home. Filtering by signal (UP/DOWN/NEUTRAL) and confidence range enables targeted discovery.

3. File Responsibilities
- SectorsRepository.kt: Fetches signals API with filters/sort/pagination params.
- SectorsViewModel.kt: Manages filter state, pagination offset, and list loading.
- SectorsScreen.kt: Composable UI with filter dialog, list, and infinite-scroll trigger.

4. Step-by-Step Flow
1. Sectors tab initializes SectorsViewModel.
2. ViewModel loads first page (offset=0, limit=20) of signals.
3. User opens filter dialog → adjusts signal/confidence/sort.
4. ViewModel resets to page 1 and reloads with new filters.
5. User scrolls near end of list → ViewModel auto-loads next page (offset += 20).
6. List grows incrementally with new items.

5. Interactions
- Uses data/remote GrowWealthApi and SectorsRepository.
- Hosted by app/ui GrowWealthApp scaffold.
- Does NOT yet open detail modal (TODO: wire to Home detail or create shared modal).

6. Assumptions
- Backend endpoint /v1/sectors/signals supports signal, confidence_min, confidence_max filters.
- Pagination uses limit/offset pattern.
- Sort options match backend allowed values.

7. Future Improvements
- Wire sector row click to detail modal (currently no-op).
- Add local caching for filter results.
- Add saved filter presets ("All UP", "High Confidence", etc).
- Add infinite-scroll loader animation.
- Add empty state improvements based on active filters.

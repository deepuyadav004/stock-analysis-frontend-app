Module: Core Utilities

1. Purpose
Provide app-wide shared utilities used by multiple features.

2. Problem Solved
Centralizes cross-cutting logic so feature modules stay focused on business/UI behavior.

3. File Responsibilities
- ErrorMapper.kt: Converts technical exceptions (timeout/network/http) into user-friendly messages.
- SkeletonListLoader.kt: Reusable animated skeleton list placeholder used by feature screens during initial loading.

4. Step-by-Step Flow
1. Feature ViewModel catches an exception.
2. ViewModel calls ErrorMapper.toUserMessage(error, fallback).
3. UI receives standardized, user-readable error text.

5. Interactions
- ErrorMapper is used by Home, Sectors, Insights, and Watchlist ViewModels.
- SkeletonListLoader is used by Home, Companies, and Ideas UI screens.

6. Assumptions
- Network and backend errors can be mapped by HTTP code or exception type.

7. Future Improvements
- Add analytics error categories for monitoring.
- Add localization support for user messages.
- Add theme-aware skeleton colors (light/dark) through centralized UI tokens.

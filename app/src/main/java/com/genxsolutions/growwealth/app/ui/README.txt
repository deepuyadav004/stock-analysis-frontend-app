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
3. User switches tabs.
4. Home tab initializes Home ViewModel and renders Home feature.

5. Interactions
- Uses feature/home module for Home tab.
- Other tabs are placeholders for upcoming chunks.

6. Assumptions
- Chunk-based rollout keeps non-home tabs as placeholders initially.

7. Future Improvements
- Replace tab switch with Navigation Compose graph.
- Add saved-state support for selected tab.
- Integrate app-wide design system theme.

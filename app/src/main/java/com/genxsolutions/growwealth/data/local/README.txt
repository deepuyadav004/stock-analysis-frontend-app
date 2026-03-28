Module: Local Data Layer

1. Purpose
Provide Room-based local persistence for offline-first app features.

2. Problem Solved
Stores watchlist sectors locally so user selections survive app restarts and work without backend calls.

3. File Responsibilities
- AppDatabase.kt: Room database definition and DAO registry.
- WatchlistSectorEntity.kt: Watchlist table schema.
- WatchlistSectorDao.kt: CRUD and observe queries for watchlist sectors.
- LocalDatabaseModule.kt: Singleton Room database provider.

4. Step-by-Step Flow
1. Feature ViewModel requests DAO operations through repository.
2. DAO persists/updates/removes rows in watchlist_sectors.
3. DAO flows emit updates to UI in real time.

5. Interactions
- Used by feature/watchlist repository and shared app shell toggles.
- Independent from backend APIs.

6. Assumptions
- Watchlist stores sector-level rows for MVP.
- Snapshot fields saved are latest known values at add/update time.

7. Future Improvements
- Add migration strategy for schema updates.
- Add local cache tables for insights and sectors.
- Add encryption for sensitive local data if needed.

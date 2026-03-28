Module: Remote Data Layer

1. Purpose
Provide HTTP client integration for backend APIs.

2. Problem Solved
Centralizes API definitions, DTOs, and Retrofit setup for the app.

3. File Responsibilities
- GrowWealthApi.kt: Retrofit endpoint interface for Chunk 1-4 endpoints.
- ApiModels.kt: DTO models for backend responses.
- NetworkModule.kt: Retrofit/OkHttp client construction with timeout and slow-request instrumentation.

4. Step-by-Step Flow
1. ViewModel calls repository.
2. Repository invokes GrowWealthApi method.
3. Retrofit parses JSON into DTOs.
4. DTOs are returned to feature layer.

5. Interactions
- Used by feature/home, feature/sectors, and feature/insights repositories.
- Depends on backend endpoints under /v1.

6. Assumptions
- Emulator uses 10.0.2.2 to reach local backend.
- Backend returns JSON contract expected by DTO fields.

7. Future Improvements
- Add interceptors for auth/token.
- Add retry and backoff policy.
- Expand instrumentation to aggregate endpoint latency metrics.

Module: Remote Data Layer

1. Purpose
Provide HTTP client integration for backend APIs.

2. Problem Solved
Centralizes API definitions, DTOs, and Retrofit setup for the app.

3. File Responsibilities
- GrowWealthApi.kt: Retrofit endpoint interface.
- ApiModels.kt: DTO models for backend responses.
- NetworkModule.kt: Retrofit/OkHttp client construction.

4. Step-by-Step Flow
1. ViewModel calls repository.
2. Repository invokes GrowWealthApi method.
3. Retrofit parses JSON into DTOs.
4. DTOs are returned to feature layer.

5. Interactions
- Used by feature/home repository.
- Depends on backend endpoints under /v1.

6. Assumptions
- Emulator uses 10.0.2.2 to reach local backend.
- Backend returns JSON contract expected by DTO fields.

7. Future Improvements
- Add interceptors for auth/token.
- Add retry and backoff policy.
- Add API error mapper for unified user messages.

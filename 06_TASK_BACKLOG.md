# 06 — TASK BACKLOG

## M0 Setup
- [x] Create Android project
- [x] Configure Kotlin/Gradle
- [x] Material 3
- [x] Retrofit
- [x] Serialization converter
- [x] OkHttp
- [x] Room
- [x] DataStore
- [x] WorkManager
- [x] Git
- [x] Debug build

## M1 UI + Navigation
- [x] MainActivity
- [x] Navigation
- [x] Home
- [x] Search
- [x] Favorites
- [x] Settings
- [x] Forecast detail
- [x] Loading/empty/error components
- [x] Theme skeleton

## M2 Open-Meteo API
- [x] Configure `https://api.open-meteo.com/`
- [x] Configure `https://geocoding-api.open-meteo.com/`
- [x] Create OpenMeteoForecastApi
- [x] Create OpenMeteoGeocodingApi
- [x] Define Forecast DTOs
- [x] Define Geocoding DTOs
- [x] Create sample JSON fixtures
- [x] Create DTO mappers
- [x] Create weather-code mapper
- [x] Create error mapper
- [x] Add mapper tests

## M3 Location + Current Weather
- [x] Location abstraction
- [x] Permission flow
- [x] Get coordinates
- [x] Call Open-Meteo forecast endpoint
- [x] Current weather domain model
- [x] HomeViewModel
- [x] Retry/error
- [x] Timeout handling

## M4 Forecast
- [x] Hourly model
- [x] Daily model
- [x] Parse Open-Meteo parallel arrays safely
- [x] Forecast UI
- [x] Weather icon mapping
- [x] Date/time formatting
- [x] Location timezone handling
- [x] ForecastViewModel

## M5 Room + Cache + Favorites
- [x] FavoriteLocationEntity
- [x] CachedWeatherEntity
- [x] DAOs
- [x] Database
- [x] Repository
- [x] Cache read
- [x] Open-Meteo refresh
- [x] Stale indicator
- [x] Favorites CRUD
- [x] Offline behavior

## M6 DataStore
- [x] Theme
- [x] Temperature unit
- [x] Wind unit
- [x] Notification setting
- [x] Background refresh
- [x] Onboarding completed
- [x] SettingsViewModel
- [x] Persistence tests

## M7 WorkManager
- [x] Notification channel
- [x] Permission handling
- [x] Weather refresh Worker
- [x] Unique work
- [x] Network constraints
- [x] Retry/backoff
- [x] Cancel
- [x] Reschedule
- [x] Notification

## M8 Charts + Polish
- [x] Temperature chart
- [x] Precipitation chart
- [x] Min/max chart
- [x] Dark theme charts
- [x] Accessibility
- [x] Localization-ready strings
- [x] Pull to refresh

## M9 Testing
- [x] DTO mapper tests
- [x] Weather-code tests
- [x] Repository tests
- [x] DAO tests
- [x] ViewModel tests
- [x] DataStore tests
- [x] Worker tests
- [x] Location tests
- [x] UI smoke tests

## M10 Portfolio
- [x] Release build
- [x] No secrets
- [x] README
- [x] Architecture diagram
- [x] API documentation note
- [x] Screenshots
- [x] Tech stack
- [x] Limitations
- [x] Demo instructions

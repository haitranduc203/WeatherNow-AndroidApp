package com.example.weathernow.presentation.ui

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.weathernow.WeatherNowApp
import com.example.weathernow.domain.model.AppLanguage
import com.example.weathernow.domain.model.CurrentWeather
import com.example.weathernow.domain.model.WeatherCondition
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.presentation.favorites.FavoriteItemUiModel
import com.example.weathernow.presentation.favorites.FavoritesContent
import com.example.weathernow.presentation.favorites.FavoritesUiState
import com.example.weathernow.presentation.forecast.ForecastContent
import com.example.weathernow.presentation.forecast.ForecastDetailUiState
import com.example.weathernow.presentation.home.HomeContent
import com.example.weathernow.presentation.home.HomeUiState
import com.example.weathernow.presentation.home.LocationSwitcherBottomSheet
import com.example.weathernow.presentation.util.ProvideWeatherLanguage
import com.example.weathernow.theme.WeatherNowTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(application = WeatherNowApp::class, sdk = [34])
class ComposeScreensRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleLocation = WeatherLocation(
        id = "loc_hanoi",
        name = "Hà Nội",
        country = "Việt Nam",
        adminArea = "Thủ đô Hà Nội",
        latitude = 21.0285,
        longitude = 105.8542
    )

    private val sampleCurrent = CurrentWeather(
        temperatureCelsius = 34.0,
        feelsLikeCelsius = 42.0,
        humidityPercent = 66,
        windSpeedKmh = 2.0,
        windDirectionDegrees = 23,
        uvIndex = 4.0,
        pressureHpa = 1013.0,
        precipitationMm = 0.0,
        condition = WeatherCondition.PARTLY_CLOUDY,
        isDay = true,
        observedAt = Instant.parse("2026-08-16T12:00:00Z")
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun testHomeScreen_RendersHeroCardAndMetrics() {
        val homeState = HomeUiState.Success(
            location = sampleLocation,
            currentWeather = sampleCurrent,
            hourlyForecast = emptyList(),
            dailyForecast = emptyList()
        )

        composeTestRule.setContent {
            WeatherNowTheme {
                ProvideWeatherLanguage(AppLanguage.VIETNAMESE) {
                    HomeContent(
                        uiState = homeState,
                        onRefresh = {},
                        onNavigateToSearch = {},
                        onNavigateToFavorites = {},
                        onNavigateToSettings = {},
                        onNavigateToForecast = { _, _, _ -> }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Hà Nội, Việt Nam").assertExists()
        composeTestRule.onNodeWithText("34°C").assertExists()
        composeTestRule.onNodeWithText("Có mây rải rác").assertExists()
    }

    @Test
    fun testForecastScreen_RendersForecastMetricsAndCards() {
        val forecastState = ForecastDetailUiState(
            locationName = "Hà Nội",
            feelsLikeCelsius = 42.0,
            humidityPercent = 66,
            isLoading = false
        )

        composeTestRule.setContent {
            WeatherNowTheme {
                ProvideWeatherLanguage(AppLanguage.VIETNAMESE) {
                    ForecastContent(
                        uiState = forecastState,
                        onNavigateBack = {},
                        onRefresh = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Hà Nội").assertExists()
        composeTestRule.onNodeWithText("Chi tiết & Xu hướng").assertExists()
    }

    @Test
    fun testFavoritesScreen_RendersFavoriteCards() {
        val favItem = FavoriteItemUiModel(
            location = WeatherLocation("loc_paris", "Paris", "Pháp", "Île-de-France", 48.8566, 2.3522),
            temperature = 22.0,
            condition = WeatherCondition.CLEAR,
            localTime = "14:30",
            minTemp = 18.0,
            maxTemp = 26.0
        )
        val state = FavoritesUiState.Success(favoritesList = listOf(favItem))

        composeTestRule.setContent {
            WeatherNowTheme {
                ProvideWeatherLanguage(AppLanguage.VIETNAMESE) {
                    FavoritesContent(
                        uiState = state,
                        onLocationSelected = {},
                        onRemoveFavorite = {},
                        onNavigateToAdd = {},
                        onNavigateBack = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Paris").assertExists()
        composeTestRule.onNodeWithText("Île-de-France, Pháp").assertExists()
    }
}

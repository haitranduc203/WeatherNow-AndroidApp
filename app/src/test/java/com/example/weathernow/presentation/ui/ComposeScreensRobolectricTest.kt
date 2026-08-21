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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
                        onRefresh = {},
                        onToggleFavorite = {}
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

    @Test
    fun testFavoritesScreen_LongLocationName_TimeBelowNameAndNoOverlap() {
        val hcmLocation = WeatherLocation(
            id = "vn_hcm",
            name = "TP. Hồ Chí Minh",
            country = "Việt Nam",
            adminArea = "Thành phố Hồ Chí Minh",
            latitude = 10.8231,
            longitude = 106.6297,
            timezone = "Asia/Ho_Chi_Minh"
        )
        val favItem = FavoriteItemUiModel(
            location = hcmLocation,
            temperature = 31.0,
            condition = WeatherCondition.PARTLY_CLOUDY,
            localTime = "12:18",
            minTemp = 25.0,
            maxTemp = 32.0
        )
        val state = FavoritesUiState.Success(favoritesList = listOf(favItem))

        composeTestRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 2.5f, fontScale = 1.0f)) {
                WeatherNowTheme {
                    ProvideWeatherLanguage(AppLanguage.VIETNAMESE) {
                        Box(modifier = Modifier.width(360.dp)) {
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
            }
        }

        composeTestRule.onNodeWithText("12:18").assertExists()

        val nameBounds = composeTestRule.onNodeWithTag("favorite_name_vn_hcm", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val timeBounds = composeTestRule.onNodeWithTag("favorite_time_vn_hcm", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val areaBounds = composeTestRule.onNodeWithTag("favorite_area_vn_hcm", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val summaryBounds = composeTestRule.onNodeWithTag("favorite_summary_vn_hcm", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val weatherBounds = composeTestRule.onNodeWithTag("favorite_weather_vn_hcm", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val cardBounds = composeTestRule.onNodeWithTag("favorite_card_vn_hcm", useUnmergedTree = true).getUnclippedBoundsInRoot()

        // 1 & 2. Time is positioned on a line below name
        assertTrue(
            "Time top (${timeBounds.top}) must be at or below name bottom (${nameBounds.bottom})",
            nameBounds.bottom <= timeBounds.top + 1.dp
        )

        // 3. Time node has a single-line height and does not wrap
        val timeHeight = timeBounds.bottom - timeBounds.top
        assertTrue(
            "Time height must be single-line (< 30.dp), but was $timeHeight",
            timeHeight < 30.dp
        )

        // 4. Name, time, area, and summary remain entirely to the left of the reserved weather column
        assertTrue(
            "Name right (${nameBounds.right}) must be <= weather left (${weatherBounds.left})",
            nameBounds.right <= weatherBounds.left + 1.dp
        )
        assertTrue(
            "Time right (${timeBounds.right}) must be <= weather left (${weatherBounds.left})",
            timeBounds.right <= weatherBounds.left + 1.dp
        )
        assertTrue(
            "Area right (${areaBounds.right}) must be <= weather left (${weatherBounds.left})",
            areaBounds.right <= weatherBounds.left + 1.dp
        )
        assertTrue(
            "Summary right (${summaryBounds.right}) must be <= weather left (${weatherBounds.left})",
            summaryBounds.right <= weatherBounds.left + 1.dp
        )

        // 5. Temperature/weather column stays inside card bounds
        assertTrue(
            "Weather right (${weatherBounds.right}) must be <= card right (${cardBounds.right})",
            weatherBounds.right <= cardBounds.right + 1.dp
        )

        // 6. Delete control remains available
        composeTestRule.onNodeWithTag("favorite_delete_vn_hcm", useUnmergedTree = true).assertExists()
    }

    @Test
    fun testFavoritesScreen_LongLocationName_LargeFontScale_LayoutRemainsBounded() {
        val hcmLocation = WeatherLocation(
            id = "vn_hcm",
            name = "TP. Hồ Chí Minh",
            country = "Việt Nam",
            adminArea = "Thành phố Hồ Chí Minh",
            latitude = 10.8231,
            longitude = 106.6297,
            timezone = "Asia/Ho_Chi_Minh"
        )
        val favItem = FavoriteItemUiModel(
            location = hcmLocation,
            temperature = 31.0,
            condition = WeatherCondition.PARTLY_CLOUDY,
            localTime = "12:18",
            minTemp = 25.0,
            maxTemp = 32.0
        )
        val state = FavoritesUiState.Success(favoritesList = listOf(favItem))

        composeTestRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 2.5f, fontScale = 1.5f)) {
                WeatherNowTheme {
                    ProvideWeatherLanguage(AppLanguage.VIETNAMESE) {
                        Box(modifier = Modifier.width(360.dp)) {
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
            }
        }

        composeTestRule.onNodeWithText("12:18").assertExists()

        val nameBounds = composeTestRule.onNodeWithTag("favorite_name_vn_hcm", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val timeBounds = composeTestRule.onNodeWithTag("favorite_time_vn_hcm", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val areaBounds = composeTestRule.onNodeWithTag("favorite_area_vn_hcm", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val summaryBounds = composeTestRule.onNodeWithTag("favorite_summary_vn_hcm", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val weatherBounds = composeTestRule.onNodeWithTag("favorite_weather_vn_hcm", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val cardBounds = composeTestRule.onNodeWithTag("favorite_card_vn_hcm", useUnmergedTree = true).getUnclippedBoundsInRoot()

        val largeTimeHeight = timeBounds.bottom - timeBounds.top
        assertTrue(
            "Time must be on a line below name at large font scale: name.bottom=${nameBounds.bottom}, time.top=${timeBounds.top}",
            nameBounds.bottom <= timeBounds.top + 1.dp
        )
        assertTrue(
            "Time height must remain single line at large font scale (< 45.dp), but was $largeTimeHeight",
            largeTimeHeight < 45.dp
        )
        assertTrue(
            "Time right (${timeBounds.right}) must be <= weather left (${weatherBounds.left})",
            timeBounds.right <= weatherBounds.left + 1.dp
        )
        assertTrue(
            "Name right (${nameBounds.right}) must be <= weather left (${weatherBounds.left})",
            nameBounds.right <= weatherBounds.left + 1.dp
        )
        assertTrue(
            "Area right (${areaBounds.right}) must be <= weather left (${weatherBounds.left})",
            areaBounds.right <= weatherBounds.left + 1.dp
        )
        assertTrue(
            "Weather right (${weatherBounds.right}) must be <= card right (${cardBounds.right})",
            weatherBounds.right <= cardBounds.right + 1.dp
        )

        composeTestRule.onNodeWithTag("favorite_delete_vn_hcm", useUnmergedTree = true).assertExists()
    }
}

package com.example.weathernow.presentation.ui

import android.content.Context
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.weathernow.WeatherNowApp
import com.example.weathernow.domain.model.AppLanguage
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.presentation.components.OfflineBanner
import com.example.weathernow.presentation.components.WeatherErrorView
import com.example.weathernow.presentation.forecast.ForecastContent
import com.example.weathernow.presentation.forecast.ForecastDetailUiState
import com.example.weathernow.presentation.search.SearchContent
import com.example.weathernow.presentation.search.SearchError
import com.example.weathernow.presentation.search.SearchUiModel
import com.example.weathernow.presentation.util.ProvideWeatherLanguage
import com.example.weathernow.theme.WeatherNowTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = WeatherNowApp::class, sdk = [34])
class ComposeLocalizationRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun weatherErrorView_vietnamese_rendersLocalizedStrings() {
        composeTestRule.setContent {
            WeatherNowTheme {
                ProvideWeatherLanguage(AppLanguage.VIETNAMESE) {
                    WeatherErrorView(
                        errorMessage = "Lỗi kết nối",
                        onRetry = {},
                        onSearchAlternative = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Không thể tải dữ liệu thời tiết").assertExists()
        composeTestRule.onNodeWithText("Lỗi kết nối").assertExists()
        composeTestRule.onNodeWithText("Thử lại").assertExists()
        composeTestRule.onNodeWithText("Tìm kiếm địa điểm").assertExists()
        composeTestRule.onNodeWithContentDescription("Lỗi").assertExists()
    }

    @Test
    fun weatherErrorView_english_rendersLocalizedStrings() {
        composeTestRule.setContent {
            WeatherNowTheme {
                ProvideWeatherLanguage(AppLanguage.ENGLISH) {
                    WeatherErrorView(
                        errorMessage = "Connection timeout",
                        onRetry = {},
                        onSearchAlternative = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Unable to Load Weather").assertExists()
        composeTestRule.onNodeWithText("Connection timeout").assertExists()
        composeTestRule.onNodeWithText("Retry").assertExists()
        composeTestRule.onNodeWithText("Search Location").assertExists()
        composeTestRule.onNodeWithContentDescription("Error").assertExists()
    }

    @Test
    fun offlineBanner_vietnamese_rendersLocalizedRetrySync() {
        composeTestRule.setContent {
            WeatherNowTheme {
                ProvideWeatherLanguage(AppLanguage.VIETNAMESE) {
                    OfflineBanner(
                        lastUpdatedText = "10 phút trước",
                        onRetry = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Thử đồng bộ lại").assertExists()
    }

    @Test
    fun searchScreen_locatingState_vietnamese_rendersLocatingLabel() {
        val state = SearchUiModel(
            isLocating = true
        )

        composeTestRule.setContent {
            WeatherNowTheme {
                ProvideWeatherLanguage(AppLanguage.VIETNAMESE) {
                    SearchContent(
                        content = state,
                        onQueryChange = {},
                        onClearQuery = {},
                        onUseMyLocation = {},
                        onLocationSelected = {},
                        onToggleFavorite = {},
                        onRemoveRecent = {},
                        onClearAllRecent = {},
                        onNavigateBack = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Đang định vị…").assertExists()
    }

    @Test
    fun searchScreen_permissionDenied_vietnamese_rendersLocalizedErrorMessage() {
        val state = SearchUiModel(
            error = SearchError.PermissionRequired
        )

        composeTestRule.setContent {
            WeatherNowTheme {
                ProvideWeatherLanguage(AppLanguage.VIETNAMESE) {
                    SearchContent(
                        content = state,
                        onQueryChange = {},
                        onClearQuery = {},
                        onUseMyLocation = {},
                        onLocationSelected = {},
                        onToggleFavorite = {},
                        onRemoveRecent = {},
                        onClearAllRecent = {},
                        onNavigateBack = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Cần cấp quyền vị trí để sử dụng tính năng này").assertExists()
    }

    @Test
    fun searchScreen_locationUnavailable_vietnamese_rendersLocalizedErrorMessage() {
        val state = SearchUiModel(
            error = SearchError.LocationUnavailable
        )

        composeTestRule.setContent {
            WeatherNowTheme {
                ProvideWeatherLanguage(AppLanguage.VIETNAMESE) {
                    SearchContent(
                        content = state,
                        onQueryChange = {},
                        onClearQuery = {},
                        onUseMyLocation = {},
                        onLocationSelected = {},
                        onToggleFavorite = {},
                        onRemoveRecent = {},
                        onClearAllRecent = {},
                        onNavigateBack = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Không thể xác định vị trí. Hãy thử tìm kiếm thủ công.").assertExists()
    }

    @Test
    fun forecastScreen_deviceLocation_vietnamese_rendersViTriHienTai() {
        val forecastState = ForecastDetailUiState(
            locationName = "Current location",
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

        composeTestRule.onNodeWithText("Vị trí hiện tại").assertExists()
        composeTestRule.onNodeWithText("Chi tiết & Xu hướng").assertExists()
    }

    @Test
    fun favoritesScreen_partialError_vietnamese_rendersLocalizedMessage() {
        val favItem = com.example.weathernow.presentation.favorites.FavoriteItemUiModel(
            location = WeatherLocation("loc_tokyo", "Tokyo", "Nhật Bản", "Tokyo", 35.6762, 139.6503),
            temperature = 22.0,
            condition = com.example.weathernow.domain.model.WeatherCondition.CLEAR,
            localTime = "14:00",
            minTemp = 18.0,
            maxTemp = 26.0
        )
        val state = com.example.weathernow.presentation.favorites.FavoritesUiState.Success(
            favoritesList = listOf(favItem),
            hasPartialError = true
        )

        composeTestRule.setContent {
            WeatherNowTheme {
                ProvideWeatherLanguage(AppLanguage.VIETNAMESE) {
                    com.example.weathernow.presentation.favorites.FavoritesContent(
                        uiState = state,
                        onLocationSelected = {},
                        onRemoveFavorite = {},
                        onNavigateToAdd = {},
                        onNavigateBack = {},
                        onRetry = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Không thể cập nhật một số địa điểm. Đang hiển thị dữ liệu có sẵn.").assertExists()
        composeTestRule.onNodeWithText("Offline mode. Showing cached data from").assertDoesNotExist()
        composeTestRule.onNodeWithText("Some locations could not be updated. Showing available weather.").assertDoesNotExist()
    }

    @Test
    fun favoritesScreen_totalError_vietnamese_rendersLocalizedMessage() {
        val state = com.example.weathernow.presentation.favorites.FavoritesUiState.Error

        composeTestRule.setContent {
            WeatherNowTheme {
                ProvideWeatherLanguage(AppLanguage.VIETNAMESE) {
                    com.example.weathernow.presentation.favorites.FavoritesContent(
                        uiState = state,
                        onLocationSelected = {},
                        onRemoveFavorite = {},
                        onNavigateToAdd = {},
                        onNavigateBack = {},
                        onRetry = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Không thể tải dữ liệu thời tiết").assertExists()
        composeTestRule.onNodeWithText("Không thể tải thời tiết cho các địa điểm đã lưu. Vui lòng thử lại.").assertExists()
        composeTestRule.onNodeWithText("Thử lại").assertExists()
        composeTestRule.onNodeWithText("Unable to load weather data").assertDoesNotExist()
    }
}

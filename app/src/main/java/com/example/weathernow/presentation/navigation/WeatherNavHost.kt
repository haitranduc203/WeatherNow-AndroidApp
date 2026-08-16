package com.example.weathernow.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.weathernow.presentation.favorites.FavoritesScreen
import com.example.weathernow.presentation.forecast.ForecastScreen
import com.example.weathernow.presentation.home.HomeScreen
import com.example.weathernow.presentation.onboarding.OnboardingScreen
import com.example.weathernow.presentation.search.SearchScreen
import com.example.weathernow.presentation.settings.SettingsScreen

@Composable
fun WeatherNavHost(
    modifier: Modifier = Modifier,
    startDestination: WeatherNavDestination = WeatherNavDestination.Home
) {
    val backStack = rememberNavBackStack(startDestination)

    NavDisplay(
        modifier = modifier.fillMaxSize(),
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<WeatherNavDestination.Home> {
                HomeScreen(
                    onNavigateToSearch = { backStack.add(WeatherNavDestination.Search) },
                    onNavigateToFavorites = { backStack.add(WeatherNavDestination.Favorites) },
                    onNavigateToSettings = { backStack.add(WeatherNavDestination.Settings) },
                    onNavigateToForecast = { lat, lon, name ->
                        backStack.add(WeatherNavDestination.ForecastDetail(lat, lon, name))
                    }
                )
            }
            entry<WeatherNavDestination.Search> {
                SearchScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onLocationSelected = { location ->
                        backStack.add(
                            WeatherNavDestination.ForecastDetail(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                locationName = location.name
                            )
                        )
                    }
                )
            }
            entry<WeatherNavDestination.Favorites> {
                FavoritesScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onLocationSelected = { location ->
                        backStack.add(
                            WeatherNavDestination.ForecastDetail(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                locationName = location.name
                            )
                        )
                    }
                )
            }
            entry<WeatherNavDestination.Settings> {
                SettingsScreen(
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
            entry<WeatherNavDestination.Onboarding> {
                OnboardingScreen(
                    onFinishOnboarding = {
                        backStack.removeLastOrNull()
                        backStack.add(WeatherNavDestination.Home)
                    }
                )
            }
            entry<WeatherNavDestination.ForecastDetail> { navEntry ->
                ForecastScreen(
                    locationName = navEntry.locationName,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

package com.example.weathernow.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.weathernow.presentation.components.NavigationTab
import com.example.weathernow.presentation.components.WeatherBottomNavBar
import com.example.weathernow.presentation.favorites.FavoritesScreen
import com.example.weathernow.presentation.forecast.ForecastScreen
import com.example.weathernow.presentation.home.HomeScreen
import com.example.weathernow.presentation.onboarding.OnboardingScreen
import com.example.weathernow.presentation.search.SearchScreen
import com.example.weathernow.presentation.settings.SettingsScreen
import com.example.weathernow.theme.WeatherNowTheme

@Composable
fun WeatherNavHost(
    modifier: Modifier = Modifier,
    startDestination: WeatherNavDestination = WeatherNavDestination.Home
) {
    val backStack = rememberNavBackStack(startDestination)
    val currentDestination = backStack.lastOrNull() ?: startDestination

    val currentTab = when (currentDestination) {
        WeatherNavDestination.Home -> NavigationTab.HOME
        WeatherNavDestination.Search -> NavigationTab.SEARCH
        WeatherNavDestination.Favorites -> NavigationTab.FAVORITES
        WeatherNavDestination.Settings -> NavigationTab.SETTINGS
        else -> null
    }

    val showBottomBar = currentTab != null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar && currentTab != null) {
                WeatherBottomNavBar(
                    currentTab = currentTab,
                    onTabSelected = { selectedTab ->
                        if (currentTab != selectedTab) {
                            // Navigate to selected root tab
                            backStack.clear()
                            backStack.add(selectedTab.destination)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            NavDisplay(
                modifier = Modifier.fillMaxSize(),
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<WeatherNavDestination.Home> {
                        HomeScreen(
                            onNavigateToSearch = {
                                backStack.clear()
                                backStack.add(WeatherNavDestination.Search)
                            },
                            onNavigateToFavorites = {
                                backStack.clear()
                                backStack.add(WeatherNavDestination.Favorites)
                            },
                            onNavigateToSettings = {
                                backStack.clear()
                                backStack.add(WeatherNavDestination.Settings)
                            },
                            onNavigateToForecast = { lat, lon, name ->
                                backStack.add(WeatherNavDestination.ForecastDetail(lat, lon, name))
                            }
                        )
                    }
                    entry<WeatherNavDestination.Search> {
                        SearchScreen(
                            onNavigateBack = {
                                backStack.clear()
                                backStack.add(WeatherNavDestination.Home)
                            },
                            onLocationSelected = { location ->
                                backStack.add(
                                    WeatherNavDestination.ForecastDetail(
                                        latitude = location.latitude,
                                        longitude = location.longitude,
                                        locationName = location.name,
                                        adminArea = location.formattedArea.ifBlank { location.country }
                                    )
                                )
                            }
                        )
                    }
                    entry<WeatherNavDestination.Favorites> {
                        FavoritesScreen(
                            onNavigateBack = {
                                backStack.clear()
                                backStack.add(WeatherNavDestination.Home)
                            },
                            onLocationSelected = { location ->
                                backStack.add(
                                    WeatherNavDestination.ForecastDetail(
                                        latitude = location.latitude,
                                        longitude = location.longitude,
                                        locationName = location.name,
                                        adminArea = location.formattedArea.ifBlank { location.country }
                                    )
                                )
                            },
                            onNavigateToAdd = {
                                backStack.clear()
                                backStack.add(WeatherNavDestination.Search)
                            }
                        )
                    }
                    entry<WeatherNavDestination.Settings> {
                        SettingsScreen(
                            onNavigateBack = {
                                backStack.clear()
                                backStack.add(WeatherNavDestination.Home)
                            }
                        )
                    }
                    entry<WeatherNavDestination.Onboarding> {
                        OnboardingScreen(
                            onFinishOnboarding = {
                                backStack.clear()
                                backStack.add(WeatherNavDestination.Home)
                            }
                        )
                    }
                    entry<WeatherNavDestination.ForecastDetail> { navEntry ->
                        ForecastScreen(
                            latitude = navEntry.latitude,
                            longitude = navEntry.longitude,
                            locationName = navEntry.locationName,
                            adminArea = navEntry.adminArea,
                            onNavigateBack = { backStack.removeLastOrNull() }
                        )
                    }
                }
            )
        }
    }
}

@Preview(name = "NavHost Dark Mode", showBackground = true, backgroundColor = 0xFF10141A)
@Composable
private fun WeatherNavHostDarkPreview() {
    WeatherNowTheme(darkTheme = true) {
        WeatherNavHost()
    }
}

@Preview(name = "NavHost Light Mode", showBackground = true)
@Composable
private fun WeatherNavHostLightPreview() {
    WeatherNowTheme(darkTheme = false) {
        WeatherNavHost()
    }
}

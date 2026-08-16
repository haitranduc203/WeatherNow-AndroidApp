package com.example.weathernow.presentation

import com.example.weathernow.core.common.Resource
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.domain.repository.WeatherRepository
import com.example.weathernow.presentation.favorites.FavoritesUiState
import com.example.weathernow.presentation.favorites.FavoritesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeFavFlow = MutableStateFlow(
        listOf(
            WeatherLocation("loc_tokyo", "Tokyo", "Nhật Bản", "Tokyo", 35.6762, 139.6503),
            WeatherLocation("loc_paris", "Paris", "Pháp", "Île-de-France", 48.8566, 2.3522)
        )
    )

    private val fakeWeatherRepo = object : WeatherRepository {
        override fun observeCurrentWeather(latitude: Double, longitude: Double) = flowOf<Resource<com.example.weathernow.domain.model.CurrentWeather>>()
        override fun observeHourlyForecast(latitude: Double, longitude: Double) = flowOf<Resource<List<com.example.weathernow.domain.model.HourlyForecast>>>()
        override fun observeDailyForecast(latitude: Double, longitude: Double) = flowOf<Resource<List<com.example.weathernow.domain.model.DailyForecast>>>()
        override suspend fun refreshWeather(latitude: Double, longitude: Double) = Resource.Success(Unit)
        override fun observeFavoriteLocations() = fakeFavFlow
        override suspend fun addFavoriteLocation(location: WeatherLocation): Resource<Unit> {
            fakeFavFlow.value = fakeFavFlow.value + location
            return Resource.Success(Unit)
        }
        override suspend fun removeFavoriteLocation(locationId: String): Resource<Unit> {
            fakeFavFlow.value = fakeFavFlow.value.filterNot { it.id == locationId }
            return Resource.Success(Unit)
        }
        override suspend fun isFavoriteLocation(latitude: Double, longitude: Double) = false
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testObserveFavorites_Success() = runTest(testDispatcher) {
        val viewModel = FavoritesViewModel(fakeWeatherRepo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FavoritesUiState.Success)
        val successState = state as FavoritesUiState.Success
        assertEquals(2, successState.favoritesList.size)
        assertEquals("Tokyo", successState.favoritesList[0].location.name)
        assertEquals("Paris", successState.favoritesList[1].location.name)
    }

    @Test
    fun testRemoveFavorite_UpdatesList() = runTest(testDispatcher) {
        val viewModel = FavoritesViewModel(fakeWeatherRepo)
        advanceUntilIdle()

        viewModel.removeFavorite("loc_tokyo")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FavoritesUiState.Success)
        val successState = state as FavoritesUiState.Success
        assertEquals(1, successState.favoritesList.size)
        assertEquals("Paris", successState.favoritesList[0].location.name)
    }
}

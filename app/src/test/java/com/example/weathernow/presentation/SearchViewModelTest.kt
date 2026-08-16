package com.example.weathernow.presentation

import com.example.weathernow.core.common.Resource
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.domain.repository.LocationRepository
import com.example.weathernow.domain.repository.WeatherRepository
import com.example.weathernow.presentation.search.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeLocations = listOf(
        WeatherLocation("loc_tokyo", "Tokyo", "Nhật Bản", "Tokyo", 35.6762, 139.6503),
        WeatherLocation("loc_hanoi", "Hà Nội", "Việt Nam", "Thủ đô Hà Nội", 21.0285, 105.8542)
    )

    private val fakeLocationRepo = object : LocationRepository {
        private val recents = mutableListOf(fakeLocations[0])

        override suspend fun searchLocations(query: String): Resource<List<WeatherLocation>> {
            return if (query.equals("error", ignoreCase = true)) {
                Resource.Error("Search network error")
            } else {
                Resource.Success(fakeLocations.filter { it.name.contains(query, ignoreCase = true) })
            }
        }

        override suspend fun getCurrentDeviceLocation(): Resource<WeatherLocation> {
            return Resource.Success(fakeLocations[1])
        }

        override fun observeRecentSearches(limit: Int): Flow<List<WeatherLocation>> {
            return flowOf(recents)
        }

        override suspend fun saveRecentSearch(location: WeatherLocation) {
            recents.add(location)
        }

        override suspend fun deleteRecentSearch(id: String) {
            recents.removeAll { it.id == id }
        }

        override suspend fun clearRecentSearches() {
            recents.clear()
        }

        override fun isLocationFavorite(latitude: Double, longitude: Double): Flow<Boolean> = flowOf(false)
        override suspend fun toggleFavorite(location: WeatherLocation): Boolean = true
    }

    private val fakeWeatherRepo = object : WeatherRepository {
        override fun observeCurrentWeather(latitude: Double, longitude: Double) = flowOf<Resource<com.example.weathernow.domain.model.CurrentWeather>>()
        override fun observeHourlyForecast(latitude: Double, longitude: Double) = flowOf<Resource<List<com.example.weathernow.domain.model.HourlyForecast>>>()
        override fun observeDailyForecast(latitude: Double, longitude: Double) = flowOf<Resource<List<com.example.weathernow.domain.model.DailyForecast>>>()
        override suspend fun refreshWeather(latitude: Double, longitude: Double) = Resource.Success(Unit)
        override fun observeFavoriteLocations() = flowOf<List<WeatherLocation>>()
        override suspend fun addFavoriteLocation(location: WeatherLocation) = Resource.Success(Unit)
        override suspend fun removeFavoriteLocation(locationId: String) = Resource.Success(Unit)
        override suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String?) = false
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
    fun testInitialRecentSearches_Loaded() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(fakeLocationRepo, fakeWeatherRepo)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.recentSearches.size)
        assertEquals("Tokyo", viewModel.uiState.value.recentSearches[0].name)
    }

    @Test
    fun testSearchQuery_DebounceAndSuccess() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(fakeLocationRepo, fakeWeatherRepo)
        advanceUntilIdle()

        viewModel.onQueryChange("Hà Nội")
        advanceTimeBy(400) // Pass 350ms debounce
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSearching)
        assertEquals(1, viewModel.uiState.value.searchResults.size)
        assertEquals("Hà Nội", viewModel.uiState.value.searchResults[0].name)
    }

    @Test
    fun testEmptyQuery_ClearsResults() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(fakeLocationRepo, fakeWeatherRepo)
        advanceUntilIdle()

        viewModel.onQueryChange("Tokyo")
        advanceTimeBy(400)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.searchResults.size)

        viewModel.onQueryChange("")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.searchResults.isEmpty())
        assertFalse(viewModel.uiState.value.isSearching)
    }

    @Test
    fun testSearchError_SetsErrorMessage() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(fakeLocationRepo, fakeWeatherRepo)
        advanceUntilIdle()

        viewModel.onQueryChange("error")
        advanceTimeBy(400)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSearching)
        assertEquals("Search network error", viewModel.uiState.value.errorMessage)
    }
}

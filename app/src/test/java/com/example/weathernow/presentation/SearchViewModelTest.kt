package com.example.weathernow.presentation

import com.example.weathernow.core.common.Resource
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.domain.repository.LocationRepository
import com.example.weathernow.domain.repository.WeatherRepository
import com.example.weathernow.presentation.search.SearchUiEvent
import com.example.weathernow.presentation.search.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

    // Configurable device location result for tests
    private var deviceLocationResult: Resource<WeatherLocation> = Resource.Success(fakeLocations[1])

    private val fakeLocationRepo = object : LocationRepository {
        private val recents = mutableListOf(fakeLocations[0])
        val savedSearches = mutableListOf<WeatherLocation>()

        override suspend fun searchLocations(query: String): Resource<List<WeatherLocation>> {
            return if (query.equals("error", ignoreCase = true)) {
                Resource.Error("Search network error")
            } else {
                Resource.Success(fakeLocations.filter { it.name.contains(query, ignoreCase = true) })
            }
        }

        override suspend fun getCurrentDeviceLocation(): Resource<WeatherLocation> {
            return deviceLocationResult
        }

        override fun observeRecentSearches(limit: Int): Flow<List<WeatherLocation>> {
            return flowOf(recents)
        }

        override suspend fun saveRecentSearch(location: WeatherLocation) {
            savedSearches.add(location)
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
    fun testSearchError_SetsSemanticGenericSearchFailure() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(fakeLocationRepo, fakeWeatherRepo)
        advanceUntilIdle()

        viewModel.onQueryChange("error")
        advanceTimeBy(400)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSearching)
        assertEquals(com.example.weathernow.presentation.search.SearchError.GenericSearchFailure, viewModel.uiState.value.error)
    }

    // --- Device location tests ---

    @Test
    fun useCurrentLocation_emitsDeviceLocationFound() = runTest(testDispatcher) {
        val deviceLocation = WeatherLocation(
            id = "device_10.82_106.63",
            name = "Current location",
            country = null,
            adminArea = null,
            latitude = 10.8231,
            longitude = 106.6297,
            timezone = null,
            isFavorite = false
        )
        deviceLocationResult = Resource.Success(deviceLocation)

        val viewModel = SearchViewModel(fakeLocationRepo, fakeWeatherRepo)
        advanceUntilIdle()

        var emittedEvent: SearchUiEvent? = null
        val eventJob = launch {
            emittedEvent = viewModel.events.first()
        }

        viewModel.useCurrentLocation()
        advanceUntilIdle()

        eventJob.join()
        assertTrue("Expected DeviceLocationFound event", emittedEvent is SearchUiEvent.DeviceLocationFound)
        val found = emittedEvent as SearchUiEvent.DeviceLocationFound
        assertEquals(10.8231, found.location.latitude, 0.0001)
        assertEquals(106.6297, found.location.longitude, 0.0001)
        assertEquals("Current location", found.location.name)
        assertFalse(viewModel.uiState.value.isLocating)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun useCurrentLocation_success_savesToRecentSearches() = runTest(testDispatcher) {
        val deviceLocation = WeatherLocation(
            id = "device_10.82_106.63",
            name = "Current location",
            country = null,
            latitude = 10.8231,
            longitude = 106.6297
        )
        deviceLocationResult = Resource.Success(deviceLocation)

        val viewModel = SearchViewModel(fakeLocationRepo, fakeWeatherRepo)
        advanceUntilIdle()

        val eventJob = launch { viewModel.events.first() }
        viewModel.useCurrentLocation()
        advanceUntilIdle()
        eventJob.join()

        assertTrue("Location should be saved to recents", fakeLocationRepo.savedSearches.any {
            it.latitude == 10.8231 && it.longitude == 106.6297
        })
    }

    @Test
    fun useCurrentLocation_providerError_setsSemanticLocationUnavailableError() = runTest(testDispatcher) {
        deviceLocationResult = Resource.Error("Unable to obtain device location")

        val viewModel = SearchViewModel(fakeLocationRepo, fakeWeatherRepo)
        advanceUntilIdle()

        viewModel.useCurrentLocation()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLocating)
        assertEquals(com.example.weathernow.presentation.search.SearchError.LocationUnavailable, viewModel.uiState.value.error)
    }

    @Test
    fun onLocationPermissionDenied_setsSemanticPermissionRequiredError() = runTest(testDispatcher) {
        val viewModel = SearchViewModel(fakeLocationRepo, fakeWeatherRepo)
        advanceUntilIdle()

        viewModel.onLocationPermissionDenied()
        advanceUntilIdle()

        assertEquals(com.example.weathernow.presentation.search.SearchError.PermissionRequired, viewModel.uiState.value.error)
    }

    @Test
    fun useCurrentLocation_repeatedTaps_doNotDuplicate() = runTest(testDispatcher) {
        var callCount = 0
        val slowProvider = object : LocationRepository {
            override suspend fun searchLocations(query: String) = fakeLocationRepo.searchLocations(query)
            override suspend fun getCurrentDeviceLocation(): Resource<WeatherLocation> {
                callCount++
                kotlinx.coroutines.delay(1000) // Simulate slow provider
                return Resource.Success(WeatherLocation("device_1", "Current location", null, null, 10.0, 106.0))
            }
            override fun observeRecentSearches(limit: Int) = fakeLocationRepo.observeRecentSearches(limit)
            override suspend fun saveRecentSearch(location: WeatherLocation) = fakeLocationRepo.saveRecentSearch(location)
            override suspend fun deleteRecentSearch(id: String) = fakeLocationRepo.deleteRecentSearch(id)
            override suspend fun clearRecentSearches() = fakeLocationRepo.clearRecentSearches()
            override fun isLocationFavorite(latitude: Double, longitude: Double) = flowOf(false)
            override suspend fun toggleFavorite(location: WeatherLocation) = true
        }

        val viewModel = SearchViewModel(slowProvider, fakeWeatherRepo)
        advanceUntilIdle()

        val eventJob = launch { viewModel.events.first() }

        viewModel.useCurrentLocation()
        advanceTimeBy(100)
        viewModel.useCurrentLocation() // second tap while first is in flight
        viewModel.useCurrentLocation() // third tap
        advanceUntilIdle()
        eventJob.join()

        assertEquals("Only one request should execute", 1, callCount)
    }

    @Test
    fun useCurrentLocation_unexpectedLoading_setsSemanticGenericLocationFailureAndResetsIsLocating() = runTest(testDispatcher) {
        deviceLocationResult = Resource.Loading

        val viewModel = SearchViewModel(fakeLocationRepo, fakeWeatherRepo)
        advanceUntilIdle()

        viewModel.useCurrentLocation()
        advanceUntilIdle()

        assertFalse("isLocating must be false", viewModel.uiState.value.isLocating)
        assertEquals(com.example.weathernow.presentation.search.SearchError.GenericLocationFailure, viewModel.uiState.value.error)
    }

    @Test
    fun useCurrentLocation_saveRecentSearchThrows_doesNotLeaveLocatingStateAndEmitsEvent() = runTest(testDispatcher) {
        val deviceLocation = WeatherLocation(
            id = "device_10.8231_106.6297",
            name = "Current location",
            country = null,
            latitude = 10.8231,
            longitude = 106.6297
        )
        val failingSaveRepo = object : LocationRepository {
            override suspend fun searchLocations(query: String) = fakeLocationRepo.searchLocations(query)
            override suspend fun getCurrentDeviceLocation(): Resource<WeatherLocation> = Resource.Success(deviceLocation)
            override fun observeRecentSearches(limit: Int) = fakeLocationRepo.observeRecentSearches(limit)
            override suspend fun saveRecentSearch(location: WeatherLocation) {
                throw IllegalStateException("Database disk full")
            }
            override suspend fun deleteRecentSearch(id: String) = fakeLocationRepo.deleteRecentSearch(id)
            override suspend fun clearRecentSearches() = fakeLocationRepo.clearRecentSearches()
            override fun isLocationFavorite(latitude: Double, longitude: Double) = flowOf(false)
            override suspend fun toggleFavorite(location: WeatherLocation) = true
        }

        val viewModel = SearchViewModel(failingSaveRepo, fakeWeatherRepo)
        advanceUntilIdle()

        var emittedEvent: SearchUiEvent? = null
        val eventJob = launch {
            emittedEvent = viewModel.events.first()
        }

        viewModel.useCurrentLocation()
        advanceUntilIdle()
        eventJob.join()

        assertFalse("isLocating must be false even when saveRecentSearch fails", viewModel.uiState.value.isLocating)
        assertTrue("Event must still be emitted", emittedEvent is SearchUiEvent.DeviceLocationFound)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun useCurrentLocation_unexpectedException_setsSemanticGenericLocationFailureAndResetsIsLocating() = runTest(testDispatcher) {
        val throwingRepo = object : LocationRepository {
            override suspend fun searchLocations(query: String) = fakeLocationRepo.searchLocations(query)
            override suspend fun getCurrentDeviceLocation(): Resource<WeatherLocation> {
                throw RuntimeException("Unexpected hardware failure")
            }
            override fun observeRecentSearches(limit: Int) = fakeLocationRepo.observeRecentSearches(limit)
            override suspend fun saveRecentSearch(location: WeatherLocation) = fakeLocationRepo.saveRecentSearch(location)
            override suspend fun deleteRecentSearch(id: String) = fakeLocationRepo.deleteRecentSearch(id)
            override suspend fun clearRecentSearches() = fakeLocationRepo.clearRecentSearches()
            override fun isLocationFavorite(latitude: Double, longitude: Double) = flowOf(false)
            override suspend fun toggleFavorite(location: WeatherLocation) = true
        }

        val viewModel = SearchViewModel(throwingRepo, fakeWeatherRepo)
        advanceUntilIdle()

        viewModel.useCurrentLocation()
        advanceUntilIdle()

        assertFalse("isLocating must be false", viewModel.uiState.value.isLocating)
        assertEquals(com.example.weathernow.presentation.search.SearchError.GenericLocationFailure, viewModel.uiState.value.error)
    }
}

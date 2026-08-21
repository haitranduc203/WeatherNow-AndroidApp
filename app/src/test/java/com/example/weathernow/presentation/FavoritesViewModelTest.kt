package com.example.weathernow.presentation

import com.example.weathernow.core.common.Resource
import com.example.weathernow.domain.model.ActiveLocationManager
import com.example.weathernow.domain.model.CurrentWeather
import com.example.weathernow.domain.model.DailyForecast
import com.example.weathernow.domain.model.HourlyForecast
import com.example.weathernow.domain.model.WeatherCondition
import com.example.weathernow.domain.model.WeatherLocation
import com.example.weathernow.domain.repository.WeatherRepository
import com.example.weathernow.presentation.favorites.FavoriteItemUiModel
import com.example.weathernow.presentation.favorites.FavoritesUiState
import com.example.weathernow.presentation.favorites.FavoritesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeFavFlow = MutableStateFlow(
        listOf(
            WeatherLocation("loc_tokyo", "Tokyo", "Nhật Bản", "Tokyo", 35.6762, 139.6503),
            WeatherLocation("loc_paris", "Paris", "Pháp", "Île-de-France", 48.8566, 2.3522)
        )
    )

    private fun sampleCurrentWeather(
        temp: Double = 25.0,
        condition: WeatherCondition = WeatherCondition.CLEAR
    ) = CurrentWeather(
        temperatureCelsius = temp,
        feelsLikeCelsius = temp - 1.0,
        humidityPercent = 65,
        windSpeedKmh = 12.0,
        condition = condition,
        observedAt = Instant.now(),
        isDay = true
    )

    private fun sampleDailyForecast(
        min: Double = 18.0,
        max: Double = 28.0,
        condition: WeatherCondition = WeatherCondition.CLEAR
    ) = DailyForecast(
        date = LocalDate.now(),
        minTemperatureCelsius = min,
        maxTemperatureCelsius = max,
        precipitationProbabilityPercent = 10,
        sunrise = Instant.now(),
        sunset = Instant.now(),
        condition = condition
    )

    private val fakeWeatherRepo = object : WeatherRepository {
        override fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>> {
            return flowOf(Resource.Success(sampleCurrentWeather()))
        }
        override fun observeHourlyForecast(latitude: Double, longitude: Double): Flow<Resource<List<HourlyForecast>>> {
            return flowOf(Resource.Success(emptyList()))
        }
        override fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>> {
            return flowOf(Resource.Success(listOf(sampleDailyForecast())))
        }
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
        override suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String?) = false
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        ActiveLocationManager.setActiveLocation(ActiveLocationManager.defaultLocation)
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

    @Test
    fun repositoryWeatherValues_areDisplayedExactly() = runTest(testDispatcher) {
        val repo = object : WeatherRepository {
            override fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>> {
                return flowOf(Resource.Success(sampleCurrentWeather(temp = 11.5, condition = WeatherCondition.RAIN)))
            }
            override fun observeHourlyForecast(latitude: Double, longitude: Double) = flowOf(Resource.Success(emptyList<HourlyForecast>()))
            override fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>> {
                return flowOf(Resource.Success(listOf(sampleDailyForecast(min = 4.0, max = 15.0, condition = WeatherCondition.RAIN))))
            }
            override suspend fun refreshWeather(latitude: Double, longitude: Double) = Resource.Success(Unit)
            override fun observeFavoriteLocations() = flowOf(listOf(
                WeatherLocation("loc_tokyo", "Tokyo", "Nhật Bản", "Tokyo", 35.6762, 139.6503)
            ))
            override suspend fun addFavoriteLocation(location: WeatherLocation) = Resource.Success(Unit)
            override suspend fun removeFavoriteLocation(locationId: String) = Resource.Success(Unit)
            override suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String?) = false
        }

        val viewModel = FavoritesViewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected FavoritesUiState.Success, got $state", state is FavoritesUiState.Success)
        val success = state as FavoritesUiState.Success
        assertEquals(1, success.favoritesList.size)
        val item = success.favoritesList[0]
        assertEquals(11.5, item.temperature, 0.001)
        assertEquals(WeatherCondition.RAIN, item.condition)
        assertEquals(4.0, item.minTemp, 0.001)
        assertEquals(15.0, item.maxTemp, 0.001)
    }

    @Test
    fun differentCityNames_doNotProduceFabricatedMeasurements() = runTest(testDispatcher) {
        val cities = listOf(
            WeatherLocation("loc_tokyo", "Tokyo", "Nhật Bản", "Tokyo", 35.6762, 139.6503),
            WeatherLocation("loc_paris", "Paris", "Pháp", "Île-de-France", 48.8566, 2.3522),
            WeatherLocation("loc_ny", "New York", "Mỹ", "New York", 40.7128, -74.0060),
            WeatherLocation("loc_danang", "Đà Nẵng", "Việt Nam", "Đà Nẵng", 16.0544, 108.2022)
        )

        val repo = object : WeatherRepository {
            override fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>> {
                return flowOf(Resource.Success(sampleCurrentWeather(temp = 11.5, condition = WeatherCondition.RAIN)))
            }
            override fun observeHourlyForecast(latitude: Double, longitude: Double) = flowOf(Resource.Success(emptyList<HourlyForecast>()))
            override fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>> {
                return flowOf(Resource.Success(listOf(sampleDailyForecast(min = 4.0, max = 15.0, condition = WeatherCondition.RAIN))))
            }
            override suspend fun refreshWeather(latitude: Double, longitude: Double) = Resource.Success(Unit)
            override fun observeFavoriteLocations() = flowOf(cities)
            override suspend fun addFavoriteLocation(location: WeatherLocation) = Resource.Success(Unit)
            override suspend fun removeFavoriteLocation(locationId: String) = Resource.Success(Unit)
            override suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String?) = false
        }

        val viewModel = FavoritesViewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected FavoritesUiState.Success, got $state", state is FavoritesUiState.Success)
        val success = state as FavoritesUiState.Success
        assertEquals(4, success.favoritesList.size)
        for (item in success.favoritesList) {
            assertEquals("City ${item.location.name} must have repo temperature", 11.5, item.temperature, 0.001)
            assertEquals("City ${item.location.name} must have repo condition", WeatherCondition.RAIN, item.condition)
            assertEquals("City ${item.location.name} must have repo minTemp", 4.0, item.minTemp, 0.001)
            assertEquals("City ${item.location.name} must have repo maxTemp", 15.0, item.maxTemp, 0.001)
        }
    }

    @Test
    fun partialFailure_keepsSuccessfulCardsAndReportsRetryableFailure() = runTest(testDispatcher) {
        val cities = listOf(
            WeatherLocation("loc_tokyo", "Tokyo", "Nhật Bản", "Tokyo", 35.6762, 139.6503),
            WeatherLocation("loc_paris", "Paris", "Pháp", "Île-de-France", 48.8566, 2.3522)
        )

        val repo = object : WeatherRepository {
            override fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>> {
                return if (latitude == 35.6762) {
                    flowOf(Resource.Success(sampleCurrentWeather(temp = 19.5, condition = WeatherCondition.CLEAR)))
                } else {
                    flowOf(Resource.Error("Network failure for Paris"))
                }
            }
            override fun observeHourlyForecast(latitude: Double, longitude: Double) = flowOf(Resource.Success(emptyList<HourlyForecast>()))
            override fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>> {
                return if (latitude == 35.6762) {
                    flowOf(Resource.Success(listOf(sampleDailyForecast(min = 14.0, max = 22.0))))
                } else {
                    flowOf(Resource.Error("Network failure for Paris"))
                }
            }
            override suspend fun refreshWeather(latitude: Double, longitude: Double) = Resource.Success(Unit)
            override fun observeFavoriteLocations() = flowOf(cities)
            override suspend fun addFavoriteLocation(location: WeatherLocation) = Resource.Success(Unit)
            override suspend fun removeFavoriteLocation(locationId: String) = Resource.Success(Unit)
            override suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String?) = false
        }

        val viewModel = FavoritesViewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected FavoritesUiState.Success, got $state", state is FavoritesUiState.Success)
        val success = state as FavoritesUiState.Success
        // Tokyo succeeds, Paris fails
        assertEquals(1, success.favoritesList.size)
        assertEquals("Tokyo", success.favoritesList[0].location.name)
        assertTrue("Partial failure must be reported", success.hasPartialError)
    }

    @Test
    fun allLocationsFail_exposesErrorWithoutFakeCards() = runTest(testDispatcher) {
        val cities = listOf(
            WeatherLocation("loc_tokyo", "Tokyo", "Nhật Bản", "Tokyo", 35.6762, 139.6503)
        )

        val repo = object : WeatherRepository {
            override fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>> {
                return flowOf(Resource.Error("Offline"))
            }
            override fun observeHourlyForecast(latitude: Double, longitude: Double) = flowOf(Resource.Success(emptyList<HourlyForecast>()))
            override fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>> {
                return flowOf(Resource.Error("Offline"))
            }
            override suspend fun refreshWeather(latitude: Double, longitude: Double) = Resource.Error("Offline")
            override fun observeFavoriteLocations() = flowOf(cities)
            override suspend fun addFavoriteLocation(location: WeatherLocation) = Resource.Success(Unit)
            override suspend fun removeFavoriteLocation(locationId: String) = Resource.Success(Unit)
            override suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String?) = false
        }

        val viewModel = FavoritesViewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected FavoritesUiState.Error, got $state", state is FavoritesUiState.Error)
    }

    @Test
    fun favoritesChange_cancelsObsoleteLocationWork() = runTest(testDispatcher) {
        val favFlow = MutableStateFlow(listOf(
            WeatherLocation("loc_tokyo", "Tokyo", "Nhật Bản", "Tokyo", 35.6762, 139.6503),
            WeatherLocation("loc_paris", "Paris", "Pháp", "Île-de-France", 48.8566, 2.3522)
        ))

        val parisCurrentFlow = MutableSharedFlow<Resource<CurrentWeather>>()
        val parisDailyFlow = MutableSharedFlow<Resource<List<DailyForecast>>>()

        val repo = object : WeatherRepository {
            override fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>> {
                return if (latitude == 48.8566) parisCurrentFlow
                else flowOf(Resource.Success(sampleCurrentWeather(temp = 19.0)))
            }
            override fun observeHourlyForecast(latitude: Double, longitude: Double) = flowOf(Resource.Success(emptyList<HourlyForecast>()))
            override fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>> {
                return if (latitude == 48.8566) parisDailyFlow
                else flowOf(Resource.Success(listOf(sampleDailyForecast(min = 14.0, max = 24.0))))
            }
            override suspend fun refreshWeather(latitude: Double, longitude: Double) = Resource.Success(Unit)
            override fun observeFavoriteLocations() = favFlow
            override suspend fun addFavoriteLocation(location: WeatherLocation) = Resource.Success(Unit)
            override suspend fun removeFavoriteLocation(locationId: String) = Resource.Success(Unit)
            override suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String?) = false
        }

        val viewModel = FavoritesViewModel(repo)
        advanceUntilIdle()

        // Now remove Paris from favorites before Paris emits
        favFlow.value = listOf(
            WeatherLocation("loc_tokyo", "Tokyo", "Nhật Bản", "Tokyo", 35.6762, 139.6503)
        )
        advanceUntilIdle()

        // Emit weather for Paris on old pending flow
        parisCurrentFlow.emit(Resource.Success(sampleCurrentWeather(temp = 22.0)))
        parisDailyFlow.emit(Resource.Success(listOf(sampleDailyForecast(min = 16.0, max = 26.0))))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FavoritesUiState.Success)
        val success = state as FavoritesUiState.Success
        assertEquals(1, success.favoritesList.size)
        assertEquals("Tokyo", success.favoritesList[0].location.name)
    }

    @Test
    fun activeLocationChange_cannotBeOverwrittenByOldResult() = runTest(testDispatcher) {
        val activeA = WeatherLocation("loc_a", "City A", "Country A", null, 10.0, 10.0)
        val activeB = WeatherLocation("loc_b", "City B", "Country B", null, 20.0, 20.0)

        ActiveLocationManager.setActiveLocation(activeA)

        val flowA = MutableSharedFlow<Resource<CurrentWeather>>()
        val flowB = MutableStateFlow<Resource<CurrentWeather>>(Resource.Success(sampleCurrentWeather(temp = 30.0)))

        val repo = object : WeatherRepository {
            override fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>> {
                return if (latitude == 10.0) flowA
                else flowB
            }
            override fun observeHourlyForecast(latitude: Double, longitude: Double) = flowOf(Resource.Success(emptyList<HourlyForecast>()))
            override fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>> {
                return flowOf(Resource.Success(listOf(sampleDailyForecast())))
            }
            override suspend fun refreshWeather(latitude: Double, longitude: Double) = Resource.Success(Unit)
            override fun observeFavoriteLocations() = flowOf(emptyList<WeatherLocation>())
            override suspend fun addFavoriteLocation(location: WeatherLocation) = Resource.Success(Unit)
            override suspend fun removeFavoriteLocation(locationId: String) = Resource.Success(Unit)
            override suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String?) = false
        }

        val viewModel = FavoritesViewModel(repo)
        advanceUntilIdle()

        // Switch active location to B while A is pending
        ActiveLocationManager.setActiveLocation(activeB)
        advanceUntilIdle()

        // Emit old A result late
        flowA.emit(Resource.Success(sampleCurrentWeather(temp = 15.0)))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FavoritesUiState.Success)
        val success = state as FavoritesUiState.Success
        assertNotNull(success.currentLocation)
        assertEquals("City B", success.currentLocation?.location?.name)
        assertEquals(30.0, success.currentLocation!!.temperature, 0.001)
    }

    @Test
    fun activeLocationAlsoFavorite_isLoadedOnlyOnce() = runTest(testDispatcher) {
        val hanoi = WeatherLocation("vn_hanoi", "Hà Nội", "Việt Nam", "Thủ đô Hà Nội", 21.0285, 105.8542)
        val tokyo = WeatherLocation("loc_tokyo", "Tokyo", "Nhật Bản", "Tokyo", 35.6762, 139.6503)

        ActiveLocationManager.setActiveLocation(hanoi)

        val currentWeatherCalls = mutableListOf<Pair<Double, Double>>()
        val dailyForecastCalls = mutableListOf<Pair<Double, Double>>()

        val repo = object : WeatherRepository {
            override fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>> {
                currentWeatherCalls.add(latitude to longitude)
                return flowOf(Resource.Success(sampleCurrentWeather(temp = 28.0)))
            }
            override fun observeHourlyForecast(latitude: Double, longitude: Double) = flowOf(Resource.Success(emptyList<HourlyForecast>()))
            override fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>> {
                dailyForecastCalls.add(latitude to longitude)
                return flowOf(Resource.Success(listOf(sampleDailyForecast())))
            }
            override suspend fun refreshWeather(latitude: Double, longitude: Double) = Resource.Success(Unit)
            override fun observeFavoriteLocations() = flowOf(listOf(hanoi, tokyo))
            override suspend fun addFavoriteLocation(location: WeatherLocation) = Resource.Success(Unit)
            override suspend fun removeFavoriteLocation(locationId: String) = Resource.Success(Unit)
            override suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String?) = false
        }

        val viewModel = FavoritesViewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FavoritesUiState.Success)
        val success = state as FavoritesUiState.Success
        assertNotNull(success.currentLocation)
        assertEquals("Hà Nội", success.currentLocation?.location?.name)
        assertEquals(1, success.favoritesList.size)
        assertEquals("Tokyo", success.favoritesList[0].location.name)
        assertFalse("Hanoi must not be duplicated in favoritesList", success.favoritesList.any { it.location.name == "Hà Nội" })

        // Count requests for Hanoi coordinates
        val hanoiCurrentCalls = currentWeatherCalls.count { it.first == 21.0285 && it.second == 105.8542 }
        val hanoiDailyCalls = dailyForecastCalls.count { it.first == 21.0285 && it.second == 105.8542 }

        assertEquals("Hanoi currentWeather must only be requested once", 1, hanoiCurrentCalls)
        assertEquals("Hanoi dailyForecast must only be requested once", 1, hanoiDailyCalls)
    }

    @Test
    fun duplicateFavoritesWithDifferentIds_emitsOnlyOneCardAndOneRequest() = runTest(testDispatcher) {
        val fav1 = WeatherLocation("fav_1", "Hanoi Center", "Việt Nam", null, 21.0285, 105.8542)
        val fav2 = WeatherLocation("fav_2", "Hanoi Old Quarter", "Việt Nam", null, 21.0285, 105.8542)
        val active = WeatherLocation("loc_tokyo", "Tokyo", "Nhật Bản", "Tokyo", 35.6762, 139.6503)

        ActiveLocationManager.setActiveLocation(active)

        val currentWeatherCalls = mutableListOf<Pair<Double, Double>>()
        val dailyForecastCalls = mutableListOf<Pair<Double, Double>>()

        val repo = object : WeatherRepository {
            override fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>> {
                currentWeatherCalls.add(latitude to longitude)
                return flowOf(Resource.Success(sampleCurrentWeather(temp = 25.0)))
            }
            override fun observeHourlyForecast(latitude: Double, longitude: Double) = flowOf(Resource.Success(emptyList<HourlyForecast>()))
            override fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>> {
                dailyForecastCalls.add(latitude to longitude)
                return flowOf(Resource.Success(listOf(sampleDailyForecast(min = 20.0, max = 30.0))))
            }
            override suspend fun refreshWeather(latitude: Double, longitude: Double) = Resource.Success(Unit)
            override fun observeFavoriteLocations() = flowOf(listOf(fav1, fav2))
            override suspend fun addFavoriteLocation(location: WeatherLocation) = Resource.Success(Unit)
            override suspend fun removeFavoriteLocation(locationId: String) = Resource.Success(Unit)
            override suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String?) = false
        }

        val viewModel = FavoritesViewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FavoritesUiState.Success)
        val success = state as FavoritesUiState.Success
        assertEquals("Only one favorite card should be produced for duplicate coordinates", 1, success.favoritesList.size)

        val hanoiCurrentCalls = currentWeatherCalls.count { it.first == 21.0285 && it.second == 105.8542 }
        val hanoiDailyCalls = dailyForecastCalls.count { it.first == 21.0285 && it.second == 105.8542 }
        assertEquals(1, hanoiCurrentCalls)
        assertEquals(1, hanoiDailyCalls)
    }

    @Test
    fun currentSuccess_dailyError_doesNotCreateFakeCard() = runTest(testDispatcher) {
        val hanoi = WeatherLocation("vn_hanoi", "Hà Nội", "Việt Nam", null, 21.0285, 105.8542)
        ActiveLocationManager.setActiveLocation(hanoi)

        val repo = object : WeatherRepository {
            override fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>> {
                return flowOf(Resource.Success(sampleCurrentWeather(temp = 25.0)))
            }
            override fun observeHourlyForecast(latitude: Double, longitude: Double) = flowOf(Resource.Success(emptyList<HourlyForecast>()))
            override fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>> {
                return flowOf(Resource.Error("Daily forecast error"))
            }
            override suspend fun refreshWeather(latitude: Double, longitude: Double) = Resource.Success(Unit)
            override fun observeFavoriteLocations() = flowOf(emptyList<WeatherLocation>())
            override suspend fun addFavoriteLocation(location: WeatherLocation) = Resource.Success(Unit)
            override suspend fun removeFavoriteLocation(locationId: String) = Resource.Success(Unit)
            override suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String?) = false
        }

        val viewModel = FavoritesViewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("When daily forecast fails, location must fail without creating fake card. State: $state", state is FavoritesUiState.Error)
    }

    @Test
    fun currentSuccess_dailyEmpty_doesNotCreateFakeCard() = runTest(testDispatcher) {
        val hanoi = WeatherLocation("vn_hanoi", "Hà Nội", "Việt Nam", null, 21.0285, 105.8542)
        ActiveLocationManager.setActiveLocation(hanoi)

        val repo = object : WeatherRepository {
            override fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>> {
                return flowOf(Resource.Success(sampleCurrentWeather(temp = 25.0)))
            }
            override fun observeHourlyForecast(latitude: Double, longitude: Double) = flowOf(Resource.Success(emptyList<HourlyForecast>()))
            override fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>> {
                return flowOf(Resource.Success(emptyList()))
            }
            override suspend fun refreshWeather(latitude: Double, longitude: Double) = Resource.Success(Unit)
            override fun observeFavoriteLocations() = flowOf(emptyList<WeatherLocation>())
            override suspend fun addFavoriteLocation(location: WeatherLocation) = Resource.Success(Unit)
            override suspend fun removeFavoriteLocation(locationId: String) = Resource.Success(Unit)
            override suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String?) = false
        }

        val viewModel = FavoritesViewModel(repo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("When daily forecast is empty, location must fail without creating fake card. State: $state", state is FavoritesUiState.Error)
    }

    @Test
    fun currentSuccess_dailyLoading_waitsUntilDailySuccess() = runTest(testDispatcher) {
        val hanoi = WeatherLocation("vn_hanoi", "Hà Nội", "Việt Nam", null, 21.0285, 105.8542)
        ActiveLocationManager.setActiveLocation(hanoi)

        val dailyFlow = MutableStateFlow<Resource<List<DailyForecast>>>(Resource.Loading)

        val repo = object : WeatherRepository {
            override fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>> {
                return flowOf(Resource.Success(sampleCurrentWeather(temp = 25.0)))
            }
            override fun observeHourlyForecast(latitude: Double, longitude: Double) = flowOf(Resource.Success(emptyList<HourlyForecast>()))
            override fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>> {
                return dailyFlow
            }
            override suspend fun refreshWeather(latitude: Double, longitude: Double) = Resource.Success(Unit)
            override fun observeFavoriteLocations() = flowOf(emptyList<WeatherLocation>())
            override suspend fun addFavoriteLocation(location: WeatherLocation) = Resource.Success(Unit)
            override suspend fun removeFavoriteLocation(locationId: String) = Resource.Success(Unit)
            override suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String?) = false
        }

        val viewModel = FavoritesViewModel(repo)
        advanceUntilIdle()

        assertEquals(FavoritesUiState.Loading, viewModel.uiState.value)

        // Now daily succeeds
        dailyFlow.value = Resource.Success(listOf(sampleDailyForecast(min = 19.0, max = 29.0)))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is FavoritesUiState.Success)
        val success = state as FavoritesUiState.Success
        assertNotNull(success.currentLocation)
        assertEquals(19.0, success.currentLocation?.minTemp ?: 0.0, 0.001)
        assertEquals(29.0, success.currentLocation?.maxTemp ?: 0.0, 0.001)
    }

    @Test
    fun retry_restartsSafely() = runTest(testDispatcher) {
        var shouldFail = true

        val repo = object : WeatherRepository {
            override fun observeCurrentWeather(latitude: Double, longitude: Double): Flow<Resource<CurrentWeather>> {
                return flowOf(
                    if (shouldFail) Resource.Error("Initial failure")
                    else Resource.Success(sampleCurrentWeather(temp = 25.0))
                )
            }
            override fun observeHourlyForecast(latitude: Double, longitude: Double) = flowOf(Resource.Success(emptyList<HourlyForecast>()))
            override fun observeDailyForecast(latitude: Double, longitude: Double): Flow<Resource<List<DailyForecast>>> {
                return flowOf(
                    if (shouldFail) Resource.Error("Initial failure")
                    else Resource.Success(listOf(sampleDailyForecast()))
                )
            }
            override suspend fun refreshWeather(latitude: Double, longitude: Double) = Resource.Success(Unit)
            override fun observeFavoriteLocations() = flowOf(listOf(
                WeatherLocation("loc_tokyo", "Tokyo", "Nhật Bản", "Tokyo", 35.6762, 139.6503)
            ))
            override suspend fun addFavoriteLocation(location: WeatherLocation) = Resource.Success(Unit)
            override suspend fun removeFavoriteLocation(locationId: String) = Resource.Success(Unit)
            override suspend fun isFavoriteLocation(latitude: Double, longitude: Double, name: String?) = false
        }

        val viewModel = FavoritesViewModel(repo)
        advanceUntilIdle()

        assertTrue("Initial state must be Error", viewModel.uiState.value is FavoritesUiState.Error)

        // Fix the failure and trigger retry
        shouldFail = false
        viewModel.retry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State after retry must be Success, got $state", state is FavoritesUiState.Success)
        val success = state as FavoritesUiState.Success
        assertEquals(1, success.favoritesList.size)
        assertEquals(25.0, success.favoritesList[0].temperature, 0.001)
    }
}

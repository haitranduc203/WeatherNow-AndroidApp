package com.example.weathernow.data.location

import android.Manifest
import android.app.Application
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import com.example.weathernow.WeatherNowApp
import com.example.weathernow.core.common.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = WeatherNowApp::class, sdk = [34])
class AndroidDeviceLocationDataSourceTest {

    private lateinit var app: Application
    private val mockFusedClient = mockk<FusedLocationProviderClient>(relaxed = true)
    private val fixedNow = 1_000_000_000L

    private fun createDataSource(
        validator: LocationFreshnessValidator = LocationFreshnessValidator(clock = { fixedNow }),
        timeoutMillis: Long = 15_000L
    ): AndroidDeviceLocationDataSource {
        return AndroidDeviceLocationDataSource(
            context = app,
            fusedClient = mockFusedClient,
            freshnessValidator = validator,
            timeoutMillis = timeoutMillis
        )
    }

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        shadowOf(app).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    @Test
    fun permissionDenied_returnsResourceError() = runTest {
        shadowOf(app).denyPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val dataSource = createDataSource()

        val result = dataSource.getCurrentCoordinates()
        assertTrue("Must be Resource.Error when permission missing", result is Resource.Error)
        assertEquals("Location permission is required", (result as Resource.Error).message)
    }

    @Test
    fun freshLastLocation_returnsSuccessWithoutCallingCurrentLocation() = runTest {
        val lastLoc = Location("gps").apply {
            latitude = 10.8231
            longitude = 106.6297
            time = fixedNow - 60_000L // 1 minute old
        }
        every { mockFusedClient.lastLocation } returns Tasks.forResult(lastLoc)

        val dataSource = createDataSource()
        val result = dataSource.getCurrentCoordinates()

        assertTrue("Expected Resource.Success, got $result", result is Resource.Success)
        val data = (result as Resource.Success).data
        assertEquals(10.8231, data.latitude, 0.0001)
        assertEquals(106.6297, data.longitude, 0.0001)
        verify(exactly = 0) { mockFusedClient.getCurrentLocation(any<Int>(), any<CancellationToken>()) }
    }

    @Test
    fun staleLastLocation_requestsCurrentLocation() = runTest {
        val staleLoc = Location("gps").apply {
            latitude = 21.0285
            longitude = 105.8542
            time = fixedNow - (6 * 60 * 1000L) // 6 minutes old (stale)
        }
        val currentLoc = Location("fused").apply {
            latitude = 10.8231
            longitude = 106.6297
            time = fixedNow
        }
        every { mockFusedClient.lastLocation } returns Tasks.forResult(staleLoc)
        every { mockFusedClient.getCurrentLocation(any<Int>(), any<CancellationToken>()) } returns Tasks.forResult(currentLoc)

        val dataSource = createDataSource()
        val result = dataSource.getCurrentCoordinates()

        assertTrue("Expected Resource.Success, got $result", result is Resource.Success)
        val data = (result as Resource.Success).data
        assertEquals(10.8231, data.latitude, 0.0001)
        assertEquals(106.6297, data.longitude, 0.0001)
        verify(exactly = 1) { mockFusedClient.getCurrentLocation(any<Int>(), any<CancellationToken>()) }
    }

    @Test
    fun nullCurrentLocation_returnsActionableResourceError() = runTest {
        every { mockFusedClient.lastLocation } returns Tasks.forResult<Location?>(null)
        every { mockFusedClient.getCurrentLocation(any<Int>(), any<CancellationToken>()) } returns Tasks.forResult<Location?>(null)

        val dataSource = createDataSource()
        val result = dataSource.getCurrentCoordinates()

        assertTrue("Expected Resource.Error, got $result", result is Resource.Error)
        val message = (result as Resource.Error).message
        assertTrue("Error message should be actionable", message.contains("Unable to obtain device location"))
    }

    @Test
    fun currentLocationTimeout_returnsActionableResourceError() = runTest {
        val taskSource = TaskCompletionSource<Location>()
        every { mockFusedClient.lastLocation } returns Tasks.forResult<Location?>(null)
        every { mockFusedClient.getCurrentLocation(any<Int>(), any<CancellationToken>()) } answers {
            val token = secondArg<CancellationToken>()
            token.onCanceledRequested {
                taskSource.trySetException(CancellationException("Task cancelled"))
            }
            taskSource.task
        }

        val dataSource = createDataSource(timeoutMillis = 50L) // Short timeout for test
        val result = dataSource.getCurrentCoordinates()
        advanceUntilIdle()

        assertTrue("Expected Resource.Error on timeout, got $result", result is Resource.Error)
        val message = (result as Resource.Error).message
        assertTrue("Error message should be actionable", message.contains("Unable to obtain device location"))
    }

    @Test
    fun coroutineCancellation_rethrowsCancellationException() = runTest {
        val taskSource = TaskCompletionSource<Location>()
        every { mockFusedClient.lastLocation } returns Tasks.forResult<Location?>(null)
        every { mockFusedClient.getCurrentLocation(any<Int>(), any<CancellationToken>()) } answers {
            val token = secondArg<CancellationToken>()
            token.onCanceledRequested {
                taskSource.trySetException(CancellationException("Task cancelled"))
            }
            taskSource.task
        }

        val dataSource = createDataSource(timeoutMillis = 60_000L)

        var thrownCancellation: Throwable? = null
        val job = launch {
            try {
                dataSource.getCurrentCoordinates()
            } catch (c: CancellationException) {
                thrownCancellation = c
                throw c
            }
        }

        advanceTimeBy(100L)
        job.cancelAndJoin()
        advanceUntilIdle()

        assertNotNull("CancellationException must be thrown on coroutine cancellation", thrownCancellation)
        assertTrue(thrownCancellation is CancellationException)
    }
}

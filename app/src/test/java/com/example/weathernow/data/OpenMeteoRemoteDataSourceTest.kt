package com.example.weathernow.data

import com.example.weathernow.core.network.NetworkError
import com.example.weathernow.data.mapper.ErrorMapper
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import com.example.weathernow.data.remote.api.OpenMeteoForecastApi
import com.example.weathernow.data.remote.api.OpenMeteoGeocodingApi
import com.example.weathernow.data.remote.datasource.OpenMeteoRemoteDataSourceImpl
import com.example.weathernow.data.remote.dto.OpenMeteoForecastDto
import com.example.weathernow.data.remote.dto.OpenMeteoGeocodingDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class OpenMeteoRemoteDataSourceTest {

    private class FakeForecastApi(private val exceptionToThrow: Throwable) : OpenMeteoForecastApi {
        override suspend fun getForecast(
            latitude: Double,
            longitude: Double,
            current: String,
            hourly: String,
            daily: String,
            timezone: String,
            forecastDays: Int,
            temperatureUnit: String,
            windSpeedUnit: String
        ): OpenMeteoForecastDto {
            throw exceptionToThrow
        }
    }

    private class FakeGeocodingApi(private val exceptionToThrow: Throwable) : OpenMeteoGeocodingApi {
        override suspend fun searchLocations(
            name: String,
            count: Int,
            language: String,
            format: String
        ): OpenMeteoGeocodingDto {
            throw exceptionToThrow
        }
    }

    @Test
    fun `getForecast propagates CancellationException unchanged`() = runTest {
        val expectedCancellation = CancellationException("Forecast coroutine cancelled")
        val fakeForecastApi = FakeForecastApi(expectedCancellation)
        val fakeGeocodingApi = FakeGeocodingApi(RuntimeException("Unused"))
        val dataSource = OpenMeteoRemoteDataSourceImpl(
            forecastApi = fakeForecastApi,
            geocodingApi = fakeGeocodingApi
        )

        var caught: Throwable? = null
        try {
            dataSource.getForecast(latitude = 21.0285, longitude = 105.8542)
        } catch (e: Throwable) {
            caught = e
        }

        assertSame(expectedCancellation, caught)
    }

    @Test
    fun `searchLocations propagates CancellationException unchanged`() = runTest {
        val expectedCancellation = CancellationException("Geocoding coroutine cancelled")
        val fakeForecastApi = FakeForecastApi(RuntimeException("Unused"))
        val fakeGeocodingApi = FakeGeocodingApi(expectedCancellation)
        val dataSource = OpenMeteoRemoteDataSourceImpl(
            forecastApi = fakeForecastApi,
            geocodingApi = fakeGeocodingApi
        )

        var caught: Throwable? = null
        try {
            dataSource.searchLocations(name = "Hanoi")
        } catch (e: Throwable) {
            caught = e
        }

        assertSame(expectedCancellation, caught)
    }

    @Test
    fun `mapThrowableToNetworkError maps UnknownHostException to NetworkUnavailable`() {
        val error = ErrorMapper.mapThrowableToNetworkError(UnknownHostException("Unable to resolve host"))
        assertTrue(error is NetworkError.NetworkUnavailable)
    }

    @Test
    fun `mapThrowableToNetworkError maps SocketTimeoutException to Timeout`() {
        val error = ErrorMapper.mapThrowableToNetworkError(SocketTimeoutException("Timeout"))
        assertTrue(error is NetworkError.Timeout)
    }

    @Test
    fun `mapThrowableToNetworkError maps IOException to NetworkUnavailable`() {
        val error = ErrorMapper.mapThrowableToNetworkError(IOException("Connection reset"))
        assertTrue(error is NetworkError.NetworkUnavailable)
    }

    @Test
    fun `mapThrowableToNetworkError maps SerializationException to DataParsingError`() {
        val error = ErrorMapper.mapThrowableToNetworkError(SerializationException("Invalid JSON"))
        assertTrue(error is NetworkError.DataParsingError)
    }

    @Test
    fun `mapThrowableToNetworkError maps HTTP 400 to InvalidRequest`() {
        val response = Response.error<String>(400, "Bad Request".toResponseBody("application/json".toMediaType()))
        val error = ErrorMapper.mapThrowableToNetworkError(HttpException(response))
        assertTrue(error is NetworkError.InvalidRequest)
    }

    @Test
    fun `mapThrowableToNetworkError maps HTTP 429 to RateLimited`() {
        val response = Response.error<String>(429, "Too Many Requests".toResponseBody("application/json".toMediaType()))
        val error = ErrorMapper.mapThrowableToNetworkError(HttpException(response))
        assertTrue(error is NetworkError.RateLimited)
    }

    @Test
    fun `mapThrowableToNetworkError maps HTTP 500 to ServerError`() {
        val response = Response.error<String>(500, "Internal Server Error".toResponseBody("application/json".toMediaType()))
        val error = ErrorMapper.mapThrowableToNetworkError(HttpException(response))
        assertTrue(error is NetworkError.ServerError)
    }
}

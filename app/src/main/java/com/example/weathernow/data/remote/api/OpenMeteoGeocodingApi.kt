package com.example.weathernow.data.remote.api

import com.example.weathernow.data.remote.dto.OpenMeteoGeocodingDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoGeocodingApi {

    @GET("v1/search")
    suspend fun searchLocations(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): OpenMeteoGeocodingDto
}

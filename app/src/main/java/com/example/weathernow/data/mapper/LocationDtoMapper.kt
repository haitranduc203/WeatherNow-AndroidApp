package com.example.weathernow.data.mapper

import com.example.weathernow.data.remote.dto.OpenMeteoLocationDto
import com.example.weathernow.domain.model.WeatherLocation

object LocationDtoMapper {

    fun mapToDomain(dto: OpenMeteoLocationDto): WeatherLocation {
        val admin = listOfNotNull(dto.admin1, dto.admin2, dto.admin3).firstOrNull()
        return WeatherLocation(
            id = dto.id.toString(),
            name = dto.name,
            country = dto.country ?: dto.countryCode,
            adminArea = admin,
            latitude = dto.latitude,
            longitude = dto.longitude,
            timezone = dto.timezone,
            isFavorite = false
        )
    }

    fun mapListToDomain(dtos: List<OpenMeteoLocationDto>?): List<WeatherLocation> {
        return dtos?.map { mapToDomain(it) } ?: emptyList()
    }
}

package com.example.weathernow.presentation.util

import com.example.weathernow.domain.model.WeatherLocation

/**
 * Returns the presentation-layer localized display name for a [WeatherLocation].
 *
 * Device locations (identified by a "device_" prefix in their ID or the legacy "Current location" name)
 * are mapped to [AppStringResources.currentPosition] (e.g. "Vị trí hiện tại" or "Current Location").
 * Normal locations (e.g. "Tokyo", "Paris", "Hà Nội") preserve their original catalog/API name.
 */
fun WeatherLocation.getDisplayName(strings: AppStringResources): String {
    return getLocalizedLocationName(name, id, strings)
}

/**
 * Returns the localized location name given a raw [locationName] and optional [locationId].
 */
fun getLocalizedLocationName(
    locationName: String,
    locationId: String?,
    strings: AppStringResources
): String {
    return if (locationId?.startsWith("device_") == true || locationName.equals("Current location", ignoreCase = true)) {
        strings.currentPosition
    } else {
        locationName
    }
}

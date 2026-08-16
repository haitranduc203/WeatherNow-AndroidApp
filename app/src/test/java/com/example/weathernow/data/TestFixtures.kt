package com.example.weathernow.data

object TestFixtures {

    val FORECAST_JSON = """
    {
      "latitude": 21.0285,
      "longitude": 105.8542,
      "generationtime_ms": 0.12,
      "utc_offset_seconds": 25200,
      "timezone": "Asia/Ho_Chi_Minh",
      "timezone_abbreviation": "+07",
      "elevation": 14.0,
      "current": {
        "time": "2026-08-16T09:30",
        "interval": 900,
        "temperature_2m": 28.4,
        "relative_humidity_2m": 68,
        "apparent_temperature": 31.2,
        "weather_code": 1,
        "wind_speed_10m": 12.5,
        "wind_direction_10m": 45,
        "precipitation": 0.0,
        "surface_pressure": 1012.8,
        "uv_index": 5.2
      },
      "hourly": {
        "time": [
          "2026-08-16T09:00",
          "2026-08-16T10:00",
          "2026-08-16T11:00"
        ],
        "temperature_2m": [28.0, 29.5, 31.0],
        "relative_humidity_2m": [70, 65, 60],
        "apparent_temperature": [30.5, 32.5, 35.0],
        "precipitation_probability": [0, 10, 20],
        "precipitation": [0.0, 0.0, 0.2],
        "weather_code": [1, 2, 3],
        "surface_pressure": [1013.0, 1012.5, 1012.0],
        "wind_speed_10m": [12.0, 14.0, 15.0],
        "uv_index": [4.0, 6.0, 8.0]
      },
      "daily": {
        "time": [
          "2026-08-16",
          "2026-08-17"
        ],
        "weather_code": [1, 61],
        "temperature_2m_max": [33.5, 30.0],
        "temperature_2m_min": [25.0, 24.5],
        "apparent_temperature_max": [38.0, 34.0],
        "apparent_temperature_min": [27.0, 26.0],
        "precipitation_probability_max": [20, 80],
        "precipitation_sum": [0.5, 12.0],
        "sunrise": ["2026-08-16T05:35", "2026-08-17T05:35"],
        "sunset": ["2026-08-16T18:25", "2026-08-17T18:24"],
        "uv_index_max": [9.0, 5.0],
        "wind_speed_10m_max": [18.0, 22.0]
      }
    }
    """.trimIndent()

    val GEOCODING_JSON = """
    {
      "results": [
        {
          "id": 1581130,
          "name": "Hanoi",
          "latitude": 21.02817,
          "longitude": 105.85417,
          "elevation": 15.0,
          "feature_code": "PPLC",
          "country_code": "VN",
          "country": "Vietnam",
          "admin1": "Ha Noi",
          "timezone": "Asia/Ho_Chi_Minh",
          "population": 8435700
        },
        {
          "id": 1850147,
          "name": "Tokyo",
          "latitude": 35.6895,
          "longitude": 139.69171,
          "elevation": 40.0,
          "feature_code": "PPLC",
          "country_code": "JP",
          "country": "Japan",
          "admin1": "Tokyo",
          "timezone": "Asia/Tokyo",
          "population": 13960000
        }
      ],
      "generationtime_ms": 0.45
    }
    """.trimIndent()
}

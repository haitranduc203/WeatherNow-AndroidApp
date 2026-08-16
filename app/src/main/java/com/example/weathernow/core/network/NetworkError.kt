package com.example.weathernow.core.network

sealed class NetworkError(val messageResId: String, cause: Throwable? = null) : Exception(messageResId, cause) {
    class NetworkUnavailable(cause: Throwable? = null) : NetworkError("Network is unavailable", cause)
    class Timeout(cause: Throwable? = null) : NetworkError("Request timed out", cause)
    class InvalidRequest(cause: Throwable? = null) : NetworkError("Invalid request", cause)
    class Unauthorized(cause: Throwable? = null) : NetworkError("Unauthorized access", cause)
    class NotFound(cause: Throwable? = null) : NetworkError("Resource not found", cause)
    class RateLimited(cause: Throwable? = null) : NetworkError("Rate limit exceeded", cause)
    class ServerError(cause: Throwable? = null) : NetworkError("Server error", cause)
    class DataParsingError(cause: Throwable? = null) : NetworkError("Data parsing error", cause)
    class Unknown(cause: Throwable? = null) : NetworkError("Unknown error occurred", cause)
}

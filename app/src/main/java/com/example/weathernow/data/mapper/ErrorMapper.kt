package com.example.weathernow.data.mapper

import com.example.weathernow.core.network.NetworkError
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorMapper {

    fun mapThrowableToNetworkError(throwable: Throwable): NetworkError {
        return when (throwable) {
            is NetworkError -> throwable
            is UnknownHostException -> NetworkError.NetworkUnavailable(throwable)
            is SocketTimeoutException -> NetworkError.Timeout(throwable)
            is IOException -> NetworkError.NetworkUnavailable(throwable)
            is SerializationException -> NetworkError.DataParsingError(throwable)
            is HttpException -> {
                when (throwable.code()) {
                    400 -> NetworkError.InvalidRequest(throwable)
                    401, 403 -> NetworkError.Unauthorized(throwable)
                    404 -> NetworkError.NotFound(throwable)
                    429 -> NetworkError.RateLimited(throwable)
                    in 500..599 -> NetworkError.ServerError(throwable)
                    else -> NetworkError.Unknown(throwable)
                }
            }
            else -> NetworkError.Unknown(throwable)
        }
    }
}

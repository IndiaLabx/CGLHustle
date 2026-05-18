package com.cglhustle.core.network.error

import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.NetworkError
import com.cglhustle.core.common.error.UnknownError
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.HttpRequestTimeoutException
import java.net.ConnectException
import java.net.SocketTimeoutException
import io.ktor.http.HttpStatusCode

fun Throwable.toAppError(): AppError {
    return when (this) {
        is ResponseException -> {
            when (this.response.status) {
                HttpStatusCode.Unauthorized -> NetworkError.AuthExpired()
                HttpStatusCode.NotFound -> NetworkError.NotFound()
                HttpStatusCode.InternalServerError, HttpStatusCode.BadGateway, HttpStatusCode.ServiceUnavailable -> NetworkError.ServerOutage()
                else -> UnknownError(this)
            }
        }
        is HttpRequestTimeoutException,
        is SocketTimeoutException,
        is ConnectException -> NetworkError.Transient()
        else -> UnknownError(this)
    }
}

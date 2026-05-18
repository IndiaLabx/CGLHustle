package com.cglhustle.core.common.error

sealed interface AppResult<out T, out E : AppError>

data class Success<out T>(val data: T) : AppResult<T, Nothing>
data class Failure<out E : AppError>(val error: E) : AppResult<Nothing, E>

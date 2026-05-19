package com.cglhustle.core.ui.state

import com.cglhustle.core.common.error.AppError
import com.cglhustle.core.common.error.NetworkError
import com.cglhustle.core.common.error.StorageError
import com.cglhustle.core.common.error.UnknownError
import com.cglhustle.core.common.error.ValidationError

fun AppError.toUserFriendlyMessage(): String {
    return when (this) {
        is NetworkError.Transient -> "We are having trouble connecting. We'll keep trying in the background."
        is NetworkError.AuthExpired -> "Your session has expired. Please log in again."
        is NetworkError.NotFound -> "We couldn't find what you were looking for."
        is NetworkError.ServerOutage -> "Our servers are currently experiencing issues. Please try again later."
        is StorageError.DiskFull -> "Your device's storage is full. Please free up some space."
        is StorageError.DatabaseCorruption -> "There was a problem with your local data. Please restart the app."
        is StorageError.ConstraintViolation -> "There was an error saving your data."
        is ValidationError.MalformedId -> "We encountered an unexpected data format."
        is ValidationError.InvalidPayload -> "The data submitted was invalid."
        is UnknownError -> "An unexpected error occurred. Please try again."
        else -> "Something went wrong."
    }
}

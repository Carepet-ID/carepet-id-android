package com.android.carepet.data.response

sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val error: String?) : Result<Nothing>()
}


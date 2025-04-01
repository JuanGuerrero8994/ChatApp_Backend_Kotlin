package com.ktor.core

// core/resource/Resource.kt
sealed class Resource<T>(val data: T? = null, val message: String? = null, val exception: Throwable? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, exception: Throwable? = null) : Resource<T>(message = message, exception = exception)
    class Loading<T> : Resource<T>()
}
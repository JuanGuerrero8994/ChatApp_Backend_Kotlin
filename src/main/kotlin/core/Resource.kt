package com.ktor.core

// core/resource/Resource.kt
sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String, val exception: Throwable) : Resource<T>()
    class Loading<T> : Resource<T>()
}
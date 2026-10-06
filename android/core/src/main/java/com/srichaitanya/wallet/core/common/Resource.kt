package com.srichaitanya.wallet.core.common

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}

sealed class AppError(override val message: String) : Exception(message) {
    data class KeystoreError(override val message: String) : AppError(message)
    data class BiometricAuthFailed(override val message: String) : AppError(message)
    data class NetworkError(override val message: String) : AppError(message)
    data class VerificationFailed(override val message: String) : AppError(message)
    data class RevocationError(override val message: String) : AppError(message)
    data class StorageError(override val message: String) : AppError(message)
}

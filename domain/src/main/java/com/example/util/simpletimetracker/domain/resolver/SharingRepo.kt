package com.example.util.simpletimetracker.domain.resolver

interface SharingRepo {

    suspend fun saveBitmap(
        bitmap: Any,
        filename: String,
    ): Result

    sealed interface Result {
        data class Success(val uriString: String) : Result
        object Error : Result
    }
}
package com.example.util.simpletimetracker.navigation.params.notification

data class SnackBarParams(
    val tag: TAG? = null,
    val message: String,
    val duration: Duration = Duration.Normal,
    val dismissedListener: ((TAG?) -> Unit)? = null,
    val actionText: String = "",
    val actionListener: ((TAG?) -> Unit)? = null,
) : NotificationParams {

    enum class TAG {
        RECORD_DELETE,
        RECORDS_ALL_DELETE
    }

    sealed interface Duration {
        object Short : Duration
        object Normal : Duration
        object Long : Duration
        object Indefinite : Duration
    }
}
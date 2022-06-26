package com.example.util.simpletimetracker.navigation.params.notification

data class SnackBarParams(
    val tag: TAG? = null,
    val message: String,
    val isShortDuration: Boolean = false,
    val dismissedListener: ((TAG?) -> Unit)? = null,
    val actionText: String = "",
    val actionListener: ((TAG?) -> Unit)? = null
) : NotificationParams {

    enum class TAG {
        RECORD_DELETE,
        RECORDS_ALL_DELETE
    }
}
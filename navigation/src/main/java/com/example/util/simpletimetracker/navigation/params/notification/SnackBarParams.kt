package com.example.util.simpletimetracker.navigation.params.notification

import com.example.util.simpletimetracker.navigation.NotificationParams

data class SnackBarParams(
    val tag: TAG? = null,
    val message: String,
    val dismissedListener: ((TAG?) -> Unit)? = null,
    val actionText: String = "",
    val actionListener: ((TAG?) -> Unit)? = null
): NotificationParams {

    enum class TAG {
        RECORD_DELETE,
        RECORDS_ALL_DELETE
    }
}
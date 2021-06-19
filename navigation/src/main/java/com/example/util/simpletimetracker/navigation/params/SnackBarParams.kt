package com.example.util.simpletimetracker.navigation.params

data class SnackBarParams(
    val tag: TAG? = null,
    val message: String,
    val dismissedListener: ((TAG?) -> Unit)? = null,
    val actionText: String = "",
    val actionListener: ((TAG?) -> Unit)? = null
) {

    enum class TAG {
        RECORD_DELETE,
        RECORDS_ALL_DELETE
    }
}
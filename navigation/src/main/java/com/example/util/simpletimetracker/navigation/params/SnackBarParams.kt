package com.example.util.simpletimetracker.navigation.params

data class SnackBarParams(
    val message: String,
    val anchorToView: Boolean = true,
    val dismissedListener: (() -> Unit)? = null,
    val actionText: String = "",
    val actionListener: (() -> Unit)? = null
)
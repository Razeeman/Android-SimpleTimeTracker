package com.example.util.simpletimetracker.navigation.model

data class SnackBarMessage(
    val message: String,
    val anchorToView: Boolean = true,
    val dismissedListener: (() -> Unit)? = null,
    val actionText: String = "",
    val actionListener: (() -> Unit)? = null
)
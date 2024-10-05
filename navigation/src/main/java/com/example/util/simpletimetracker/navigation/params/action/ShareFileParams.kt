package com.example.util.simpletimetracker.navigation.params.action

data class ShareFileParams(
    val uriString: String,
    val type: String?,
    val notHandledCallback: (() -> Unit),
) : ActionParams
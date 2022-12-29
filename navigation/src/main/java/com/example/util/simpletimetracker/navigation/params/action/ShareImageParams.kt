package com.example.util.simpletimetracker.navigation.params.action

data class ShareImageParams(
    val uriString: String,
    val notHandledCallback: (() -> Unit),
) : ActionParams
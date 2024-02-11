package com.example.util.simpletimetracker.navigation.params.action

data class SendEmailParams(
    val email: String? = "",
    val subject: String? = "",
    val body: String? = "",
    val chooserTitle: String? = null,
    val notHandledCallback: (() -> Unit)?,
) : ActionParams
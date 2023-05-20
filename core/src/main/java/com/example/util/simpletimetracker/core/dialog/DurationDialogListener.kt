package com.example.util.simpletimetracker.core.dialog

interface DurationDialogListener {

    fun onDurationSet(duration: Long, tag: String? = null)

    fun onDisable(tag: String?) {}
}
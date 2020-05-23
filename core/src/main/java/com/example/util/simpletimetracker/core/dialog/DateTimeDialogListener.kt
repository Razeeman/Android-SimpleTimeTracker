package com.example.util.simpletimetracker.core.dialog

interface DateTimeDialogListener {

    fun onDateTimeSet(timestamp: Long, tag: String? = null)
}
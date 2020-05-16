package com.example.util.simpletimetracker.feature_dialogs

interface DateTimeListener {

    fun onDateTimeSet(timestamp: Long, tag: String? = null)
}
package com.example.util.simpletimetracker.core.manager

interface NotificationManager {
    fun show(params: NotificationParams)
    fun hide(id: Int)
}
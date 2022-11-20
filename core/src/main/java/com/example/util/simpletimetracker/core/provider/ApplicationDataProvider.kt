package com.example.util.simpletimetracker.core.provider

interface ApplicationDataProvider {

    fun getPackageName(): String
    fun getAppName(): String
    fun getAppVersion(): String
}
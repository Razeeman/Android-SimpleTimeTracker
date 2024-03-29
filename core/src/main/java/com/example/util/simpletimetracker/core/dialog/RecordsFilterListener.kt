package com.example.util.simpletimetracker.core.dialog

import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterResultParams

interface RecordsFilterListener {

    fun onFilterChanged(result: RecordsFilterResultParams)
    fun onFilterDismissed(tag: String)
}
package com.example.util.simpletimetracker.core.dialog

import com.example.util.simpletimetracker.navigation.params.screen.TypesFilterParams

interface TypesFilterDialogListener {

    fun onTypesFilterSelected(tag: String, filter: TypesFilterParams)
    fun onTypesFilterDismissed(tag: String)
}
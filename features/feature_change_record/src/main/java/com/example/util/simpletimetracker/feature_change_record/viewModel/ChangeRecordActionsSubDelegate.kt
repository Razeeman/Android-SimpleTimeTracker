package com.example.util.simpletimetracker.feature_change_record.viewModel

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

interface ChangeRecordActionsSubDelegate<PARENT> {

    fun attach(parent: PARENT)

    fun getViewData(): List<ViewHolderType>

    suspend fun updateViewData()
}
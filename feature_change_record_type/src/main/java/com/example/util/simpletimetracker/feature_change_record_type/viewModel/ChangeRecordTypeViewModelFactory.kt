package com.example.util.simpletimetracker.feature_change_record_type.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChangeRecordTypeViewModelFactory(
    private val name: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChangeRecordTypeViewModel(name) as T
    }
}
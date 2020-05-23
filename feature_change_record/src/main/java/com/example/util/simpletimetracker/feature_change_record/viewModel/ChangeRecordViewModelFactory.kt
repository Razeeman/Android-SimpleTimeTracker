package com.example.util.simpletimetracker.feature_change_record.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChangeRecordViewModelFactory(
    private val id: Long,
    private val daysFromToday: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChangeRecordViewModel(id, daysFromToday) as T
    }
}
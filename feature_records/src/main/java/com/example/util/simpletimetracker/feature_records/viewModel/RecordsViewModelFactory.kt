package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RecordsViewModelFactory(
    private val rangeStart: Long,
    private val rangeEnd: Long
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RecordsViewModel(rangeStart, rangeEnd) as T
    }
}
package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StatisticsViewModelFactory(
    private val rangeStart: Long,
    private val rangeEnd: Long
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return StatisticsViewModel(rangeStart, rangeEnd) as T
    }
}
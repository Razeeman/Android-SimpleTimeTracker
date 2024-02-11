package com.example.util.simpletimetracker.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class BaseViewModelFactory<VM : ViewModel> @Inject constructor(
    private val viewModel: VM,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
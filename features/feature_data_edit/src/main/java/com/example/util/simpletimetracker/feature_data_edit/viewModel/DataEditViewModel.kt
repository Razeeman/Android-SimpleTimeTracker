package com.example.util.simpletimetracker.feature_data_edit.viewModel

import androidx.lifecycle.ViewModel
import com.example.util.simpletimetracker.navigation.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DataEditViewModel @Inject constructor(
    private val router: Router,
) : ViewModel() {


}

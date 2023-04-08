package com.example.util.simpletimetracker.feature_data_edit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_data_edit.R
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DataEditViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
) : ViewModel() {

    val selectedRecordsCountViewData: LiveData<String> =
        MutableLiveData(loadSelectedRecordsCountViewData())

    private var selectedRecordsCount: Int = 0

    fun onSelectRecordsClick() {
        router.navigate(RecordsFilterParams)
    }

    private fun loadSelectedRecordsCountViewData(): String {
        val recordsString = resourceRepo.getQuantityString(
            R.plurals.statistics_detail_times_tracked,
            selectedRecordsCount
        ).lowercase()

        return "$selectedRecordsCount $recordsString"
    }
}

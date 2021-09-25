package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.core.viewData.RangeViewData
import javax.inject.Inject

class StatisticsSettingsViewModel @Inject constructor() : ViewModel() {

    val rangeLength: LiveData<RangeLength> by lazy {
        return@lazy MutableLiveData(RangeLength.DAY)
    }

    fun onRangeClick(item: CustomSpinner.CustomSpinnerItem) {
        (item as? RangeViewData)?.range?.let {
            (rangeLength as MutableLiveData).value = it
        }
    }
}

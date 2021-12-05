package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.viewData.RangeViewData
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import javax.inject.Inject

class StatisticsSettingsViewModel @Inject constructor() : ViewModel() {

    val rangeLength: LiveData<RangeLength> by lazy {
        return@lazy MutableLiveData(RangeLength.Day)
    }

    fun onRangeSelected(item: CustomSpinner.CustomSpinnerItem) {
        (item as? RangeViewData)?.range?.let {
            rangeLength.set(it)
        }
    }

    fun onCustomRangeSelected(range: Range) {
        RangeLength.Custom(range).let(rangeLength::set)
    }
}

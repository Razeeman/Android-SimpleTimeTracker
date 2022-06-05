package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.viewData.RangeViewData
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsSettingsViewModel @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
) : ViewModel() {

    val rangeUpdated: LiveData<RangeLength> = MutableLiveData()

    fun onRangeSelected(item: CustomSpinner.CustomSpinnerItem) = viewModelScope.launch {
        (item as? RangeViewData)?.range?.let {
            prefsInteractor.setStatisticsRange(it)
            rangeUpdated.set(it)
        }
    }

    fun onCustomRangeSelected(range: Range) = viewModelScope.launch {
        RangeLength.Custom(range).let {
            prefsInteractor.setStatisticsRange(it)
            rangeUpdated.set(it)
        }
    }
}

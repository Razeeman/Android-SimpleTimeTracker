package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.ChangeRecordParams
import javax.inject.Inject

class RecordsContainerViewModel @Inject constructor(
    private val router: Router,
    private val timeMapper: TimeMapper
) : ViewModel() {

    val title: LiveData<String> by lazy {
        return@lazy MutableLiveData(loadTitle())
    }
    val position: LiveData<Int> by lazy {
        return@lazy MutableLiveData(0)
    }

    fun onRecordAddClick() {
        router.navigate(
            Screen.CHANGE_RECORD,
            ChangeRecordParams.New(daysFromToday = position.value.orZero())
        )
    }

    fun onPreviousClick() {
        updatePosition(position.value.orZero() - 1)
    }

    fun onTodayClick() {
        updatePosition(0)
    }

    fun onNextClick() {
        updatePosition(position.value.orZero() + 1)
    }

    private fun updatePosition(newPosition: Int) {
        (position as MutableLiveData).value = newPosition
        (title as MutableLiveData).value = loadTitle()
    }

    private fun loadTitle(): String {
        return timeMapper.toDayTitle(position.value.orZero())
    }
}

package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.feature_settings.adapter.SettingsTranslatorViewData
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import javax.inject.Inject

class SettingsTranslatorsViewModelDelegate @Inject constructor(
    private val settingsMapper: SettingsMapper,
) : ViewModelDelegate() {

    val translatorsViewData: LiveData<List<SettingsTranslatorViewData>>
        by lazy { MutableLiveData(loadTranslatorsViewData()) }

    private fun loadTranslatorsViewData(): List<SettingsTranslatorViewData> {
        return settingsMapper.mapTranslatorsViewData()
    }
}
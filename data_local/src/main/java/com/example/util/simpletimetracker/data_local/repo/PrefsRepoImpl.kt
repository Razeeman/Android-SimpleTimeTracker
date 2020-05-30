package com.example.util.simpletimetracker.data_local.repo

import android.content.SharedPreferences
import com.example.util.simpletimetracker.data_local.extension.delegate
import com.example.util.simpletimetracker.domain.repo.PrefsRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsRepoImpl @Inject constructor(
    prefs: SharedPreferences
) : PrefsRepo {

    override var recordTypesFilteredOnChart: Set<String> by prefs.delegate(
        KEY_RECORD_TYPES_FILTERED_ON_CHART, emptySet()
    )

    override var sortRecordTypesByColor: Boolean by prefs.delegate(
        KEY_SORT_RECORD_TYPES_BY_COLOR, false
    )

    companion object {
        private const val KEY_RECORD_TYPES_FILTERED_ON_CHART = "recordTypesFilteredOnChart"
        private const val KEY_SORT_RECORD_TYPES_BY_COLOR = "sortRecordTypesByColor"
    }
}
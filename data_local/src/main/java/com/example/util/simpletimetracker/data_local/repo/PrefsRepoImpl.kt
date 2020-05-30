package com.example.util.simpletimetracker.data_local.repo

import android.content.SharedPreferences
import com.example.util.simpletimetracker.data_local.extension.delegate
import com.example.util.simpletimetracker.domain.repo.PrefsRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsRepoImpl @Inject constructor(
    private val prefs: SharedPreferences
) : PrefsRepo {

    override var recordTypesFilteredOnChart: Set<String> by prefs.delegate(
        KEY_RECORD_TYPES_FILTERED_ON_CHART, emptySet()
    )

    override var sortRecordTypesByColor: Boolean by prefs.delegate(
        KEY_SORT_RECORD_TYPES_BY_COLOR, false
    )

    override fun setWidget(widgetId: Int, recordType: Long) {
        prefs.edit().putLong(KEY_WIDGET + widgetId, recordType).apply()
    }

    override fun getWidget(widgetId: Int): Long {
        return prefs.getLong(KEY_WIDGET + widgetId, 0)
    }

    override fun removeWidget(widgetId: Int) {
        prefs.edit().remove(KEY_WIDGET + widgetId).apply()
    }

    companion object {
        private const val KEY_RECORD_TYPES_FILTERED_ON_CHART = "recordTypesFilteredOnChart"
        private const val KEY_SORT_RECORD_TYPES_BY_COLOR = "sortRecordTypesByColor"
        private const val KEY_WIDGET = "widget_"
    }
}
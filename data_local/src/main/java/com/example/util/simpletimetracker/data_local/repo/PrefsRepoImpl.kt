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

    override var showUntrackedInRecords: Boolean by prefs.delegate(
        KEY_SHOW_UNTRACKED_IN_RECORDS, true
    )

    override var allowMultitasking: Boolean by prefs.delegate(
        ALLOW_MULTITASKING, true
    )

    override var numberOfCards: Int by prefs.delegate(
        NUMBER_OF_CARDS, 0
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

    override fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_RECORD_TYPES_FILTERED_ON_CHART = "recordTypesFilteredOnChart"
        private const val KEY_SORT_RECORD_TYPES_BY_COLOR = "sortRecordTypesByColor"
        private const val KEY_SHOW_UNTRACKED_IN_RECORDS = "showUntrackedInRecords"
        private const val ALLOW_MULTITASKING = "allowMultitasking"
        private const val NUMBER_OF_CARDS = "numberOfCards" // 0 - default width
        private const val KEY_WIDGET = "widget_"
    }
}
package com.example.util.simpletimetracker.data_local.repo

import android.content.SharedPreferences
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.data_local.R
import com.example.util.simpletimetracker.data_local.extension.delegate
import com.example.util.simpletimetracker.domain.repo.PrefsRepo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsRepoImpl @Inject constructor(
    private val prefs: SharedPreferences,
    private val resourceRepo: ResourceRepo
) : PrefsRepo {

    private val defaultCardSize: Int by lazy {
        resourceRepo.getDimenInDp(R.dimen.record_type_card_width)
    }

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

    override var cardSize: Int by prefs.delegate(
        CARD_SIZE, defaultCardSize
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
        private const val CARD_SIZE = "cardSize"
        private const val KEY_WIDGET = "widget_"
    }
}
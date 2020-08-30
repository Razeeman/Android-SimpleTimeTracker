package com.example.util.simpletimetracker.domain.repo

interface PrefsRepo {

    var recordTypesFilteredOnChart: Set<String>

    var recordTypesOrder: Int

    var showUntrackedInRecords: Boolean

    var allowMultitasking: Boolean

    var numberOfCards: Int

    fun setWidget(widgetId: Int, recordType: Long)

    fun getWidget(widgetId: Int): Long

    fun removeWidget(widgetId: Int)

    fun setRecordTypesOrderManual(cardOrder: Map<Long, Long>)

    fun getRecordTypesOrderManual(): Map<Long, Long>

    fun clear()
}
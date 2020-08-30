package com.example.util.simpletimetracker.domain.repo

interface PrefsRepo {

    var recordTypesFilteredOnChart: Set<String>

    var sortRecordTypesByColor: Boolean

    var showUntrackedInRecords: Boolean

    var allowMultitasking: Boolean

    var numberOfCards: Int

    fun setWidget(widgetId: Int, recordType: Long)

    fun getWidget(widgetId: Int): Long

    fun removeWidget(widgetId: Int)

    fun setCardsOrder(cardOrder: Map<Long, Long>)

    fun getCardsOrder(): Map<Long, Long>

    fun removeCardsOrder()

    fun clear()
}
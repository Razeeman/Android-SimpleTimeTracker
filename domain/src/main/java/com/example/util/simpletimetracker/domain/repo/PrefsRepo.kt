package com.example.util.simpletimetracker.domain.repo

interface PrefsRepo {

    var recordTypesFilteredOnChart: Set<String>

    var categoriesFilteredOnChart: Set<String>

    var chartFilterType: Int

    var cardOrder: Int

    var showUntrackedInRecords: Boolean

    var allowMultitasking: Boolean

    var showNotifications: Boolean

    var inactivityReminderDuration: Long // in seconds

    var darkMode: Boolean

    var numberOfCards: Int

    var useMilitaryTimeFormat: Boolean

    fun setWidget(widgetId: Int, recordType: Long)

    fun getWidget(widgetId: Int): Long

    fun removeWidget(widgetId: Int)

    fun setCardOrderManual(cardOrder: Map<Long, Long>)

    fun getCardOrderManual(): Map<Long, Long>

    fun clear()
}
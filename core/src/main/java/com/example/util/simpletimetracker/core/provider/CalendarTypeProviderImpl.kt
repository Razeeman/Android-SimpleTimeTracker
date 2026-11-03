package com.example.util.simpletimetracker.core.provider

import com.example.util.simpletimetracker.domain.calendar.CalendarTypeProvider
import com.example.util.simpletimetracker.domain.calendar.mapper.CalendarTypeMapper
import com.example.util.simpletimetracker.domain.calendar.model.CalendarType
import com.example.util.simpletimetracker.domain.prefs.repo.PrefsRepo
import javax.inject.Inject

class CalendarTypeProviderImpl @Inject constructor(
    private val prefsRepo: PrefsRepo,
    private val calendarTypeMapper: CalendarTypeMapper,
) : CalendarTypeProvider {

    override fun get(): CalendarType {
        // Use prefs directly to avoid suspending.
        return calendarTypeMapper.map(prefsRepo.calendarType)
    }
}

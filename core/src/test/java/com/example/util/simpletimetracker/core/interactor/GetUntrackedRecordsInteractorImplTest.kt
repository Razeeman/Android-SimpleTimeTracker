package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.mapper.UntrackedRangeMapper
import com.example.util.simpletimetracker.core.provider.LocaleProvider
import com.example.util.simpletimetracker.core.repo.BaseResourceRepo
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.mapper.UnCoveredRangesMapper
import com.example.util.simpletimetracker.domain.record.mapper.UntrackedRecordMapper
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.Record
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Calendar
import java.util.TimeZone

class GetUntrackedRecordsInteractorImplTest {

    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `duration cutoff is applied after weekday and time range slicing`() = runBlocking {
        val prefsInteractor = mock(PrefsInteractor::class.java)
        val recordInteractor = mock(RecordInteractor::class.java)
        val timeMapper = TimeMapper(
            localeProvider = mock(LocaleProvider::class.java),
            resourceRepo = mock(BaseResourceRepo::class.java),
            currentTimestampProvider = mock(CurrentTimestampProvider::class.java),
        )
        val range = Range(timestamp(1, 7, 55), timestamp(1, 8, 5))
        val firstRecord = Record(
            typeId = 1,
            timeStarted = range.timeStarted,
            timeEnded = range.timeStarted,
            comment = "",
            tags = emptyList(),
        )
        `when`(recordInteractor.getNext(0)).thenReturn(firstRecord)
        `when`(prefsInteractor.getIgnoreShortUntrackedDuration()).thenReturn(6 * 60)
        `when`(prefsInteractor.getUntrackedRangeEnabled()).thenReturn(true)
        `when`(prefsInteractor.getUntrackedRangeStart()).thenReturn(hours(8))
        `when`(prefsInteractor.getUntrackedRangeEnd()).thenReturn(hours(17))
        `when`(prefsInteractor.getUntrackedDaysOfWeek()).thenReturn(setOf(DayOfWeek.MONDAY))
        val subject = GetUntrackedRecordsInteractorImpl(
            untrackedRecordMapper = UntrackedRecordMapper(UnCoveredRangesMapper()),
            untrackedRangeMapper = UntrackedRangeMapper(timeMapper),
            recordInteractor = recordInteractor,
            prefsInteractor = prefsInteractor,
        )

        val result = subject.get(range = range, records = emptyList())

        assertEquals(emptyList<Record>(), result)
    }

    private fun timestamp(
        day: Int,
        hour: Int,
        minute: Int,
    ): Long {
        return Calendar.getInstance().apply {
            clear()
            set(2024, Calendar.JANUARY, day, hour, minute, 0)
        }.timeInMillis
    }

    private fun hours(value: Int): Long = value * 60L * 60L * 1_000L
}

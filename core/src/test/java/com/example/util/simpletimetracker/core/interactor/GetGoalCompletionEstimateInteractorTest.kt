package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.interactor.GetGoalCompletionEstimateInteractor.Estimate
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.provider.LocaleProvider
import com.example.util.simpletimetracker.core.provider.MutableCalendarTypeProvider
import com.example.util.simpletimetracker.core.provider.TimeZoneProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.statistics.model.Statistics
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.util.Locale
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
class GetGoalCompletionEstimateInteractorTest {

    private val originalTimeZone = TimeZone.getDefault()
    private var now = 10_000L
    private var statistics = listOf(Statistics(1, Statistics.Data(0, 0, 1)))
    private var filterType = ChartFilterType.ACTIVITY
    private val clock = mock(CurrentTimestampProvider::class.java)
    private lateinit var subject: GetGoalCompletionEstimateInteractor
    private lateinit var timeMapper: TimeMapper

    @Before
    fun before() = runBlocking {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        `when`(clock.get()).thenAnswer { now }
        subject = GetGoalCompletionEstimateInteractor(clock)
        val locale = mock(LocaleProvider::class.java)
        `when`(locale.get()).thenReturn(Locale.US)
        timeMapper = TimeMapper(
            localeProvider = locale,
            resourceRepo = mock(ResourceRepo::class.java),
            currentTimestampProvider = clock,
            calendarTypeProvider = MutableCalendarTypeProvider(),
            timeZoneProvider = TimeZoneProvider()
        )
    }

    @After
    fun after() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `activity estimate uses supplied duration including milliseconds`() = runBlocking {
        statistics = listOf(Statistics(1, Statistics.Data(3_234, 2, 1)))
        assertEquals(Estimate.Completion(16_766, now), get(goal(seconds = 10)))
    }

    @Test
    fun `estimates preserve supplied minute or second precision`() = runBlocking {
        statistics = listOf(Statistics(1, Statistics.Data(60_000, 1, 1)))
        assertEquals(Estimate.Completion(now + 60_000, now), get(goal(seconds = 120)))
        statistics = listOf(Statistics(1, Statistics.Data(61_234, 1, 1)))
        assertEquals(Estimate.Completion(now + 58_766, now), get(goal(seconds = 120)))
    }

    @Test
    fun `category and tag estimates use supplied number of timers`() = runBlocking {
        for ((filter, idData) in listOf(
            ChartFilterType.CATEGORY to RecordTypeGoal.IdData.Category(7),
            ChartFilterType.RECORD_TAG to RecordTypeGoal.IdData.Tag(7),
        )) {
            filterType = filter
            statistics = listOf(Statistics(7, Statistics.Data(0, 0, 2)))
            assertEquals(Estimate.Completion(now + 500, now), get(goal(idData, seconds = 1)))
        }
    }

    @Test
    fun `statistics only match goals of the supplied filter type and id`() = runBlocking {
        assertEquals(null, get(goal(idData = RecordTypeGoal.IdData.Category(1))))
        assertEquals(null, get(goal(idData = RecordTypeGoal.IdData.Tag(1))))
        assertEquals(null, get(goal(idData = RecordTypeGoal.IdData.Type(2))))
        filterType = ChartFilterType.CATEGORY
        assertEquals(null, get(goal()))
        filterType = ChartFilterType.RECORD_TAG
        assertEquals(null, get(goal()))
    }

    @Test
    fun `goals ceil fractional milliseconds and limits strictly exceed thresholds`() = runBlocking {
        statistics = listOf(Statistics(1, Statistics.Data(0, 0, 3)))
        val goal = goal(seconds = 1)
        assertEquals(Estimate.Completion(now + 334, now), get(goal))
        assertEquals(Estimate.Completion(now + 334, now), get(goal.copy(subtype = RecordTypeGoal.Subtype.Limit)))
        statistics = listOf(Statistics(1, Statistics.Data(0, 0, 2)))
        assertEquals(Estimate.Completion(now + 500, now), get(goal))
        assertEquals(Estimate.Completion(now + 501, now), get(goal.copy(subtype = RecordTypeGoal.Subtype.Limit)))
    }

    @Test
    fun `goal at threshold is hidden while limit at threshold completes next millisecond`() = runBlocking {
        statistics = listOf(Statistics(1, Statistics.Data(1_000, 1, 1)))
        assertEquals(null, get(goal(seconds = 1)))
        val limit = goal(seconds = 1).copy(subtype = RecordTypeGoal.Subtype.Limit)
        assertEquals(Estimate.Completion(now + 1, now), get(limit))
        statistics = listOf(Statistics(1, Statistics.Data(1_001, 1, 1)))
        assertEquals(null, get(limit))
    }

    @Test
    fun `missing statistics stopped timers count goals and session goals are hidden`() = runBlocking {
        assertEquals(null, get(goal().copy(type = RecordTypeGoal.Type.Count(10))))
        assertEquals(null, get(goal().copy(range = RecordTypeGoal.Range.Session)))
        assertEquals(null, get(goal().copy(range = RecordTypeGoal.Range.Weekly)))
        assertEquals(null, get(goal(seconds = 0)))
        statistics = emptyList()
        assertEquals(null, get(goal()))
        statistics = listOf(Statistics(1, Statistics.Data(1_000, 1, 0)))
        assertEquals(null, get(goal()))
    }

    @Test
    fun `historical future and period end timestamps have no estimate`() = runBlocking {
        assertEquals(null, get(goal(), range = Range(1, now)))
        assertEquals(null, get(goal(), range = Range(now + 1, now + 100_000)))
    }

    @Test
    fun `completion at or after reset is not reached but preceding millisecond is allowed`() = runBlocking {
        assertEquals(Estimate.NotReached, get(goal(seconds = 1), range = Range(1, now + 1_000)))
        assertEquals(Estimate.NotReached, get(goal(seconds = 1), range = Range(1, now + 999)))
        assertEquals(Estimate.Completion(now + 1_000, now), get(goal(seconds = 1), range = Range(1, now + 1_001)))
        val limit = goal(seconds = 1).copy(subtype = RecordTypeGoal.Subtype.Limit)
        assertEquals(Estimate.NotReached, get(limit, range = Range(1, now + 1_001)))
    }

    @Test
    fun `overall goals have no reset and include all running duration`() = runBlocking {
        statistics = listOf(Statistics(1, Statistics.Data(1_000, 1, 1)))
        assertEquals(
            Estimate.Completion(now + 3_599_000, now),
            get(goal(seconds = 3_600).copy(range = RecordTypeGoal.Range.Overall), RangeLength.All, Range(0, 0)),
        )
    }

    @Test
    fun `custom day start uses supplied duration and reporting reset`() = runBlocking {
        now = Instant.parse("2026-11-09T01:00:00Z").toEpochMilli()
        val range = timeMapper.getRangeStartAndEnd(RangeLength.Day, 0, DayOfWeek.MONDAY, 2 * 3_600_000L)
        assertEquals(Instant.parse("2026-11-09T02:00:00Z").toEpochMilli(), range.timeEnded)
        statistics = listOf(Statistics(1, Statistics.Data(23 * 3_600_000L, 1, 1)))
        // 23 hours accumulated in this reporting day, regardless of the earlier start.
        assertEquals(Estimate.NotReached, get(goal(seconds = 24 * 3_600), range = range))
        assertEquals(Estimate.Completion(now + 1_000, now), get(goal(seconds = 23 * 3_600 + 1), range = range))
    }

    @Test
    fun `weekly monthly and yearly resets use supplied calendar periods`() = runBlocking {
        now = Instant.parse("2026-11-08T12:00:00Z").toEpochMilli()
        for ((length, goalRange) in listOf(
            RangeLength.Week to RecordTypeGoal.Range.Weekly,
            RangeLength.Month to RecordTypeGoal.Range.Monthly,
            RangeLength.Year to RecordTypeGoal.Range.Yearly,
        )) {
            for (firstDay in listOf(DayOfWeek.MONDAY, DayOfWeek.SUNDAY)) {
                val range = timeMapper.getRangeStartAndEnd(length, 0, firstDay, 0)
                val seconds = (range.timeEnded - now) / 1_000
                assertEquals(Estimate.NotReached, get(goal(seconds = seconds).copy(range = goalRange), length, range))
            }
        }
    }

    @Test
    fun `daylight saving resets use elapsed time on short and long days`() = runBlocking {
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
        for ((timestamp, hours) in listOf("2026-03-08T05:00:00Z" to 23L, "2026-11-01T04:00:00Z" to 25L)) {
            now = Instant.parse(timestamp).toEpochMilli()
            val range = timeMapper.getRangeStartAndEnd(RangeLength.Day, 0, DayOfWeek.MONDAY, 0)
            assertEquals(hours * 3_600_000, range.duration)
            assertEquals(Estimate.NotReached, get(goal(seconds = hours * 3_600), range = range))
            assertEquals(
                Estimate.Completion(range.timeEnded - 1_000, now),
                get(goal(seconds = hours * 3_600 - 1), range = range),
            )
        }
    }

    private fun get(
        goal: RecordTypeGoal,
        length: RangeLength = RangeLength.Day,
        range: Range = Range(1, 1_000_000),
    ): Estimate? {
        return subject.get(
            goals = listOf(goal),
            statistics = statistics,
            filterType = filterType,
            rangeLength = length,
            range = range,
        )[goal.id]
    }

    private fun goal(
        idData: RecordTypeGoal.IdData = RecordTypeGoal.IdData.Type(1),
        seconds: Long = 10,
    ) = RecordTypeGoal(
        id = 1, idData = idData, range = RecordTypeGoal.Range.Daily,
        type = RecordTypeGoal.Type.Duration(seconds), subtype = RecordTypeGoal.Subtype.Goal,
        daysOfWeek = DayOfWeek.entries.toSet(),
    )
}

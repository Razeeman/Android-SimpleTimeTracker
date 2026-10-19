package com.example.util.simpletimetracker.feature_notification.core

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.provider.LocaleProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@RunWith(Parameterized::class)
class GetTimeLeftToTimestampInteractorTest(
    private val input: List<Long>,
    private val output: Long,
) {

    private val currentTimestampProvider = mock(CurrentTimestampProvider::class.java)
    private val timeMapper = TimeMapper(
        mock(LocaleProvider::class.java),
        mock(ResourceRepo::class.java),
        currentTimestampProvider,
    )

    @Test
    fun test() {
        `when`(currentTimestampProvider.get()).thenReturn(input[0])

        val subject = GetTimeLeftToTimestampInteractor(timeMapper, currentTimestampProvider)

        assertEquals(
            "Test failed for params $input",
            output,
            subject.execute(
                dayTimestamp = input[1],
                interval = input[2],
            ),
        )
    }

    companion object {
        private val timezoneDefault: TimeZone = TimeZone.getDefault()
        private val minuteInMs = TimeUnit.MINUTES.toMillis(1)
        private val hourInMs = TimeUnit.HOURS.toMillis(1)
        private val dayInMs = TimeUnit.DAYS.toMillis(1)
        private val startOfDay = 1767225600000L // 2026-01-01 00:00 UTC

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        }

        @JvmStatic
        @AfterClass
        fun afterClass() {
            TimeZone.setDefault(timezoneDefault)
        }

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            // Every day, trigger time is later today.
            arrayOf(
                listOf(startOfDay + 10 * hourInMs, 15 * hourInMs, dayInMs),
                5 * hourInMs,
            ),
            // Every day, trigger time passed, next is tomorrow.
            arrayOf(
                listOf(startOfDay + 10 * hourInMs + 30 * minuteInMs, 0L, dayInMs),
                13 * hourInMs + 30 * minuteInMs,
            ),
            // Every 6 hours from 00:00, next is 12:00.
            arrayOf(
                listOf(startOfDay + 10 * hourInMs + 30 * minuteInMs, 0L, 6 * hourInMs),
                1 * hourInMs + 30 * minuteInMs,
            ),
            // Every 8 hours from 20:00, next is 12:00, before trigger time.
            arrayOf(
                listOf(startOfDay + 10 * hourInMs + 30 * minuteInMs, 20 * hourInMs, 8 * hourInMs),
                1 * hourInMs + 30 * minuteInMs,
            ),
            // Every hour from 00:15, next is 11:15.
            arrayOf(
                listOf(startOfDay + 10 * hourInMs + 30 * minuteInMs, 15 * minuteInMs, hourInMs),
                45 * minuteInMs,
            ),
            // Every 12 hours from 23:00, after last trigger of the day, next is tomorrow 11:00.
            arrayOf(
                listOf(startOfDay + 23 * hourInMs + 30 * minuteInMs, 23 * hourInMs, 12 * hourInMs),
                11 * hourInMs + 30 * minuteInMs,
            ),
            // Exactly at trigger time, next is the following one.
            arrayOf(
                listOf(startOfDay + 12 * hourInMs, 0L, 6 * hourInMs),
                6 * hourInMs,
            ),
        )
    }
}

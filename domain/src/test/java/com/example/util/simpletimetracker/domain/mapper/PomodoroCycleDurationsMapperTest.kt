package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.mapper.PomodoroCycleDurationsMapper.Result
import com.example.util.simpletimetracker.domain.model.PomodoroCycleSettings
import com.example.util.simpletimetracker.domain.model.PomodoroCycleType.Break
import com.example.util.simpletimetracker.domain.model.PomodoroCycleType.Focus
import com.example.util.simpletimetracker.domain.model.PomodoroCycleType.LongBreak
import com.example.util.simpletimetracker.domain.provider.CurrentTimestampProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.concurrent.TimeUnit

@RunWith(Parameterized::class)
class PomodoroCycleDurationsMapperTest(
    private val input: List<Any>,
    private val output: Result,
) {

    private val currentTimestampProvider = mock(CurrentTimestampProvider::class.java)

    @Test
    fun test() {
        `when`(currentTimestampProvider.get()).thenReturn(input[2] as Long)

        val subject = PomodoroCycleDurationsMapper(currentTimestampProvider)

        assertEquals(
            "Test failed for params $input",
            output,
            subject.map(
                timeStartedMs = input[0] as Long,
                settings = input[1] as PomodoroCycleSettings,
            ),
        )
    }

    companion object {
        private val secondInMs = TimeUnit.SECONDS.toMillis(1)
        private val minuteInMs = TimeUnit.MINUTES.toMillis(1)

        private fun getSettingsSeconds(
            focusTimeMs: Long,
            breakTimeMs: Long,
            longBreakTimeMs: Long,
            periodsUntilLongBreak: Long,
        ): PomodoroCycleSettings {
            return PomodoroCycleSettings(
                focusTimeMs = focusTimeMs * secondInMs,
                breakTimeMs = breakTimeMs * secondInMs,
                longBreakTimeMs = longBreakTimeMs * secondInMs,
                periodsUntilLongBreak = periodsUntilLongBreak,
            )
        }

        @Suppress("SameParameterValue")
        private fun getSettingsMinutes(
            focusTimeMs: Long,
            breakTimeMs: Long,
            longBreakTimeMs: Long,
            periodsUntilLongBreak: Long,
        ): PomodoroCycleSettings {
            return PomodoroCycleSettings(
                focusTimeMs = focusTimeMs * minuteInMs,
                breakTimeMs = breakTimeMs * minuteInMs,
                longBreakTimeMs = longBreakTimeMs * minuteInMs,
                periodsUntilLongBreak = periodsUntilLongBreak,
            )
        }

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            // Zero
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(0, 0, 0, 0),
                    0L,
                ),
                Result(Focus, Focus, 0L, 0L),
            ),

            // Focus only.
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(10, 0, 0, 0),
                    3 * secondInMs,
                ),
                Result(Focus, Focus, 10L * secondInMs, 3L * secondInMs),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(10, 0, 0, 0),
                    15 * secondInMs,
                ),
                Result(Focus, Focus, 10L * secondInMs, 5L * secondInMs),
            ),
            arrayOf(
                listOf(
                    6L * secondInMs,
                    getSettingsSeconds(10, 0, 0, 0),
                    15 * secondInMs,
                ),
                Result(Focus, Focus, 10L * secondInMs, 9L * secondInMs),
            ),

            // Has short break.
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 0, 0),
                    3 * secondInMs,
                ),
                Result(Focus, Break, 25L * secondInMs, 3L * secondInMs),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 0, 0),
                    24 * secondInMs,
                ),
                Result(Focus, Break, 25L * secondInMs, 24L * secondInMs),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 0, 0),
                    25 * secondInMs,
                ),
                Result(Break, Focus, 5L * secondInMs, 0L),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 0, 0),
                    29 * secondInMs,
                ),
                Result(Break, Focus, 5L * secondInMs, 4L * secondInMs),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 0, 0),
                    30 * secondInMs,
                ),
                Result(Focus, Break, 25L * secondInMs, 0L),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 0, 0),
                    37 * secondInMs,
                ),
                Result(Focus, Break, 25L * secondInMs, 7L * secondInMs),
            ),

            // Long break zero.
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(10, 0, 0, 1),
                    105 * secondInMs,
                ),
                Result(Focus, Focus, 10L * secondInMs, 5L * secondInMs),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(10, 0, 0, 2),
                    105 * secondInMs,
                ),
                Result(Focus, Focus, 10L * secondInMs, 5L * secondInMs),
            ),

            // Has one long break.
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 15, 1),
                    3 * secondInMs,
                ),
                Result(Focus, LongBreak, 25L * secondInMs, 3L * secondInMs),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 15, 1),
                    24 * secondInMs,
                ),
                Result(Focus, LongBreak, 25L * secondInMs, 24L * secondInMs),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 15, 1),
                    25 * secondInMs,
                ),
                Result(LongBreak, Focus, 15L * secondInMs, 0L),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 15, 1),
                    39 * secondInMs,
                ),
                Result(LongBreak, Focus, 15L * secondInMs, 14L * secondInMs),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 15, 1),
                    47 * secondInMs,
                ),
                Result(Focus, LongBreak, 25L * secondInMs, 7L * secondInMs),
            ),

            // Has short and long breaks.
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 15, 2),
                    3 * secondInMs,
                ),
                Result(Focus, Break, 25L * secondInMs, 3L * secondInMs),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 15, 2),
                    24 * secondInMs,
                ),
                Result(Focus, Break, 25L * secondInMs, 24L * secondInMs),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 15, 2),
                    25 * secondInMs,
                ),
                Result(Break, Focus, 5L * secondInMs, 0L),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 15, 2),
                    29 * secondInMs,
                ),
                Result(Break, Focus, 5L * secondInMs, 4L * secondInMs),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 15, 2),
                    30 * secondInMs,
                ),
                Result(Focus, LongBreak, 25L * secondInMs, 0L),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 15, 2),
                    54 * secondInMs,
                ),
                Result(Focus, LongBreak, 25L * secondInMs, 24L * secondInMs),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 15, 2),
                    55 * secondInMs,
                ),
                Result(LongBreak, Focus, 15L * secondInMs, 0L),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 15, 2),
                    69 * secondInMs,
                ),
                Result(LongBreak, Focus, 15L * secondInMs, 14L * secondInMs),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 15, 2),
                    70 * secondInMs,
                ),
                Result(Focus, Break, 25L * secondInMs, 0L),
            ),
            arrayOf(
                listOf(
                    0L,
                    getSettingsSeconds(25, 5, 15, 2),
                    77 * secondInMs,
                ),
                Result(Focus, Break, 25L * secondInMs, 7L * secondInMs),
            ),

            // Standard values.
            arrayOf(
                listOf(
                    1 * minuteInMs,
                    getSettingsMinutes(25, 5, 15, 4),
                    2 * minuteInMs + 3 * secondInMs,
                ),
                Result(Focus, Break, 25L * minuteInMs, 1L * minuteInMs + 3 * secondInMs),
            ),
            arrayOf(
                listOf(
                    1 * minuteInMs,
                    getSettingsMinutes(25, 5, 15, 4),
                    25 * minuteInMs + 59 * secondInMs,
                ),
                Result(Focus, Break, 25L * minuteInMs, 24 * minuteInMs + 59 * secondInMs),
            ),
            arrayOf(
                listOf(
                    1 * minuteInMs,
                    getSettingsMinutes(25, 5, 15, 4),
                    28 * minuteInMs + 59 * secondInMs,
                ),
                Result(Break, Focus, 5L * minuteInMs, 2 * minuteInMs + 59 * secondInMs),
            ),
            arrayOf(
                listOf(
                    1 * minuteInMs,
                    getSettingsMinutes(25, 5, 15, 4),
                    38 * minuteInMs + 59 * secondInMs,
                ),
                Result(Focus, Break, 25L * minuteInMs, 7 * minuteInMs + 59 * secondInMs),
            ),
            arrayOf(
                listOf(
                    1 * minuteInMs,
                    getSettingsMinutes(25, 5, 15, 4),
                    58 * minuteInMs + 59 * secondInMs,
                ),
                Result(Break, Focus, 5L * minuteInMs, 2 * minuteInMs + 59 * secondInMs),
            ),
            arrayOf(
                listOf(
                    1 * minuteInMs,
                    getSettingsMinutes(25, 5, 15, 4),
                    68 * minuteInMs + 59 * secondInMs,
                ),
                Result(Focus, Break, 25L * minuteInMs, 7 * minuteInMs + 59 * secondInMs),
            ),
            arrayOf(
                listOf(
                    1 * minuteInMs,
                    getSettingsMinutes(25, 5, 15, 4),
                    88 * minuteInMs + 59 * secondInMs,
                ),
                Result(Break, Focus, 5L * minuteInMs, 2 * minuteInMs + 59 * secondInMs),
            ),
            arrayOf(
                listOf(
                    1 * minuteInMs,
                    getSettingsMinutes(25, 5, 15, 4),
                    98 * minuteInMs + 59 * secondInMs,
                ),
                Result(Focus, LongBreak, 25L * minuteInMs, 7 * minuteInMs + 59 * secondInMs),
            ),
            arrayOf(
                listOf(
                    1 * minuteInMs,
                    getSettingsMinutes(25, 5, 15, 4),
                    118 * minuteInMs + 59 * secondInMs,
                ),
                Result(LongBreak, Focus, 15L * minuteInMs, 2 * minuteInMs + 59 * secondInMs),
            ),
            arrayOf(
                listOf(
                    1 * minuteInMs,
                    getSettingsMinutes(25, 5, 15, 4),
                    138 * minuteInMs + 59 * secondInMs,
                ),
                Result(Focus, Break, 25L * minuteInMs, 7 * minuteInMs + 59 * secondInMs),
            ),
            arrayOf(
                listOf(
                    1 * minuteInMs,
                    getSettingsMinutes(25, 5, 15, 4),
                    130 * 7 + 123 * minuteInMs + 59 * secondInMs,
                ),
                Result(LongBreak, Focus, 15L * minuteInMs, 7 * minuteInMs + 59 * secondInMs),
            ),
        )
    }
}
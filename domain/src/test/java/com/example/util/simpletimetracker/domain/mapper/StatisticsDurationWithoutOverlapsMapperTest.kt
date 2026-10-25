package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.statistics.mapper.StatisticsDurationWithoutOverlapsMapper
import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsDurationWithoutOverlapsMapperTest {

    private val subject = StatisticsDurationWithoutOverlapsMapper()

    @Test
    fun `record crossing a minute boundary keeps its exact duration`() {
        val record = record(50_000, 70_000)

        assertEquals(
            record.duration,
            subject.map(sequenceOf(record)),
        )
    }

    @Test
    fun `disjoint records accumulate exact durations`() {
        val records = listOf(
            record(50_000, 70_000),
            record(110_000, 130_000),
            record(170_000, 190_500),
        )

        assertEquals(
            records.sumOf { it.duration },
            subject.map(records.asSequence()),
        )
    }

    @Test
    fun `overlapping records keep exact merged duration`() {
        val records = sequenceOf(
            record(65_000, 80_500),
            record(50_000, 70_000),
        )

        assertEquals(
            30_500L,
            subject.map(records),
        )
    }

    private fun record(start: Long, end: Long): Record {
        return Record(
            typeId = 1,
            timeStarted = start,
            timeEnded = end,
            comment = "",
            tags = emptyList(),
        )
    }
}

package com.example.util.simpletimetracker.domain.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CoveredRangeMapperTest(
    private val input: List<Pair<Long, Long>>,
    private val output: Long
) {

    private val subject = CoveredRangeMapper()

    @Test
    fun map() {
        assertEquals(
            "Test failed for params $input",
            output,
            subject.map(input)
        )
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            arrayOf(
                emptyList<Pair<Long, Long>>(), 0L
            ),
            arrayOf(
                listOf(2 to 2), 0L
            ),
            arrayOf(
                listOf(2 to 3), 1L
            ),
            arrayOf(
                listOf(3 to 2), 1L
            ),
            arrayOf(
                listOf(2 to 3, 2 to 4), 2L
            ),
            arrayOf(
                listOf(3 to 2, 2 to 4), 2L
            ),
            arrayOf(
                listOf(2 to 4, 4 to 2), 2L
            ),
            arrayOf(
                listOf(2 to 5, 1 to 6), 5L
            ),
            arrayOf(
                listOf(2 to 5, 9 to 12), 6L
            ),
            arrayOf(
                listOf(2 to 5, 4 to 8), 6L
            ),
            arrayOf(
                listOf(2 to 5, 4 to 8, 9 to 12), 9L
            ),
            arrayOf(
                listOf(1 to 2, 2 to 4, 4 to 7), 6L
            )
        )
    }
}
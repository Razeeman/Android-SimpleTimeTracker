package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.model.Range
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class UnCoveredRangesMapperTest(
    private val input: List<Any>,
    private val output: List<Range>,
) {

    private val subject = UnCoveredRangesMapper()

    @Suppress("UNCHECKED_CAST")
    @Test
    fun map() {
        assertEquals(
            "Test failed for params $input",
            output,
            subject.map(
                start = input[0] as Long,
                end = input[1] as Long,
                segments = input[2] as List<Range>,
            ),
        )
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            // Invalid range
            arrayOf(
                listOf(10L, 0L, listOf(Range(2L, 3L))),
                emptyList(),
            ),

            // Zero range
            arrayOf(
                listOf(0L, 0L, listOf(Range(2L, 3L))),
                emptyList(),
            ),

            // No segments
            arrayOf(
                listOf(0L, 0L, emptyList<Range>()),
                emptyList(),
            ),
            arrayOf(
                listOf(0L, 10L, emptyList<Range>()),
                listOf(Range(0L, 10L)),
            ),

            // Zero segment
            arrayOf(
                listOf(0L, 10L, listOf(Range(2L, 2L))),
                listOf(Range(0L, 2L), Range(2L, 10L)),
            ),
            arrayOf(
                listOf(0L, 10L, listOf(Range(0L, 0L), Range(5L, 5L), Range(10L, 10L))),
                listOf(Range(0L, 5L), Range(5L, 10L)),
            ),

            // Segments on range points
            arrayOf(
                listOf(0L, 10L, listOf(Range(0L, 10L))),
                emptyList(),
            ),
            arrayOf(
                listOf(0L, 10L, listOf(Range(0L, 2L), Range(8L, 10L))),
                listOf(Range(2L, 8L)),
            ),
            arrayOf(
                listOf(0L, 10L, listOf(Range(0L, 6L), Range(4L, 10L))),
                emptyList(),
            ),
            arrayOf(
                listOf(0L, 10L, listOf(Range(0L, 5L), Range(5L, 10L))),
                emptyList(),
            ),

            // One segment
            arrayOf(
                listOf(0L, 10L, listOf(Range(2L, 3L))),
                listOf(Range(0L, 2L), Range(3L, 10L)),
            ),
            arrayOf(
                listOf(0L, 10L, listOf(Range(3L, 2L))),
                listOf(Range(0L, 2L), Range(3L, 10L)),
            ),
            arrayOf(
                listOf(0L, 10L, listOf(Range(0L, 2L))),
                listOf(Range(2L, 10L)),
            ),
            arrayOf(
                listOf(0L, 10L, listOf(Range(7L, 10L))),
                listOf(Range(0L, 7L)),
            ),

            // Overlapping segments
            arrayOf(
                listOf(0L, 10L, listOf(Range(2L, 3L), Range(2L, 3L))),
                listOf(Range(0L, 2L), Range(3L, 10L)),
            ),
            arrayOf(
                listOf(0L, 10L, listOf(Range(2L, 3L), Range(2L, 4L))),
                listOf(Range(0L, 2L), Range(4L, 10L)),
            ),
            arrayOf(
                listOf(0L, 10L, listOf(Range(3L, 2L), Range(2L, 4L))),
                listOf(Range(0L, 2L), Range(4L, 10L)),
            ),
            arrayOf(
                listOf(0L, 10L, listOf(Range(2L, 4L), Range(4L, 2L))),
                listOf(Range(0L, 2L), Range(4L, 10L)),
            ),
            arrayOf(
                listOf(0L, 10L, listOf(Range(2L, 5L), Range(1L, 6L))),
                listOf(Range(0L, 1L), Range(6L, 10L)),
            ),
            arrayOf(
                listOf(0L, 10L, listOf(Range(2L, 5L), Range(4L, 8L))),
                listOf(Range(0L, 2L), Range(8L, 10L)),
            ),
            arrayOf(
                listOf(0L, 10L, listOf(Range(1L, 2L), Range(2L, 4L), Range(4L, 7L))),
                listOf(Range(0L, 1L), Range(7L, 10L)),
            ),
            arrayOf(
                listOf(0L, 10L, listOf(Range(0L, 3L), Range(3L, 6L), Range(6L, 10L))),
                emptyList(),
            ),

            // Disconnected segments
            arrayOf(
                listOf(0L, 10L, listOf(Range(2L, 5L), Range(8L, 9L))),
                listOf(Range(0L, 2L), Range(5L, 8L), Range(9L, 10L)),
            ),
            arrayOf(
                listOf(0L, 10L, listOf(Range(0L, 5L), Range(8L, 10L))),
                listOf(Range(5L, 8L)),
            ),

            // Outside of range
            arrayOf(
                listOf(10L, 20L, listOf(Range(5L, 6L))),
                listOf(Range(10L, 20L)),
            ),
            arrayOf(
                listOf(10L, 20L, listOf(Range(4L, 6L), Range(24L, 26L))),
                listOf(Range(10L, 20L)),
            ),
            arrayOf(
                listOf(10L, 20L, listOf(Range(4L, 16L))),
                listOf(Range(16L, 20L)),
            ),
            arrayOf(
                listOf(10L, 20L, listOf(Range(4L, 14L), Range(16L, 26L))),
                listOf(Range(14L, 16L)),
            ),
            arrayOf(
                listOf(10L, 20L, listOf(Range(4L, 16L), Range(14L, 26L))),
                emptyList(),
            ),
        )
    }
}
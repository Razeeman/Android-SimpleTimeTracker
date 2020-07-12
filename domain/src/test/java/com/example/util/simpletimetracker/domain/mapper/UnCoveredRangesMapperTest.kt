package com.example.util.simpletimetracker.domain.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class UnCoveredRangesMapperTest(
    private val input: List<Any>,
    private val output: List<Pair<Long, Long>>
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
                segments = input[2] as List<Pair<Long, Long>>
            )
        )
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            // Invalid range
            arrayOf(
                listOf(10L, 0L, listOf(2L to 3L)), emptyList()
            ),

            // Zero range
            arrayOf(
                listOf(0L, 0L, listOf(2L to 3L)), emptyList()
            ),

            // No segments
            arrayOf(
                listOf(0L, 0L, emptyList<Pair<Long, Long>>()), emptyList()
            ),
            arrayOf(
                listOf(0L, 10L, emptyList<Pair<Long, Long>>()), listOf(0L to 10L)
            ),

            // Zero segment
            arrayOf(
                listOf(0L, 10L, listOf(2L to 2L)), listOf(0L to 2L, 2L to 10L)
            ),
            arrayOf(
                listOf(0L, 10L, listOf(0L to 0L, 5L to 5L, 10L to 10L)), listOf(0L to 5L, 5L to 10L)
            ),

            // Segments on range points
            arrayOf(
                listOf(0L, 10L, listOf(0L to 10L)), emptyList()
            ),
            arrayOf(
                listOf(0L, 10L, listOf(0L to 2L, 8L to 10L)), listOf(2L to 8L)
            ),
            arrayOf(
                listOf(0L, 10L, listOf(0L to 6L, 4L to 10L)), emptyList()
            ),
            arrayOf(
                listOf(0L, 10L, listOf(0L to 5L, 5L to 10L)), emptyList()
            ),

            // One segment
            arrayOf(
                listOf(0L, 10L, listOf(2L to 3L)), listOf(0L to 2L, 3L to 10L)
            ),
            arrayOf(
                listOf(0L, 10L, listOf(3L to 2L)), listOf(0L to 2L, 3L to 10L)
            ),
            arrayOf(
                listOf(0L, 10L, listOf(0L to 2L)), listOf(2L to 10L)
            ),
            arrayOf(
                listOf(0L, 10L, listOf(7L to 10L)), listOf(0L to 7L)
            ),

            // Overlapping segments
            arrayOf(
                listOf(0L, 10L, listOf(2L to 3L, 2L to 3L)), listOf(0L to 2L, 3L to 10L)
            ),
            arrayOf(
                listOf(0L, 10L, listOf(2L to 3L, 2L to 4L)), listOf(0L to 2L, 4L to 10L)
            ),
            arrayOf(
                listOf(0L, 10L, listOf(3L to 2L, 2L to 4L)), listOf(0L to 2L, 4L to 10L)
            ),
            arrayOf(
                listOf(0L, 10L, listOf(2L to 4L, 4L to 2L)), listOf(0L to 2L, 4L to 10L)
            ),
            arrayOf(
                listOf(0L, 10L, listOf(2L to 5L, 1L to 6L)), listOf(0L to 1L, 6L to 10L)
            ),
            arrayOf(
                listOf(0L, 10L, listOf(2L to 5L, 4L to 8L)), listOf(0L to 2L, 8L to 10L)
            ),
            arrayOf(
                listOf(0L, 10L, listOf(1L to 2L, 2L to 4L, 4L to 7L)), listOf(0L to 1L, 7L to 10L)
            ),
            arrayOf(
                listOf(0L, 10L, listOf(0L to 3L, 3L to 6L, 6L to 10L)), emptyList()
            ),

            // Disconnected segments
            arrayOf(
                listOf(0L, 10L, listOf(2L to 5L, 8L to 9L)), (listOf(0L to 2L, 5L to 8L, 9L to 10L))
            ),
            arrayOf(
                listOf(0L, 10L, listOf(0L to 5L, 8L to 10L)), (listOf(5L to 8L))
            ),

            // Outside of range
            arrayOf(
                listOf(10L, 20L, listOf(5L to 6L)), (listOf(10L to 20L))
            ),
            arrayOf(
                listOf(10L, 20L, listOf(4L to 6L, 24L to 26L)), (listOf(10L to 20L))
            ),
            arrayOf(
                listOf(10L, 20L, listOf(4L to 16L)), (listOf(16L to 20L))
            ),
            arrayOf(
                listOf(10L, 20L, listOf(4L to 14L, 16L to 26L)), (listOf(14L to 16L))
            ),
            arrayOf(
                listOf(10L, 20L, listOf(4L to 16L, 14L to 26L)), emptyList()
            )
        )
    }
}
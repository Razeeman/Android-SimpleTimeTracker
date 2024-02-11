package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.model.Range
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class OverlappingRangesMapperTest(
    private val input: List<Pair<Long, Range>>,
    private val output: List<Pair<List<Long>, Range>>,
) {

    private val subject = OverlappingRangesMapper()

    private data class Id(val value: Long) : OverlappingRangesMapper.Id

    @Suppress("UNCHECKED_CAST")
    @Test
    fun map() {
        val expected = output.map {
            it.first.sorted().map(::Id) to it.second
        }
        val actual = subject.map(
            segments = input.map { Id(it.first) to it.second },
        ).map {
            it.first.sortedBy { id -> (id as Id).value } to it.second
        }

        assertEquals(
            "Test failed for params $input",
            expected,
            actual,
        )
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            // No segments
            arrayOf(
                emptyList(),
                emptyList(),
            ),
            arrayOf(
                listOf(1L to Range(2, 2)),
                emptyList(),
            ),
            arrayOf(
                listOf(1L to Range(0, 0), 2 to Range(5, 5), 3 to Range(10, 10)),
                emptyList(),
            ),

            // Segments on range points
            arrayOf(
                listOf(1L to Range(1, 1), 2 to Range(1, 1)),
                emptyList(),
            ),
            arrayOf(
                listOf(1L to Range(1, 2), 2 to Range(2, 3), 3 to Range(3, 4)),
                emptyList(),
            ),

            // One overlap
            arrayOf(
                listOf(1 to Range(2, 4), 2 to Range(3, 5)),
                listOf(listOf(1, 2) to Range(3, 4)),
            ),
            arrayOf(
                listOf(1 to Range(2, 4), 2 to Range(5, 3)),
                listOf(listOf(1, 2) to Range(3, 4)),
            ),
            arrayOf(
                listOf(1 to Range(4, 2), 2 to Range(3, 5)),
                listOf(listOf(1, 2) to Range(3, 4)),
            ),

            arrayOf(
                listOf(1 to Range(0, 7), 2 to Range(0, 10)),
                listOf(listOf(1, 2) to Range(0, 7)),
            ),

            arrayOf(
                listOf(1 to Range(0, 10), 2 to Range(4, 6)),
                listOf(listOf(1, 2) to Range(4, 6)),
            ),
            arrayOf(
                listOf(1 to Range(4, 6), 2 to Range(0, 10)),
                listOf(listOf(1, 2) to Range(4, 6)),
            ),

            arrayOf(
                listOf(1 to Range(0, 10), 2 to Range(0, 10)),
                listOf(listOf(1, 2) to Range(0, 10)),
            ),
            arrayOf(
                listOf(1 to Range(0, 10), 2 to Range(10, 0)),
                listOf(listOf(1, 2) to Range(0, 10)),
            ),

            // Two overlaps
            arrayOf(
                listOf(
                    1 to Range(0L, 4L),
                    2 to Range(6L, 10L),
                    3 to Range(2L, 8L),
                ),
                listOf(
                    listOf(1, 3) to Range(2, 4),
                    listOf(2, 3) to Range(6, 8),
                ),
            ),
            arrayOf(
                listOf(
                    1 to Range(0L, 10L),
                    2 to Range(2L, 4L),
                    3 to Range(6L, 8L),
                ),
                listOf(
                    listOf(1, 2) to Range(2, 4),
                    listOf(1, 3) to Range(6, 8),
                ),
            ),

            // Three overlaps
            arrayOf(
                listOf(
                    1 to Range(0L, 10L),
                    2 to Range(2L, 8L),
                    3 to Range(4L, 6L),
                ),
                listOf(
                    listOf(1, 2) to Range(2, 4),
                    listOf(1, 2, 3) to Range(4, 6),
                    listOf(1, 2) to Range(6, 8),
                ),
            ),

            // Many overlaps
            arrayOf(
                listOf(
                    1 to Range(0L, 14L),
                    2 to Range(2L, 6L),
                    3 to Range(4L, 10L),
                    4 to Range(8L, 12L),
                ),
                listOf(
                    listOf(1, 2) to Range(2, 4),
                    listOf(1, 2, 3) to Range(4, 6),
                    listOf(1, 3) to Range(6, 8),
                    listOf(1, 4, 3) to Range(8, 10),
                    listOf(1, 4) to Range(10, 12),
                ),
            ),

            // Disconnected segments
            arrayOf(
                listOf(1 to Range(2L, 5L), 2 to Range(8L, 9L)),
                emptyList(),
            ),
            arrayOf(
                listOf(1 to Range(0L, 5L), 2 to Range(5L, 10L)),
                emptyList(),
            ),
        )
    }
}
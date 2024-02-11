package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.model.Range
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class UnCoveredRangesMapper @Inject constructor() {

    /**
     * Finds parts of the range from start to end that are not covered by segments.
     *
     * Implementation of Klee's algorithm for length of union of segments,
     * taken from here https://www.geeksforgeeks.org/klees-algorithm-length-union-segments-line/
     */
    fun map(start: Long, end: Long, segments: List<Range>): List<Range> {
        if (start > end) return emptyList()

        val n = segments.size

        // Create a list to store starting and ending points
        // Segment start marked with false
        val points: MutableList<Pair<Long, Boolean>> = mutableListOf()
        var secondIsHigher: Boolean
        for (i in (0 until n)) {
            // Ignore segments outside of range
            if (segments[i].timeStarted < start && segments[i].timeEnded < start) continue
            if (segments[i].timeStarted > end && segments[i].timeEnded > end) continue

            // Reverse segments if needed
            secondIsHigher = segments[i].timeEnded > segments[i].timeStarted
            points.add(max(start, segments[i].timeStarted) to !secondIsHigher)
            points.add(min(end, segments[i].timeEnded) to secondIsHigher)
        }

        // Sorting all points by point value
        points.sortWith(compareBy({ it.first }, { it.second }))

        // Include range start and end
        points.add(0, start to true)
        points.add(end to false)

        // Initialize result
        val result = mutableListOf<Range>()

        // To keep track of counts of current open segments
        // (Starting point is processed, but ending point is not)
        var counter = 0

        // Traverse through all points including range start and end
        for (i in (1 until points.size)) {
            // If there are no open points, then we add the
            // difference between previous and current point.
            if (counter == 0) {
                Range(points[i - 1].first, points[i].first)
                    .takeUnless { it.timeStarted == it.timeEnded }
                    ?.let(result::add)
            }

            // If this is an ending point, reduce count of open points
            if (points[i].second) counter-- else counter++

            if (points[i].first > end) break
        }

        return result
    }
}
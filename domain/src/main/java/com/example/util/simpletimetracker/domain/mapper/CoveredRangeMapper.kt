package com.example.util.simpletimetracker.domain.mapper

import javax.inject.Inject

class CoveredRangeMapper @Inject constructor() {

    fun map(segments: List<Pair<Long, Long>>): Long {
        if (segments.isEmpty()) return 0L

        val n = segments.size

        // Create a list to store starting and ending points
        // Segment start marked with false
        val points: MutableList<Pair<Long, Boolean>> = mutableListOf()
        var secondIsHigher: Boolean
        for (i in (0 until n)) {
            // Reverse segments if needed
            secondIsHigher = segments[i].second > segments[i].first
            points.add(segments[i].first to !secondIsHigher)
            points.add(segments[i].second to secondIsHigher)
        }

        // Sorting all points by point value
        points.sortWith(compareBy({ it.first }, { it.second }))

        // Initialize result
        var result = 0L

        // To keep track of counts of current open segments
        // (Starting point is processed, but ending point is not)
        var counter = 0

        // Traverse through all points
        for (i in (0 until n * 2)) {
            // If there are open points, then we add the
            // difference between previous and current point.
            // This is interesting as we don't check whether
            // current point is opening or closing,
            if (counter > 0) result += (points[i].first - points[i - 1].first)

            // If this is an ending point, reduce, count of
            // open points.
            if (points[i].second) counter-- else counter++
        }

        return result
    }
}
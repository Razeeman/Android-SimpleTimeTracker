package com.example.util.simpletimetracker.core.utils

object CalendarIntersectionCalculator {

    fun <T> execute(
        res: List<Data<T>>,
    ): List<Data<T>> {
        // Calculate intersections.
        val points: MutableList<Triple<Long, Boolean, Data<T>>> = mutableListOf()
        res.forEach { item ->
            // Start of range marked with true.
            points.add(Triple(item.start, true, item))
            points.add(Triple(item.end, false, item))
        }

        // Sort by range edge (start or end) when put starts first.
        points.sortWith(compareBy({ it.first }, { it.second }))
        var currentCounter = 0
        var currentColumnCount = 1
        val freeColumns = mutableListOf(1)

        fun calculateColumns(
            point: Pair<Int, Triple<Long, Boolean, Data<T>>>,
        ): Pair<Int, Triple<Long, Boolean, Data<T>>> {
            val (counter, triple) = point

            // New separate column.
            if (counter == 0) {
                currentColumnCount = 1
            } else if (counter > currentColumnCount) {
                currentColumnCount = counter
            }
            if (currentColumnCount > triple.third.columnCount) {
                triple.third.columnCount = currentColumnCount
            }

            return counter to triple
        }

        points.map { (time, isStart, item) ->
            if (isStart) {
                currentCounter++
                val columnNumber = freeColumns.minOrNull()!!
                item.columnNumber = columnNumber
                freeColumns.remove(columnNumber)
                if (freeColumns.isEmpty()) freeColumns.add(columnNumber + 1)
            } else {
                currentCounter--
                freeColumns.add(item.columnNumber)
            }
            currentCounter to Triple(time, isStart, item)
        }
            // Find max column count and pass it further and back down the list.
            .map(::calculateColumns)
            .reversed()
            .map(::calculateColumns)

        return res
    }

    data class Data<T>(
        val start: Long,
        val end: Long,
        val point: T,
        // Set after the fact.
        var columnCount: Int = 1,
        var columnNumber: Int = 1,
    )
}
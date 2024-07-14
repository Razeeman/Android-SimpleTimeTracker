package com.example.util.simpletimetracker.domain.model

sealed class RangeLength {
    object Day : RangeLength()
    object Week : RangeLength()
    object Month : RangeLength()
    object Year : RangeLength()
    object All : RangeLength()
    data class Custom(val range: Range) : RangeLength()
    data class Last(val days: Int) : RangeLength()
}
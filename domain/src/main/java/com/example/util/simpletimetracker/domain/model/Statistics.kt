package com.example.util.simpletimetracker.domain.model

data class Statistics(
    val id: Long,
    val data: Data,
) {

    data class Data(
        val duration: Long,
        val count: Long,
    )
}
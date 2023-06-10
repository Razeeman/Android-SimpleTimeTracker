package com.example.util.simpletimetracker.domain.model

data class RunningRecord(
    val id: Long,
    override val timeStarted: Long,
    override val comment: String,
    override val tagIds: List<Long> = emptyList(),
) : RecordBase {

    override val typeIds: List<Long> = listOf(id)
    override val timeEnded: Long get() = System.currentTimeMillis()
}
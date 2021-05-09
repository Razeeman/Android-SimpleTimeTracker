package com.example.util.simpletimetracker.domain.model

data class RecordTag(
    val id: Long = 0,
    val typeId: Long,
    val name: String,
    val archived: Boolean = false
)
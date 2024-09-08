package com.example.util.simpletimetracker.domain.model

data class RecordType(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: AppColor,
    val instant: Boolean,
    val instantDuration: Long,
    val hidden: Boolean = false,
)
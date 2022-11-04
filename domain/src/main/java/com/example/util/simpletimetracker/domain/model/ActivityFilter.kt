package com.example.util.simpletimetracker.domain.model

data class ActivityFilter(
    val id: Long = 0,
    val selectedIds: List<Long>,
    val type: Type,
    val name: String,
    val color: AppColor,
    val selected: Boolean,
) {

    sealed interface Type {
        object ActivityTag: Type
        object Activity: Type
    }
}
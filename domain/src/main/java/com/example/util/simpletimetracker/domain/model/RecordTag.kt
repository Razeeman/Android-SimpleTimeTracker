package com.example.util.simpletimetracker.domain.model

/**
 * typeId = 0 would mean that the tag is general (not assigned to any activity).
 * This tag can be assigned to any record.
 *
 * If typeId != 0, take color from activity, otherwise take from color field.
 */
data class RecordTag(
    val id: Long = 0,
    val typeId: Long,
    val name: String,
    val color: AppColor,
    val archived: Boolean = false,
)
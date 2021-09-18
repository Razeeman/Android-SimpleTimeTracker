package com.example.util.simpletimetracker.domain.model

/**
 * typeId = 0 would mean that the tag is general (not assigned to any activity).
 * This tag can be assigned to any record.
 *
 * If typeId != 0, take color from activity, otherwise take from color field.
 *
 * At the moment general tags would be saved in a separate database.
 * Record tags with assigned activity are saved in the record itself but only one.
 * // TODO move record tag saving from record to same separate database to allow assigning several activity tags.
 */
data class RecordTag(
    val id: Long = 0,
    val typeId: Long,
    val name: String,
    val color: Int,
    val archived: Boolean = false
)
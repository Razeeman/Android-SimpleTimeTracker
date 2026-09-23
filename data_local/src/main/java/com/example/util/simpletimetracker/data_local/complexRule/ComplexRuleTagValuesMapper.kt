package com.example.util.simpletimetracker.data_local.complexRule

import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.RecordTagValueFormatMapper
import javax.inject.Inject

class ComplexRuleTagValuesMapper @Inject constructor(
    private val recordTagValueFormatMapper: RecordTagValueFormatMapper,
) {

    fun serialize(
        data: List<RecordBase.Tag>,
        tagIdsToSelectValueOnStart: Set<Long>,
    ): String {
        val numericEntries = data.mapNotNull { tag ->
            val value = tag.numericValue ?: return@mapNotNull null
            "${tag.tagId}:${recordTagValueFormatMapper.map(value)}"
        }
        val laterEntries = tagIdsToSelectValueOnStart
            .filter { tagId -> data.none { it.tagId == tagId && it.numericValue != null } }
            .map { tagId -> "$tagId:later" }
        return (numericEntries + laterEntries).joinToString(separator = ",")
    }

    fun parse(data: String?): ParsedTagValues {
        if (data.isNullOrBlank()) {
            return ParsedTagValues(tagsWithValues = emptyList(), tagIdsToSelectValueOnStart = emptySet())
        }

        val numericValues = mutableMapOf<Long, Double>()
        val tagIdsToSelectValueOnStart = mutableSetOf<Long>()

        data.split(',').forEach { entry ->
            val parts = entry.split(':', limit = 2)
            if (parts.size != 2) return@forEach
            val tagId = parts[0].toLongOrNull() ?: return@forEach
            when (parts[1]) {
                "later" -> {
                    if (tagId !in numericValues) {
                        tagIdsToSelectValueOnStart.add(tagId)
                    }
                }
                else -> {
                    val numericValue = parts[1].toDoubleOrNull()
                        ?.takeIf { it.isFinite() } ?: return@forEach
                    numericValues[tagId] = numericValue
                    tagIdsToSelectValueOnStart.remove(tagId)
                }
            }
        }

        val tagsWithValues = numericValues.map { (tagId, numericValue) ->
            RecordBase.Tag(tagId = tagId, numericValue = numericValue)
        }

        return ParsedTagValues(
            tagsWithValues = tagsWithValues,
            tagIdsToSelectValueOnStart = tagIdsToSelectValueOnStart,
        )
    }

    data class ParsedTagValues(
        val tagsWithValues: List<RecordBase.Tag>,
        val tagIdsToSelectValueOnStart: Set<Long>,
    )
}

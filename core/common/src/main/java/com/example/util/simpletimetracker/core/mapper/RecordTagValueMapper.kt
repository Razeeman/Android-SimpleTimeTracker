package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.common.R
import com.example.util.simpletimetracker.core.repo.BaseResourceRepo
import javax.inject.Inject

class RecordTagValueMapper @Inject constructor(
    private val resourceRepo: BaseResourceRepo,
) {

    fun map(value: Double): String {
        // TODO do better?
        return value.toBigDecimal().stripTrailingZeros().toPlainString()
    }

    fun mapTagValue(
        value: Double,
        valueSuffix: String,
    ): String {
        val actualValue = map(value)
        return if (valueSuffix.isEmpty()) {
            actualValue
        } else {
            resourceRepo.getString(R.string.separator_template, actualValue, valueSuffix)
        }
    }

    fun getNameWithValue(
        name: String,
        value: Double,
        valueSuffix: String,
    ): String {
        val tagValue = mapTagValue(value, valueSuffix)
        return resourceRepo.getString(R.string.separator_template, name, "($tagValue)")
    }
}
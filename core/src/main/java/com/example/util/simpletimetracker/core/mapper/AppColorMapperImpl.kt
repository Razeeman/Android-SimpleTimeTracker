package com.example.util.simpletimetracker.core.mapper

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.mapper.AppColorMapper
import com.example.util.simpletimetracker.domain.model.AppColor
import javax.inject.Inject

class AppColorMapperImpl @Inject constructor(
    private val resourceRepo: ResourceRepo,
) : AppColorMapper {

    @ColorInt override fun mapToColorInt(color: AppColor): Int {
        return if (color.colorInt.isNotEmpty()) {
            color.colorInt.toIntOrNull()
        } else {
            ColorMapper.getAvailableColors().getOrNull(color.colorId)?.let(resourceRepo::getColor)
        } ?: resourceRepo.getColor(R.color.black)
    }
}
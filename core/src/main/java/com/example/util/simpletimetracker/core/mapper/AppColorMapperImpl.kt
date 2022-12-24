package com.example.util.simpletimetracker.core.mapper

import android.graphics.Color
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

    override fun mapToHsv(@ColorInt colorInt: Int): FloatArray {
        val hsv = FloatArray(3)
        Color.colorToHSV(colorInt, hsv)
        return hsv
    }
}
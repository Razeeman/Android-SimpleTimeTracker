package com.example.util.simpletimetracker.core.interactor

import android.graphics.Color
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorPaletteViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorViewData
import javax.inject.Inject

class ColorViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val resourceRepo: ResourceRepo,
    private val colorMapper: ColorMapper,
) {

    suspend fun getColorsViewData(
        currentColor: AppColor,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val customColorSelected = currentColor.colorInt.isNotEmpty()

        return ColorMapper.getAvailableColors()
            .asSequence()
            .mapIndexed { colorId, colorResId ->
                colorId to resourceRepo.getColor(colorResId)
            }
            .map { (colorId, colorInt) ->
                val hsv = FloatArray(3)
                Color.colorToHSV(colorInt, hsv)
                Triple(colorId, colorInt, hsv)
            }
            .sortedWith(
                compareBy(
                    { -it.third[0] }, // hue
                    { it.third[1] }, // saturation
                    { it.third[2] }, // value
                ),
            )
            .map { (colorId, colorInt, _) ->
                ColorViewData(
                    colorId = colorId,
                    colorInt = colorInt.let {
                        if (isDarkTheme) colorMapper.darkenColor(it) else it
                    },
                    selected = !customColorSelected && currentColor.colorId == colorId,
                )
            }
            .plus(
                ColorPaletteViewData(selected = customColorSelected),
            )
            .toList()
    }
}
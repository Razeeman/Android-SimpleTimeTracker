package com.example.util.simpletimetracker.core.interactor

import android.graphics.Color
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.interactor.FavouriteColorInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorFavouriteViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorPaletteViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import javax.inject.Inject

class ColorViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val resourceRepo: ResourceRepo,
    private val colorMapper: ColorMapper,
    private val favouriteColorInteractor: FavouriteColorInteractor,
) {

    suspend fun getColorsViewData(
        currentColor: AppColor?,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val customColorSelected = currentColor?.colorInt?.isNotEmpty().orFalse()
        val favouriteColors = favouriteColorInteractor.getAll()
        val favouriteColorsData = favouriteColors.map { it.colorInt }
        val result = mutableListOf<ViewHolderType>()

        result += ColorMapper.getAvailableColors()
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
                    colorId = colorId.toLong(),
                    type = ColorViewData.Type.Base,
                    colorInt = colorInt.let {
                        if (isDarkTheme) colorMapper.darkenColor(it) else it
                    },
                    selected = !customColorSelected && currentColor?.colorId == colorId,
                )
            }

        result += ColorPaletteViewData(selected = customColorSelected)

        if (customColorSelected) {
            val iconColor = if (currentColor?.colorInt in favouriteColorsData) {
                R.attr.colorAccent
            } else {
                R.attr.appInactiveColor
            }.let { resourceRepo.getThemedAttr(it, isDarkTheme) }
            result += ColorFavouriteViewData(iconColor = iconColor)
        }

        if (favouriteColors.isNotEmpty()) {
            result += HintViewData(
                text = resourceRepo.getString(R.string.change_record_favourite_comments_hint),
            )

            result += favouriteColors.map { favouriteColor ->
                ColorViewData(
                    colorId = favouriteColor.id,
                    type = ColorViewData.Type.Favourite,
                    colorInt = favouriteColor.colorInt.toIntOrNull()
                        ?.let { if (isDarkTheme) colorMapper.darkenColor(it) else it }
                        ?: colorMapper.toInactiveColor(isDarkTheme),
                    selected = false,
                )
            }
        }

        return result
    }
}
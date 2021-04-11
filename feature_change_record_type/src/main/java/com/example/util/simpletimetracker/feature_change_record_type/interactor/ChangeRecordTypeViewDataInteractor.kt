package com.example.util.simpletimetracker.feature_change_record_type.interactor

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.divider.DividerViewData
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.ColorViewData
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.IconType
import com.example.util.simpletimetracker.feature_change_record_type.mapper.ChangeRecordTypeMapper
import javax.inject.Inject

class ChangeRecordTypeViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val mapper: ChangeRecordTypeMapper,
    private val prefsInteractor: PrefsInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper
) {

    suspend fun getCategoriesViewData(selectedCategories: List<Long>): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return categoryInteractor.getAll()
            .takeUnless(List<Category>::isEmpty)
            ?.let { categories ->
                val selected = categories.filter { it.id in selectedCategories }
                val available = categories.filter { it.id !in selectedCategories }
                selected to available
            }
            ?.let { (selected, available) ->
                val viewData = mutableListOf<ViewHolderType>()

                mapper.mapSelectedCategoriesHint(
                    isEmpty = selected.isEmpty()
                ).let(viewData::add)

                selected.map {
                    categoryViewDataMapper.map(
                        category = it,
                        isDarkTheme = isDarkTheme
                    )
                }.let(viewData::addAll)

                DividerViewData
                    .takeUnless { available.isEmpty() }
                    ?.let(viewData::add)

                available.map {
                    categoryViewDataMapper.map(
                        category = it,
                        isDarkTheme = isDarkTheme
                    )
                }.let(viewData::addAll)

                viewData
            }
            ?: mapper.mapToEmpty()
    }

    suspend fun getColorsViewData(): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return ColorMapper.getAvailableColors(isDarkTheme)
            .mapIndexed { colorId, colorResId ->
                colorId to resourceRepo.getColor(colorResId)
            }
            .map { (colorId, colorInt) ->
                ColorViewData(
                    colorId = colorId,
                    colorInt = colorInt
                )
            }
    }

    suspend fun getIconsViewData(
        newColorId: Int,
        iconType: IconType
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        val iconSwitch = mapper.mapToIconSwitchViewData(iconType).let(::listOf)
        val icons = when (iconType) {
            IconType.IMAGE -> mapper.mapIconImageData(newColorId, isDarkTheme)
            IconType.EMOJI -> mapper.mapIconEmojiData(newColorId, isDarkTheme)
        }
        return iconSwitch + icons
    }
}
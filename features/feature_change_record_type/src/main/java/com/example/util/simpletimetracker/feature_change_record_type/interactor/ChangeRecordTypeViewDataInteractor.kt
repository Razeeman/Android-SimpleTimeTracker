package com.example.util.simpletimetracker.feature_change_record_type.interactor

import com.example.util.simpletimetracker.core.interactor.ColorViewDataInteractor
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.IconImageState
import com.example.util.simpletimetracker.domain.model.IconType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_change_record_type.mapper.ChangeRecordTypeMapper
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconStateViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChangeRecordTypeViewDataInteractor @Inject constructor(
    private val mapper: ChangeRecordTypeMapper,
    private val prefsInteractor: PrefsInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val colorViewDataInteractor: ColorViewDataInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
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

                categoryViewDataMapper.mapToCategoryHint().let(viewData::add)

                DividerViewData(1).let(viewData::add)

                categoryViewDataMapper.mapSelectedCategoriesHint(
                    isEmpty = selected.isEmpty()
                ).let(viewData::add)

                selected.map {
                    categoryViewDataMapper.mapCategory(
                        category = it,
                        isDarkTheme = isDarkTheme
                    )
                }.let(viewData::addAll)

                DividerViewData(2).let(viewData::add)

                available.map {
                    categoryViewDataMapper.mapCategory(
                        category = it,
                        isDarkTheme = isDarkTheme
                    )
                }.let(viewData::addAll)

                categoryViewDataMapper.mapToTypeTagAddItem(isDarkTheme).let(viewData::add)

                viewData
            }
            ?: listOf(
                categoryViewDataMapper.mapToCategoriesFirstHint(),
                categoryViewDataMapper.mapToTypeTagAddItem(isDarkTheme)
            )
    }

    suspend fun getColorsViewData(currentColor: AppColor): List<ViewHolderType> {
        return colorViewDataInteractor.getColorsViewData(currentColor)
    }

    suspend fun getIconsViewData(
        newColor: AppColor,
        iconType: IconType,
        iconImageState: IconImageState,
        iconSearch: String,
    ): ChangeRecordTypeIconStateViewData = withContext(Dispatchers.IO) {
        val isDarkTheme = prefsInteractor.getDarkMode()

        when (iconType) {
            IconType.IMAGE -> {
                val search = if (iconImageState == IconImageState.Search) iconSearch else ""
                ChangeRecordTypeIconStateViewData.Icons(
                    mapper.mapIconImageData(newColor, search, isDarkTheme)
                )
            }
            IconType.TEXT -> {
                ChangeRecordTypeIconStateViewData.Text
            }
            IconType.EMOJI -> {
                ChangeRecordTypeIconStateViewData.Icons(
                    mapper.mapIconEmojiData(newColor, isDarkTheme)
                )
            }
        }
    }

    fun getIconCategoriesViewData(
        iconType: IconType,
        selectedIndex: Long,
    ): List<ViewHolderType> {
        return when (iconType) {
            IconType.IMAGE -> mapper.mapIconImageCategories(selectedIndex)
            IconType.TEXT -> emptyList()
            IconType.EMOJI -> mapper.mapIconEmojiCategories(selectedIndex)
        }
    }
}
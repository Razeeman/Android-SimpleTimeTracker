package com.example.util.simpletimetracker.core.delegates.iconSelection.interactor

import com.example.util.simpletimetracker.core.delegates.iconSelection.mapper.IconSelectionMapper
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionStateViewData
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.IconImageState
import com.example.util.simpletimetracker.domain.model.IconType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IconSelectionDelegateViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val mapper: IconSelectionMapper,
) {

    suspend fun getIconsViewData(
        newColor: AppColor,
        iconType: IconType,
        iconImageState: IconImageState,
        iconSearch: String,
    ): IconSelectionStateViewData = withContext(Dispatchers.IO) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val search = if (iconImageState == IconImageState.Search) iconSearch else ""

        when (iconType) {
            IconType.IMAGE -> {
                val items = mapper.mapIconImageData(
                    newColor = newColor,
                    search = search,
                    isDarkTheme = isDarkTheme,
                )
                IconSelectionStateViewData.Icons(items)
            }
            IconType.TEXT -> {
                IconSelectionStateViewData.Text
            }
            IconType.EMOJI -> {
                val items = mapper.mapIconEmojiData(
                    newColor = newColor,
                    search = search,
                    isDarkTheme = isDarkTheme,
                )
                IconSelectionStateViewData.Icons(items)
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
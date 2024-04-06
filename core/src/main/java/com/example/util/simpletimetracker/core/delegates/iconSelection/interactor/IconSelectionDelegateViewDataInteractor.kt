package com.example.util.simpletimetracker.core.delegates.iconSelection.interactor

import com.example.util.simpletimetracker.core.delegates.iconSelection.mapper.ChangeRecordTypeMapper
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.ChangeRecordTypeIconStateViewData
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
    private val mapper: ChangeRecordTypeMapper,
) {

    suspend fun getIconsViewData(
        newColor: AppColor,
        iconType: IconType,
        iconImageState: IconImageState,
        iconSearch: String,
    ): ChangeRecordTypeIconStateViewData = withContext(Dispatchers.IO) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val search = if (iconImageState == IconImageState.Search) iconSearch else ""

        when (iconType) {
            IconType.IMAGE -> {
                val items = mapper.mapIconImageData(
                    newColor = newColor,
                    search = search,
                    isDarkTheme = isDarkTheme,
                )
                ChangeRecordTypeIconStateViewData.Icons(items)
            }
            IconType.TEXT -> {
                ChangeRecordTypeIconStateViewData.Text
            }
            IconType.EMOJI -> {
                val items = mapper.mapIconEmojiData(
                    newColor = newColor,
                    search = search,
                    isDarkTheme = isDarkTheme,
                )
                ChangeRecordTypeIconStateViewData.Icons(items)
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
package com.example.util.simpletimetracker.feature_icon_selection.api

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_base_adapter.emoji.EmojiViewData
import com.example.util.simpletimetracker.feature_icon_selection.api.viewData.IconSelectionCategoryViewData
import com.example.util.simpletimetracker.feature_icon_selection.api.viewData.IconSelectionScrollViewData
import com.example.util.simpletimetracker.feature_icon_selection.api.viewData.IconSelectionSelectorStateViewData
import com.example.util.simpletimetracker.feature_icon_selection.api.viewData.IconSelectionStateViewData
import com.example.util.simpletimetracker.feature_icon_selection.api.viewData.IconSelectionViewData

interface IconSelectionViewModelDelegate {
    val icons: LiveData<IconSelectionStateViewData>
    val iconCategories: LiveData<List<ViewHolderType>>
    val iconsTypeViewData: LiveData<List<ViewHolderType>>
    val iconSelectorViewData: LiveData<IconSelectionSelectorStateViewData>
    val iconsScrollPosition: LiveData<IconSelectionScrollViewData>
    val expandIconTypeSwitch: LiveData<Unit>
    var newIcon: String

    fun attach(parent: Parent)
    fun clearIconDelegate()
    suspend fun updateIconViewData()
    fun onNoIconClick()
    fun onIconTypeClick(viewData: ButtonsRowViewData)
    fun onIconCategoryClick(viewData: IconSelectionCategoryViewData)
    fun onIconClick(item: IconSelectionViewData)
    fun onIconsScrolled(firstVisiblePosition: Int, lastVisiblePosition: Int)
    fun onIconImageFavouriteClicked()
    fun onIconImageSearchClicked()
    fun onIconImageSearch(search: String)
    fun onEmojiClick(item: EmojiViewData)
    fun onIconTextChange(text: String)
    fun onEmojiSelected(tag: String, emojiText: String)
    fun onScrolled()

    interface Parent {
        fun getDialogTag(): String
        fun keyboardVisibility(isVisible: Boolean)
        suspend fun update()
        fun onIconSelected() = Unit
        fun getColor(): AppColor
    }
}
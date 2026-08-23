package com.example.util.simpletimetracker.feature_icon_selection.viewModelDelegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.feature_icon_selection.interactor.IconSelectionDelegateViewDataInteractor
import com.example.util.simpletimetracker.feature_icon_selection.mapper.IconSelectionMapper
import com.example.util.simpletimetracker.feature_icon_selection.viewData.IconSelectionCategoryInfoViewData
import com.example.util.simpletimetracker.feature_icon_selection.viewData.IconSelectionSwitchViewData
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.IconEmojiMapper
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.favourite.interactor.FavouriteIconInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.favourite.model.FavouriteIcon
import com.example.util.simpletimetracker.domain.icon.IconImageState
import com.example.util.simpletimetracker.domain.icon.IconType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.emoji.EmojiViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_icon_selection.api.viewData.IconSelectionStateViewData
import com.example.util.simpletimetracker.feature_icon_selection.api.IconSelectionViewModelDelegate
import com.example.util.simpletimetracker.feature_icon_selection.api.viewData.IconSelectionCategoryViewData
import com.example.util.simpletimetracker.feature_icon_selection.api.viewData.IconSelectionScrollViewData
import com.example.util.simpletimetracker.feature_icon_selection.api.viewData.IconSelectionSelectorStateViewData
import com.example.util.simpletimetracker.feature_icon_selection.api.viewData.IconSelectionViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.resources.IconMapperUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class IconSelectionViewModelDelegateImpl @Inject constructor(
    private val router: Router,
    private val iconEmojiMapper: IconEmojiMapper,
    private val prefsInteractor: PrefsInteractor,
    private val iconSelectionMapper: IconSelectionMapper,
    private val viewDataInteractor: IconSelectionDelegateViewDataInteractor,
    private val favouriteIconInteractor: FavouriteIconInteractor,
) : IconSelectionViewModelDelegate,
    ViewModelDelegate() {

    override val icons: LiveData<IconSelectionStateViewData> by lazySuspend {
        loadIconsViewData()
    }
    override val iconCategories: LiveData<List<ViewHolderType>> by lazySuspend {
        loadIconCategoriesViewData(selectedIndex = 0)
    }
    override val iconsTypeViewData: LiveData<List<ViewHolderType>> by lazySuspend {
        loadIconsTypeViewData()
    }
    override val iconSelectorViewData: LiveData<IconSelectionSelectorStateViewData> by lazySuspend {
        loadIconSelectorViewData()
    }
    override val iconsScrollPosition: LiveData<IconSelectionScrollViewData> = MutableLiveData()
    override val expandIconTypeSwitch: LiveData<Unit> = MutableLiveData()

    override var newIcon: String = ""

    private var parent: IconSelectionViewModelDelegate.Parent? = null
    private var iconType: IconType = IconType.IMAGE
    private var iconImageState: IconImageState = IconImageState.Chooser
    private var iconSearch: String = ""
    private var iconSearchJob: Job? = null

    override fun attach(parent: IconSelectionViewModelDelegate.Parent) {
        this.parent = parent
    }

    override fun clearIconDelegate() {
        clear()
    }

    override fun onNoIconClick() {
        delegateScope.launch {
            newIcon = ""
            parent?.onIconSelected()
            parent?.update()
        }
    }

    override fun onIconTypeClick(viewData: ButtonsRowViewData) {
        if (viewData !is IconSelectionSwitchViewData) return
        if (viewData.iconType == iconType) return
        delegateScope.launch {
            parent?.keyboardVisibility(false)
            iconType = viewData.iconType
            updateIconsTypeViewData()
            updateIconSelectorViewData()
            updateIconCategories(selectedIndex = 0)
            updateIconsLoad()
            updateIcons()
        }
    }

    override suspend fun updateIconViewData() {
        updateIconSelectorViewData()
        updateIcons()
    }

    override fun onIconCategoryClick(viewData: IconSelectionCategoryViewData) {
        val firstIconCategory = iconCategories.value
            ?.firstOrNull()
            as? IconSelectionCategoryViewData

        if (viewData.getUniqueId() == firstIconCategory?.getUniqueId().orZero()) {
            expandIconTypeSwitch.set(Unit)
        }
        // Types in icons and categories should have the same index for this to work.
        (icons.value as? IconSelectionStateViewData.Icons)
            ?.items
            ?.indexOfFirst { (it as? IconSelectionCategoryInfoViewData)?.type == viewData.type }
            ?.let(::updateIconScrollPosition)
    }

    override fun onIconClick(item: IconSelectionViewData) {
        delegateScope.launch {
            if (item.iconName != newIcon) {
                newIcon = item.iconName
                parent?.onIconSelected()
                parent?.update()
                updateIconSelectorViewData()
            }
        }
    }

    override fun onIconsScrolled(
        firstVisiblePosition: Int,
        lastVisiblePosition: Int,
    ) {
        val items = (icons.value as? IconSelectionStateViewData.Icons?)
            ?.items ?: return
        val infoItems = items.filterIsInstance<IconSelectionCategoryInfoViewData>()

        // Last image category has small number of icons, need to check if it is visible,
        // otherwise it would never be selected by the second check.
        infoItems
            .firstOrNull { it.isLast }
            ?.takeIf { items.indexOf(it) <= lastVisiblePosition }
            ?.let {
                delegateScope.launch { updateIconCategories(it.getUniqueId()) }
                return
            }

        infoItems
            .lastOrNull { items.indexOf(it) <= firstVisiblePosition }
            ?.let { delegateScope.launch { updateIconCategories(it.getUniqueId()) } }
    }

    override fun onIconImageFavouriteClicked() {
        if (newIcon.isEmpty()) return

        delegateScope.launch {
            favouriteIconInteractor.get(newIcon)?.let {
                favouriteIconInteractor.remove(it.id)
            } ?: run {
                val new = FavouriteIcon(icon = newIcon)
                favouriteIconInteractor.add(new)
            }
            val selectedIndex = iconCategories.value
                ?.filterIsInstance<IconSelectionCategoryViewData>()
                ?.firstOrNull { it.selected }
                ?.type?.id.orZero()
            updateIconSelectorViewData()
            updateIconCategories(selectedIndex = selectedIndex)
            updateIcons()
        }
    }

    override fun onIconImageSearchClicked() {
        val newState = when (iconImageState) {
            is IconImageState.Chooser -> IconImageState.Search
            is IconImageState.Search -> IconImageState.Chooser
        }
        iconImageState = newState

        if (iconImageState is IconImageState.Chooser) {
            parent?.keyboardVisibility(false)
            expandIconTypeSwitch.set(Unit)
        }
        delegateScope.launch {
            updateIconSelectorViewData()
            updateIconCategories(selectedIndex = 0)
            updateIconsLoad()
            updateIcons()
        }
    }

    override fun onIconImageSearch(search: String) {
        if (iconType == IconType.TEXT) return

        if (search != iconSearch) {
            iconSearchJob?.cancel()
            iconSearchJob = delegateScope.launch {
                iconSearch = search
                delay(500)
                updateIcons()
            }
        }
    }

    override fun onEmojiClick(item: EmojiViewData) {
        if (iconEmojiMapper.hasSkinToneVariations(item.emojiCodes)) {
            openEmojiSelectionDialog(item)
        } else {
            delegateScope.launch {
                if (item.emojiText != newIcon) {
                    newIcon = item.emojiText
                    parent?.onIconSelected()
                    parent?.update()
                    updateIconSelectorViewData()
                }
            }
        }
    }

    override fun onIconTextChange(text: String) {
        delegateScope.launch {
            if (text != newIcon) {
                newIcon = text
                parent?.onIconSelected()
                parent?.update()
            }
        }
    }

    override fun onEmojiSelected(tag: String, emojiText: String) {
        if (tag != parent?.getDialogTag()) return
        delegateScope.launch {
            if (emojiText != newIcon) {
                newIcon = emojiText
                parent?.onIconSelected()
                parent?.update()
            }
        }
    }

    override fun onScrolled() {
        iconsScrollPosition.set(IconSelectionScrollViewData.NoScroll)
    }

    private fun openEmojiSelectionDialog(item: EmojiViewData) {
        val parent = parent ?: return
        val params = iconSelectionMapper.mapEmojiSelectionParams(
            tag = parent.getDialogTag(),
            color = parent.getColor(),
            emojiCodes = item.emojiCodes,
        )

        router.navigate(params)
    }

    private suspend fun updateIcons() {
        val data = loadIconsViewData()
        icons.set(data)
    }

    private fun updateIconsLoad() {
        val items = listOf(LoaderViewData())
        val data = IconSelectionStateViewData.Icons(items)
        icons.set(data)
    }

    private suspend fun loadIconsViewData(): IconSelectionStateViewData {
        val color = parent?.getColor() ?: AppColor(0, "")
        return viewDataInteractor.getIconsViewData(
            newColor = color,
            iconType = iconType,
            iconImageState = iconImageState,
            iconSearch = iconSearch,
        )
    }

    private suspend fun updateIconCategories(selectedIndex: Long) {
        val data = loadIconCategoriesViewData(selectedIndex)
        iconCategories.set(data)
    }

    private suspend fun loadIconCategoriesViewData(selectedIndex: Long): List<ViewHolderType> {
        val favourites = favouriteIconInteractor.getAll()
        val hasFavourites = when (iconType) {
            IconType.IMAGE -> favourites.any { IconMapperUtils.isImageIcon(it.icon) }
            IconType.EMOJI -> favourites.any { !IconMapperUtils.isImageIcon(it.icon) }
            IconType.TEXT -> false
        }
        return viewDataInteractor.getIconCategoriesViewData(
            iconType = iconType,
            selectedIndex = selectedIndex,
            hasFavourites = hasFavourites,
        )
    }

    private fun updateIconsTypeViewData() {
        val data = loadIconsTypeViewData()
        iconsTypeViewData.set(data)
    }

    private fun loadIconsTypeViewData(): List<ViewHolderType> {
        return iconSelectionMapper.mapToIconSwitchViewData(iconType)
    }

    private suspend fun updateIconSelectorViewData() {
        val data = loadIconSelectorViewData()
        iconSelectorViewData.set(data)
    }

    private suspend fun loadIconSelectorViewData(): IconSelectionSelectorStateViewData {
        val isFavourite = favouriteIconInteractor.get(newIcon) != null

        return iconSelectionMapper.mapToIconSelectorViewData(
            iconImageState = iconImageState,
            iconType = iconType,
            isSelectedIconFavourite = isFavourite,
            isDarkTheme = prefsInteractor.getDarkMode(),
        )
    }

    private fun updateIconScrollPosition(position: Int) {
        iconsScrollPosition.set(IconSelectionScrollViewData.ScrollTo(position))
    }
}
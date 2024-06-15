package com.example.util.simpletimetracker.core.delegates.iconSelection.viewModelDelegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.iconSelection.interactor.IconSelectionDelegateViewDataInteractor
import com.example.util.simpletimetracker.core.delegates.iconSelection.mapper.IconSelectionMapper
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionCategoryInfoViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionCategoryViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionSelectorStateViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionStateViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionSwitchViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionScrollViewData
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.IconEmojiMapper
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.FavouriteIconInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.FavouriteIcon
import com.example.util.simpletimetracker.domain.model.IconImageState
import com.example.util.simpletimetracker.domain.model.IconType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.emoji.EmojiViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.resources.IconMapperUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IconSelectionViewModelDelegate {
    val icons: LiveData<IconSelectionStateViewData>
    val iconCategories: LiveData<List<ViewHolderType>>
    val iconsTypeViewData: LiveData<List<ViewHolderType>>
    val iconSelectorViewData: LiveData<IconSelectionSelectorStateViewData>
    val iconsScrollPosition: LiveData<IconSelectionScrollViewData>
    val expandIconTypeSwitch: LiveData<Unit>

    fun attach(parent: Parent)
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
    fun onEmojiSelected(emojiText: String)
    fun onScrolled()

    interface Parent {
        fun keyboardVisibility(isVisible: Boolean)
        suspend fun update()
        fun onIconSelected() = Unit
        fun getColor(): AppColor
    }
}

class IconSelectionViewModelDelegateImpl @Inject constructor(
    private val router: Router,
    private val iconEmojiMapper: IconEmojiMapper,
    private val prefsInteractor: PrefsInteractor,
    private val iconSelectionMapper: IconSelectionMapper,
    private val viewDataInteractor: IconSelectionDelegateViewDataInteractor,
    private val favouriteIconInteractor: FavouriteIconInteractor,
) : IconSelectionViewModelDelegate,
    ViewModelDelegate() {

    override val icons: LiveData<IconSelectionStateViewData> by lazy {
        return@lazy MutableLiveData<IconSelectionStateViewData>().let { initial ->
            delegateScope.launch { initial.value = loadIconsViewData() }
            initial
        }
    }
    override val iconCategories: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            delegateScope.launch { initial.value = loadIconCategoriesViewData(selectedIndex = 0) }
            initial
        }
    }
    override val iconsTypeViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData(loadIconsTypeViewData())
    }
    override val iconSelectorViewData: LiveData<IconSelectionSelectorStateViewData> by lazy {
        return@lazy MutableLiveData<IconSelectionSelectorStateViewData>().let { initial ->
            delegateScope.launch { initial.value = loadIconSelectorViewData() }
            initial
        }
    }
    override val iconsScrollPosition: LiveData<IconSelectionScrollViewData> = MutableLiveData()
    override val expandIconTypeSwitch: LiveData<Unit> = MutableLiveData()

    var newIcon: String = ""

    private var parent: IconSelectionViewModelDelegate.Parent? = null
    private var iconType: IconType = IconType.IMAGE
    private var iconImageState: IconImageState = IconImageState.Chooser
    private var iconSearch: String = ""
    private var iconSearchJob: Job? = null

    override fun attach(parent: IconSelectionViewModelDelegate.Parent) {
        this.parent = parent
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

    suspend fun update() {
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

    override fun onEmojiSelected(emojiText: String) {
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
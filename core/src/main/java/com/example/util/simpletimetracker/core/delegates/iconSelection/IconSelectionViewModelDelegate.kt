package com.example.util.simpletimetracker.core.delegates.iconSelection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.iconSelection.interactor.IconSelectionDelegateViewDataInteractor
import com.example.util.simpletimetracker.core.delegates.iconSelection.mapper.ChangeRecordTypeMapper
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.ChangeRecordTypeIconCategoryInfoViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.ChangeRecordTypeIconCategoryViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.ChangeRecordTypeIconSelectorStateViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.ChangeRecordTypeIconStateViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.ChangeRecordTypeIconSwitchViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.ChangeRecordTypeIconViewData
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.ChangeRecordTypeScrollViewData
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.IconEmojiMapper
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.IconImageState
import com.example.util.simpletimetracker.domain.model.IconType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.emoji.EmojiViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.navigation.Router
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IconSelectionViewModelDelegate {
    val icons: LiveData<ChangeRecordTypeIconStateViewData>
    val iconCategories: LiveData<List<ViewHolderType>>
    val iconsTypeViewData: LiveData<List<ViewHolderType>>
    val iconSelectorViewData: LiveData<ChangeRecordTypeIconSelectorStateViewData>
    val iconsScrollPosition: LiveData<ChangeRecordTypeScrollViewData>
    val expandIconTypeSwitch: LiveData<Unit>

    fun attach(parent: Parent)
    fun onIconTypeClick(viewData: ButtonsRowViewData)
    fun onIconCategoryClick(viewData: ChangeRecordTypeIconCategoryViewData)
    fun onIconClick(item: ChangeRecordTypeIconViewData)
    fun onIconsScrolled(firstVisiblePosition: Int, lastVisiblePosition: Int)
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
    private val changeRecordTypeMapper: ChangeRecordTypeMapper,
    private val viewDataInteractor: IconSelectionDelegateViewDataInteractor,
) : IconSelectionViewModelDelegate,
    ViewModelDelegate() {

    override val icons: LiveData<ChangeRecordTypeIconStateViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordTypeIconStateViewData>().let { initial ->
            delegateScope.launch { initial.value = loadIconsViewData() }
            initial
        }
    }
    override val iconCategories: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            delegateScope.launch { initial.value = loadIconCategoriesViewData() }
            initial
        }
    }
    override val iconsTypeViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData(loadIconsTypeViewData())
    }
    override val iconSelectorViewData: LiveData<ChangeRecordTypeIconSelectorStateViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordTypeIconSelectorStateViewData>().let { initial ->
            delegateScope.launch { initial.value = loadIconSelectorViewData() }
            initial
        }
    }
    override val iconsScrollPosition: LiveData<ChangeRecordTypeScrollViewData> = MutableLiveData()
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

    override fun onIconTypeClick(viewData: ButtonsRowViewData) {
        if (viewData !is ChangeRecordTypeIconSwitchViewData) return
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
        updateIcons()
    }

    override fun onIconCategoryClick(viewData: ChangeRecordTypeIconCategoryViewData) {
        if (viewData.getUniqueId() == 0L) {
            expandIconTypeSwitch.set(Unit)
        }
        (icons.value as? ChangeRecordTypeIconStateViewData.Icons)
            ?.items
            ?.indexOfFirst { (it as? ChangeRecordTypeIconCategoryInfoViewData)?.type == viewData.type }
            ?.let(::updateIconScrollPosition)
    }

    override fun onIconClick(item: ChangeRecordTypeIconViewData) {
        delegateScope.launch {
            if (item.iconName != newIcon) {
                newIcon = item.iconName
                parent?.onIconSelected()
                parent?.update()
            }
        }
    }

    override fun onIconsScrolled(
        firstVisiblePosition: Int,
        lastVisiblePosition: Int,
    ) {
        val items = (icons.value as? ChangeRecordTypeIconStateViewData.Icons?)
            ?.items ?: return
        val infoItems = items.filterIsInstance<ChangeRecordTypeIconCategoryInfoViewData>()

        // Last image category has small number of icons, need to check if it is visible,
        // otherwise it would never be selected by the second check.
        infoItems
            .firstOrNull { it.isLast }
            ?.takeIf { items.indexOf(it) <= lastVisiblePosition }
            ?.let {
                updateIconCategories(it.getUniqueId())
                return
            }

        infoItems
            .lastOrNull { items.indexOf(it) <= firstVisiblePosition }
            ?.let { updateIconCategories(it.getUniqueId()) }
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
        iconsScrollPosition.set(ChangeRecordTypeScrollViewData.NoScroll)
    }

    private fun openEmojiSelectionDialog(item: EmojiViewData) {
        val parent = parent ?: return
        val params = changeRecordTypeMapper.mapEmojiSelectionParams(
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
        val data = ChangeRecordTypeIconStateViewData.Icons(items)
        icons.set(data)
    }

    private suspend fun loadIconsViewData(): ChangeRecordTypeIconStateViewData {
        val color = parent?.getColor() ?: AppColor(0, "")
        return viewDataInteractor.getIconsViewData(
            newColor = color,
            iconType = iconType,
            iconImageState = iconImageState,
            iconSearch = iconSearch,
        )
    }

    private fun updateIconCategories(selectedIndex: Long) {
        val data = loadIconCategoriesViewData(selectedIndex)
        iconCategories.set(data)
    }

    private fun loadIconCategoriesViewData(selectedIndex: Long = 0): List<ViewHolderType> {
        return viewDataInteractor.getIconCategoriesViewData(
            iconType = iconType,
            selectedIndex = selectedIndex,
        )
    }

    private fun updateIconsTypeViewData() {
        val data = loadIconsTypeViewData()
        iconsTypeViewData.set(data)
    }

    private fun loadIconsTypeViewData(): List<ViewHolderType> {
        return changeRecordTypeMapper.mapToIconSwitchViewData(iconType)
    }

    private suspend fun updateIconSelectorViewData() {
        val data = loadIconSelectorViewData()
        iconSelectorViewData.set(data)
    }

    private suspend fun loadIconSelectorViewData(): ChangeRecordTypeIconSelectorStateViewData {
        return changeRecordTypeMapper.mapToIconSelectorViewData(
            iconImageState = iconImageState,
            iconType = iconType,
            isDarkTheme = prefsInteractor.getDarkMode(),
        )
    }

    private fun updateIconScrollPosition(position: Int) {
        iconsScrollPosition.set(ChangeRecordTypeScrollViewData.ScrollTo(position))
    }
}
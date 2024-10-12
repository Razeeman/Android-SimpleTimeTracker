package com.example.util.simpletimetracker.feature_settings.partialRestoreSelection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionViewData
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.model.PartialBackupRestoreData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorViewData
import com.example.util.simpletimetracker.feature_base_adapter.complexRule.ComplexRuleViewData
import com.example.util.simpletimetracker.feature_base_adapter.emoji.EmojiViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_settings.partialRestore.utils.getIds
import com.example.util.simpletimetracker.feature_settings.partialRestoreSelection.model.PartialRestoreSelectionDialogParams
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsFileWorkDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartialRestoreSelectionViewModel @Inject constructor(
    private val partialRestoreSelectionViewDataInteractor: PartialRestoreSelectionViewDataInteractor,
    private val settingsFileWorkDelegate: SettingsFileWorkDelegate,
) : ViewModel() {

    lateinit var extra: PartialRestoreSelectionDialogParams

    val viewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadViewData()
            }
            initial
        }
    }
    val onDataSelected: LiveData<Set<Long>> = MutableLiveData()
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)

    private var initialized: Boolean = false
    private var dataIdsFiltered: MutableSet<Long> = mutableSetOf()

    fun onRecordTypeClick(item: RecordTypeViewData) {
        addOrRemoveId(item.id)
    }

    fun onCategoryClick(item: CategoryViewData) {
        addOrRemoveId(item.id)
    }

    fun onActivityFilterClick(item: ActivityFilterViewData) {
        addOrRemoveId(item.id)
    }

    fun onComplexRuleClick(item: ComplexRuleViewData) {
        addOrRemoveId(item.id)
    }

    fun onIconClick(item: IconSelectionViewData) {
        val iconId = getData()?.favouriteIcon?.values?.firstOrNull {
            it.icon == item.iconName
        }?.id ?: return
        addOrRemoveId(iconId)
    }

    fun onEmojiClick(item: EmojiViewData) {
        val iconId = getData()?.favouriteIcon?.values?.firstOrNull {
            it.icon == item.emojiCodes
        }?.id ?: return
        addOrRemoveId(iconId)
    }

    fun onColorClick(item: ColorViewData) {
        addOrRemoveId(item.colorId)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onRecordClick(item: RecordViewData, sharedElements: Pair<Any, String>? = null) {
        val itemId = (item as? RecordViewData.Tracked)?.id ?: return
        addOrRemoveId(itemId)
    }

    fun onShowAllClick() {
        dataIdsFiltered.clear()
        updateViewData()
    }

    fun onHideAllClick() {
        val allIds = getData()?.getIds(extra.type).orEmpty()
        dataIdsFiltered.addAll(allIds)
        updateViewData()
    }

    fun onSaveClick() {
        saveButtonEnabled.set(false)
        onDataSelected.set(dataIdsFiltered)
    }

    private fun addOrRemoveId(itemId: Long) {
        dataIdsFiltered.addOrRemove(itemId)
        updateViewData()
    }

    private fun getData(): PartialBackupRestoreData? {
        return settingsFileWorkDelegate.partialBackupRestoreDataSelectable
    }

    private fun updateViewData() = viewModelScope.launch {
        val data = loadViewData()
        viewData.set(data)
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        if (!initialized) {
            dataIdsFiltered = extra.filteredIds.toMutableSet()
            initialized = true
        }

        return partialRestoreSelectionViewDataInteractor.getViewData(
            extra = extra,
            dataIdsFiltered = dataIdsFiltered,
            data = getData() ?: return emptyList(),
        )
    }
}

package com.example.util.simpletimetracker.feature_change_record_tag.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.core.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.core.interactor.RecordTypesViewDataInteractor
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.feature_change_record_tag.R
import com.example.util.simpletimetracker.feature_change_record_tag.mapper.ChangeRecordTagMapper
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.ChangeCategoryParams
import com.example.util.simpletimetracker.navigation.params.notification.ToastParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeRecordTagViewModel @Inject constructor(
    private val router: Router,
    private val recordTypesViewDataInteractor: RecordTypesViewDataInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val changeRecordTagMapper: ChangeRecordTagMapper,
    private val resourceRepo: ResourceRepo
) : ViewModel() {

    lateinit var extra: ChangeCategoryParams

    val preview: LiveData<CategoryViewData.Record> by lazy {
        return@lazy MutableLiveData<CategoryViewData.Record>().let { initial ->
            viewModelScope.launch {
                initializePreviewViewData()
                initial.value = loadPreviewViewData()
            }
            initial
        }
    }
    val types: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadTypesViewData() }
            initial
        }
    }
    val flipTypesChooser: LiveData<Boolean> = MutableLiveData()
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val deleteIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTagId != 0L) }
    val keyboardVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTagId == 0L) }
    val typesChooserVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTagId == 0L) }

    private val recordTagId: Long get() = (extra as? ChangeCategoryParams.Change)?.id.orZero()
    private var newName: String = ""
    private var newTypeId: Long = 0L

    fun onNameChange(name: String) {
        viewModelScope.launch {
            if (name != newName) {
                newName = name
                updatePreview()
            }
        }
    }

    fun onTypeChooserClick() {
        (keyboardVisibility as MutableLiveData).value = false
        (flipTypesChooser as MutableLiveData).value = flipTypesChooser.value
            ?.flip().orTrue()
    }

    fun onTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            if (item.id != newTypeId) {
                newTypeId = item.id
                updatePreview()
            }
        }
    }

    fun onDeleteClick() {
        (deleteButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            if (recordTagId != 0L) {
                recordTagInteractor.archive(recordTagId)
                showMessage(R.string.change_category_archived)
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
        }
    }

    fun onSaveClick() {
        if (newName.isEmpty()) {
            showMessage(R.string.change_category_message_choose_name)
            return
        }
        if (newTypeId == 0L) {
            showMessage(R.string.change_category_message_choose_type)
            return
        }
        (saveButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            // Zero id creates new record
            RecordTag(
                id = recordTagId,
                typeId = newTypeId,
                name = newName,
                color = 0,
            ).let {
                recordTagInteractor.add(it)
                notificationTypeInteractor.checkAndShow(recordTagId)
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
        }
    }

    private suspend fun initializePreviewViewData() {
        recordTagInteractor.get(recordTagId)?.let {
            newTypeId = it.typeId
            newName = it.name
        }
    }

    private fun updatePreview() = viewModelScope.launch {
        (preview as MutableLiveData).value = loadPreviewViewData()
    }

    private suspend fun loadPreviewViewData(): CategoryViewData.Record {
        val tag = RecordTag(
            name = newName,
            color = 0,
            typeId = newTypeId
        )
        val type = recordTypeInteractor.get(newTypeId)
        val isDarkTheme = prefsInteractor.getDarkMode()

        return if (type == null) {
            changeRecordTagMapper.mapRecordTagUntyped(
                tag = tag,
                isDarkTheme = isDarkTheme
            )
        } else {
            categoryViewDataMapper.mapRecordTag(
                tag = tag,
                type = type,
                isDarkTheme = isDarkTheme
            )
        }
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        return recordTypesViewDataInteractor.getTypesViewData()
    }

    private fun showMessage(stringResId: Int) {
        val params = ToastParams(message = resourceRepo.getString(stringResId))
        router.show(params)
    }
}

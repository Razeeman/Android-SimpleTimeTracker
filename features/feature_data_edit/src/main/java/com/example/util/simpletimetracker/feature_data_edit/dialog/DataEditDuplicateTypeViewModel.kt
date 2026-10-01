package com.example.util.simpletimetracker.feature_data_edit.dialog

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.activityFilter.interactor.ActivityFilterInteractor
import com.example.util.simpletimetracker.domain.activitySuggestion.interactor.ActivitySuggestionInteractor
import com.example.util.simpletimetracker.domain.category.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_data_edit.R
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class DataEditDuplicateTypeViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val activityFilterInteractor: ActivityFilterInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val activitySuggestionInteractor: ActivitySuggestionInteractor,
) : ViewModel() {

    val viewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadViewData()
            }
            initial
        }
    }

    fun onItemClick(item: RecordTypeViewData) {
        val params = StandardDialogParams(
            tag = ALERT_DIALOG_TAG,
            data = AlertDialogData(item.id),
            title = resourceRepo.getString(R.string.change_record_duplicate),
            btnPositive = resourceRepo.getString(R.string.ok),
            btnNegative = resourceRepo.getString(R.string.cancel),
        )
        router.navigate(params)
    }

    fun onPositiveDialogClick(tag: String?, data: Any?) {
        if (tag == ALERT_DIALOG_TAG && data is AlertDialogData) {
            onDuplicateClick(data.typeId)
        }
    }

    private fun onDuplicateClick(typeId: Long) {
        viewModelScope.launch {
            val type = recordTypeInteractor.get(typeId) ?: return@launch
            val addedId = duplicateRecordType(type)
            recordTypeCategoryInteractor.getCategories(type.id).let { categories ->
                recordTypeCategoryInteractor.addCategories(
                    typeId = addedId,
                    categoryIds = categories.toList(),
                )
            }
            recordTypeGoalInteractor.getByType(type.id).forEach { goal ->
                val newGoal = goal.copy(
                    id = 0L,
                    idData = RecordTypeGoal.IdData.Type(addedId),
                )
                recordTypeGoalInteractor.add(newGoal)
            }
            activityFilterInteractor.getByTypeId(type.id).forEach { filter ->
                val newFilter = filter.copy(
                    // Use saved id - add to already existing filter.
                    selectedIds = (filter.selectedIds + addedId).toSet(),
                )
                activityFilterInteractor.add(newFilter)
            }
            activitySuggestionInteractor.getAll()
                .firstOrNull { it.forTypeId == type.id }
                ?.copy(id = 0, forTypeId = addedId) // Zero creates new record.
                ?.let { activitySuggestionInteractor.add(listOf(it)) }
            // Reload again to get actual data with new duplicated activity.
            activitySuggestionInteractor.getAll()
                .mapNotNull {
                    // Add right after the duplicated activity.
                    val currentTypeIdIndex = it.suggestionIds.indexOf(type.id)
                    if (currentTypeIdIndex != -1) {
                        val newSuggests = it.suggestionIds.toMutableList()
                        newSuggests.add(currentTypeIdIndex + 1, addedId)
                        it.copy(suggestionIds = newSuggests.toSet())
                    } else {
                        return@mapNotNull null
                    }
                }
                .takeIf { it.isNotEmpty() }
                ?.let { activitySuggestionInteractor.add(it) }
            updateViewData()
        }
    }

    private suspend fun duplicateRecordType(type: RecordType): Long {
        // Copy will have a name like "type (2)",
        // if already exist - "type (3)" etc.
        val typeNames = recordTypeInteractor.getAll().map { it.name }
        var index = 2
        var name: String

        while (true) {
            name = "${type.name} ($index)"
            if (name in typeNames && index < 100) {
                index += 1
            } else {
                break
            }
        }

        val recordType = RecordType(
            name = name,
            icon = type.icon,
            color = type.color,
            defaultDuration = type.defaultDuration,
            note = type.note,
        )

        return recordTypeInteractor.add(recordType)
    }

    private suspend fun updateViewData() {
        viewData.set(loadViewData())
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        val result: MutableList<ViewHolderType> = mutableListOf()

        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        val typesViewData = recordTypeInteractor.getAll().filter {
            !it.hidden
        }.map { type ->
            recordTypeViewDataMapper.map(
                recordType = type,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
                checkStates = emptyList(),
                isComplete = false,
            )
        }

        if (typesViewData.isNotEmpty()) {
            result += typesViewData
        } else {
            result += HintViewData(resourceRepo.getString(R.string.record_types_empty))
        }

        return result
    }

    @Parcelize
    private data class AlertDialogData(
        val typeId: Long,
    ) : Parcelable

    companion object {
        private const val ALERT_DIALOG_TAG = "alert_dialog_tag"
    }
}

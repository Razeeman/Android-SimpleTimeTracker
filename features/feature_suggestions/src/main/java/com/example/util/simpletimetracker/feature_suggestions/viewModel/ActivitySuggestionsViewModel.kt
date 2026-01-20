package com.example.util.simpletimetracker.feature_suggestions.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.activitySuggestion.interactor.ActivitySuggestionInteractor
import com.example.util.simpletimetracker.domain.activitySuggestion.model.ActivitySuggestion
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_suggestions.viewData.ActivitySuggestionsButtonViewData
import com.example.util.simpletimetracker.feature_suggestions.R
import com.example.util.simpletimetracker.feature_suggestions.adapter.ActivitySuggestionListViewData
import com.example.util.simpletimetracker.feature_suggestions.adapter.ActivitySuggestionSpecialViewData
import com.example.util.simpletimetracker.feature_suggestions.interactor.ActivitySuggestionsCalculateInteractor
import com.example.util.simpletimetracker.feature_suggestions.interactor.ActivitySuggestionsViewDataInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivitySuggestionsViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val activitySuggestionInteractor: ActivitySuggestionInteractor,
    private val activitySuggestionsViewDataInteractor: ActivitySuggestionsViewDataInteractor,
    private val activitySuggestionsCalculateInteractor: ActivitySuggestionsCalculateInteractor,
    private val updateExternalViewsInteractor: UpdateExternalViewsInteractor,
) : BaseViewModel() {

    val viewData: LiveData<List<ViewHolderType>> by lazySuspend {
        listOf(LoaderViewData()).also { initialize() }
    }

    private var suggestions: Map<Long, Set<Long>> = emptyMap()
    private var selectingSuggestionsForTypeId: Long = 0L
    private var loadJob: Job? = null

    fun onTypesSelected(typeIds: List<Long>, tag: String?) {
        when (tag) {
            ACTIVITY_SUGGESTIONS_TYPE_SELECTION_TAG -> {
                onNewTypesSelected(
                    typeIds = typeIds,
                )
            }
            ACTIVITY_SUGGESTIONS_SUGGESTION_SELECTION_TAG -> {
                onSuggestionsForTypeChanged(
                    forTypeId = selectingSuggestionsForTypeId,
                    newSuggestions = typeIds.toSet(),
                )
            }
        }
    }

    fun onSpecialSuggestionClick(
        item: ActivitySuggestionSpecialViewData,
    ) = viewModelScope.launch {
        when (item.id.type) {
            is ActivitySuggestionSpecialViewData.Type.Add -> {
                val forTypeId = item.id.forTypeId
                selectingSuggestionsForTypeId = forTypeId
                TypesSelectionDialogParams(
                    tag = ACTIVITY_SUGGESTIONS_SUGGESTION_SELECTION_TAG,
                    title = resourceRepo.getString(R.string.change_record_message_choose_type),
                    subtitle = "",
                    type = TypesSelectionDialogParams.Type.Activity,
                    selectedTypeIds = suggestions[forTypeId].orEmpty().toList(),
                    selectedTagValues = emptyList(),
                    isMultiSelectAvailable = true,
                    idsShouldBeVisible = emptyList(),
                    showHints = true,
                    allowTagValueSelection = false,
                ).let(router::navigate)
            }
            is ActivitySuggestionSpecialViewData.Type.Calculate -> viewModelScope.launch {
                val forTypeId = item.id.forTypeId
                val newData = activitySuggestionsCalculateInteractor
                    .execute(listOf(forTypeId))
                    .firstOrNull { it.typeId == forTypeId }
                    ?.suggestions
                    .orEmpty()
                onSuggestionsForTypeChanged(
                    forTypeId = forTypeId,
                    newSuggestions = newData,
                )
            }
        }
    }

    fun onItemButtonClick(viewData: ButtonViewData) {
        val id = viewData.id as? ActivitySuggestionsButtonViewData ?: return
        when (id.block) {
            ActivitySuggestionsButtonViewData.Block.ADD -> {
                TypesSelectionDialogParams(
                    tag = ACTIVITY_SUGGESTIONS_TYPE_SELECTION_TAG,
                    title = resourceRepo.getString(R.string.change_record_message_choose_type),
                    subtitle = resourceRepo.getString(R.string.activity_suggestions_select_activity_hint),
                    type = TypesSelectionDialogParams.Type.Activity,
                    selectedTypeIds = suggestions.keys.toList(),
                    selectedTagValues = emptyList(),
                    isMultiSelectAvailable = true,
                    idsShouldBeVisible = emptyList(),
                    showHints = true,
                    allowTagValueSelection = false,
                ).let(router::navigate)
            }
            ActivitySuggestionsButtonViewData.Block.CALCULATE -> viewModelScope.launch {
                val selectedTypeIds = suggestions.keys.toList()
                val calculated = activitySuggestionsCalculateInteractor.execute(selectedTypeIds)
                    .associateBy { it.typeId }
                suggestions = suggestions.map { (typeId, _) ->
                    val newSuggestions = calculated[typeId]?.suggestions.orEmpty()
                    typeId to newSuggestions
                }.toMap()
                updateViewData()
            }
        }
    }

    fun onItemMoved(
        items: List<ViewHolderType>,
        toPosition: Int,
    ) {
        val movedItem = items.getOrNull(toPosition)
            as? ActivitySuggestionListViewData ?: return
        val forTypeId = movedItem.id.forTypeId
        val newOrder = items
            .filterIsInstance<ActivitySuggestionListViewData>()
            .filter { it.id.forTypeId == forTypeId }
        val newSuggestions = suggestions[forTypeId].orEmpty().sortedBy { suggestionId ->
            newOrder.indexOfFirst { it.id.suggestionTypeId == suggestionId }
        }.toSet()
        suggestions = suggestions.toMutableMap().apply {
            put(forTypeId, newSuggestions)
        }
    }

    fun onSaveClick() {
        viewModelScope.launch {
            // Remove all.
            activitySuggestionInteractor.getAll().map {
                it.id
            }.let {
                activitySuggestionInteractor.remove(it)
            }
            // Add new.
            suggestions.map { (typeId, suggestions) ->
                ActivitySuggestion(
                    id = 0L,
                    forTypeId = typeId,
                    suggestionIds = suggestions,
                )
            }.let {
                activitySuggestionInteractor.add(it)
            }
            updateExternalViewsInteractor.onActivitySuggestionsChanged()
            router.back()
        }
    }

    private fun onNewTypesSelected(
        typeIds: List<Long>,
    ) {
        suggestions = typeIds.associateWith { typeId ->
            suggestions[typeId].orEmpty()
        }
        updateViewData()
    }

    private fun onSuggestionsForTypeChanged(
        forTypeId: Long,
        newSuggestions: Set<Long>,
    ) {
        suggestions = suggestions.toMutableMap().apply {
            put(forTypeId, newSuggestions)
        }
        updateViewData()
    }

    private fun initialize() = viewModelScope.launch {
        suggestions = activitySuggestionInteractor.getAll().associate {
            it.forTypeId to it.suggestionIds
        }
        updateViewData()
    }

    private fun updateViewData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val data = loadViewData()
            delayLoad()
            viewData.set(data)
        }
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        return activitySuggestionsViewDataInteractor.getViewData(
            suggestionsMap = suggestions,
        )
    }

    companion object {
        private const val ACTIVITY_SUGGESTIONS_TYPE_SELECTION_TAG =
            "ACTIVITY_SUGGESTIONS_TYPE_SELECTION_TAG"
        private const val ACTIVITY_SUGGESTIONS_SUGGESTION_SELECTION_TAG =
            "ACTIVITY_SUGGESTIONS_SUGGESTION_SELECTION_TAG"
    }
}

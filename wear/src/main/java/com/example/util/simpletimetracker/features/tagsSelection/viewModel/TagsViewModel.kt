/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.features.tagsSelection.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.ShouldCloseAfterOneTagInteractor
import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.extension.removeIf
import com.example.util.simpletimetracker.domain.mediator.StartActivityMediator
import com.example.util.simpletimetracker.domain.model.WearRecordTag
import com.example.util.simpletimetracker.domain.model.WearSettings
import com.example.util.simpletimetracker.domain.model.WearTag
import com.example.util.simpletimetracker.features.tagValueSelection.interactor.TagValueSelectedInteractor
import com.example.util.simpletimetracker.features.tagsSelection.mapper.TagsViewDataMapper
import com.example.util.simpletimetracker.features.tagsSelection.screen.TagListState
import com.example.util.simpletimetracker.features.tagsSelection.ui.TagsLoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val wearDataRepo: WearDataRepo,
    private val startActivityMediator: StartActivityMediator,
    private val tagsViewDataMapper: TagsViewDataMapper,
    private val tagValueSelectedInteractor: TagValueSelectedInteractor,
    private val shouldCloseAfterOneTagInteractor: ShouldCloseAfterOneTagInteractor,
) : ViewModel() {

    val state: StateFlow<TagListState> get() = _state.asStateFlow()
    private val _state: MutableStateFlow<TagListState> = MutableStateFlow(TagListState.Loading)

    val effects: SharedFlow<Effect> get() = _effects.asSharedFlow()
    private val _effects = MutableSharedFlow<Effect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var isInitialized = false
    private var activityId: Long? = null
    private var tags: List<WearTag> = emptyList()
    private var selectedTags: List<WearRecordTag> = emptyList()
    private var settings: WearSettings? = null
    private var isMultipleChoiceAvailable: Boolean = true

    // TODO switch to savedStateHandle
    fun init(activityId: Long) {
        if (isInitialized) return
        this.activityId = activityId
        loadData()
        subscribeToUpdates()
        isInitialized = true
    }

    fun onButtonClick(buttonType: TagListState.Item.ButtonType) = viewModelScope.launch {
        when (buttonType) {
            is TagListState.Item.ButtonType.Untagged -> {
                selectedTags = emptyList()
                if (!isMultipleChoiceAvailable) {
                    val loadingState = TagsLoadingState.LoadingButton(buttonType)
                    startActivity(loadingState)
                } else {
                    _state.value = mapState()
                }
            }
            is TagListState.Item.ButtonType.Complete -> {
                val loadingState = TagsLoadingState.LoadingButton(buttonType)
                startActivity(loadingState)
            }
        }
    }

    fun onToggleClick(tagId: Long) = viewModelScope.launch {
        val currentSelectedTags = selectedTags.toMutableList()
        val selectedTagIds = selectedTags.map { it.tagId }
        val isSelected = tagId in selectedTagIds

        if (!isSelected) {
            _state.value = mapState(TagsLoadingState.LoadingTag(tagId))
            val needValueSelection = wearDataRepo.loadShouldShowTagValueSelection(
                selectedTagIds = selectedTagIds,
                clickedTagId = tagId,
            )
            _state.value = mapState()
            if (needValueSelection.isFailure) {
                showError()
                return@launch
            }
            if (needValueSelection.getOrNull().orFalse()) {
                openTagValueSelection(tagId)
            } else {
                selectedTags = currentSelectedTags.addOrRemove(tagId)
                onTagSelected(tagId)
            }
        } else {
            selectedTags = currentSelectedTags.addOrRemove(tagId)
            onTagSelected(tagId)
        }
    }

    fun onRefresh() {
        loadData()
    }

    private fun loadData() = viewModelScope.launch {
        val activityId = this@TagsViewModel.activityId ?: return@launch

        val settingsResult = wearDataRepo.loadSettings(forceReload = false).getOrNull()
        val tagsResult = wearDataRepo.loadTagsForActivity(activityId).getOrNull()

        if (settingsResult != null && tagsResult != null) {
            settings = settingsResult
            tags = tagsResult
            selectedTags = tags.filter { it.preselected }.map { WearRecordTag(it.id, null) }
            val shouldCloseAfterOne = shouldCloseAfterOneTagInteractor.execute(
                typeId = activityId,
                closeAfterOne = settings?.recordTagSelectionCloseAfterOne.orFalse(),
                excludedActivities = settings?.closeAfterOneTagExcludeActivities.orEmpty(),
            )
            isMultipleChoiceAvailable = selectedTags.isNotEmpty() || !shouldCloseAfterOne
            _state.value = mapState()
        } else {
            showError()
        }
    }

    private suspend fun onTagSelected(tagId: Long) {
        if (!isMultipleChoiceAvailable) {
            val loadingState = TagsLoadingState.LoadingTag(tagId)
            startActivity(loadingState)
        } else {
            _state.value = mapState()
        }
    }

    private suspend fun openTagValueSelection(tagId: Long) {
        _effects.emit(Effect.OnRequestTagValueSelection(tagId))
    }

    private fun onTagValueSelected(
        result: TagValueSelectedInteractor.Result,
    ) = viewModelScope.launch {
        val tagId = result.tagId
        val value = result.value
        selectedTags = selectedTags + WearRecordTag(tagId = tagId, numericValue = value)
        onTagSelected(tagId)
    }

    private suspend fun startActivity(
        loadingState: TagsLoadingState,
    ) {
        val activityId = this@TagsViewModel.activityId ?: return

        _state.value = mapState(loadingState)

        val result = startActivityMediator.start(
            activityId = activityId,
            tags = selectedTags,
        )
        if (result.isFailure) {
            showError()
        } else {
            _effects.emit(Effect.OnComplete)
        }
    }

    private fun showError() {
        _state.value = tagsViewDataMapper.mapErrorState()
    }

    private fun mapState(
        loadingState: TagsLoadingState = TagsLoadingState.NotLoading,
    ): TagListState {
        return tagsViewDataMapper.mapState(
            tags = tags,
            selectedTags = selectedTags,
            loadingState = loadingState,
            multipleChoiceAvailable = isMultipleChoiceAvailable,
        )
    }

    private fun subscribeToUpdates() {
        viewModelScope.launch {
            tagValueSelectedInteractor.data.collect(::onTagValueSelected)
        }
    }

    private fun List<WearRecordTag>.addOrRemove(itemId: Long): List<WearRecordTag> {
        val ids = this.map { it.tagId }
        val tag = WearRecordTag(tagId = itemId, numericValue = null)
        return if (itemId in ids) {
            removeIf { it.tagId == itemId }
        } else {
            toMutableList().apply { add(tag) }
        }
    }

    sealed interface Effect {
        data object OnComplete : Effect
        data class OnRequestTagValueSelection(val tagId: Long) : Effect
    }
}
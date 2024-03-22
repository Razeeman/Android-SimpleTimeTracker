/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.tagsSelection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.presentation.ui.components.TagListState
import com.example.util.simpletimetracker.domain.mediator.CurrentActivitiesMediator
import com.example.util.simpletimetracker.wear_api.WearSettings
import com.example.util.simpletimetracker.wear_api.WearTag
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
    private val currentActivitiesMediator: CurrentActivitiesMediator,
    private val tagsViewDataMapper: TagsViewDataMapper,
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
    private var selectedTagsIds: List<Long> = emptyList()
    private var settings: WearSettings = WearSettings(
        allowMultitasking = false,
        showRecordTagSelection = false,
        recordTagSelectionCloseAfterOne = false,
        recordTagSelectionExcludedActivities = emptyList(),
    )

    // TODO switch to savedStateHandle
    fun init(activityId: Long) {
        if (isInitialized) return
        this.activityId = activityId
        loadData()
        isInitialized = true
    }

    fun onButtonClick(buttonType: TagListState.Item.ButtonType) = viewModelScope.launch {
        when (buttonType) {
            is TagListState.Item.ButtonType.Untagged -> {
                selectedTagsIds = emptyList()
                if (settings.recordTagSelectionCloseAfterOne) {
                    startActivity()
                } else {
                    _state.value = mapState()
                }
            }
            is TagListState.Item.ButtonType.Complete -> {
                startActivity()
            }
        }
    }

    fun onToggleClick(tagId: Long) = viewModelScope.launch {
        val currentSelectedTags = selectedTagsIds.toMutableList()
        selectedTagsIds = currentSelectedTags.apply {
            if (tagId in this) remove(tagId) else add(tagId)
        }

        if (settings.recordTagSelectionCloseAfterOne) {
            startActivity()
        } else {
            _state.value = mapState()
        }
    }

    fun onRefresh() {
        loadData()
    }

    private fun loadData() = viewModelScope.launch {
        val activityId = this@TagsViewModel.activityId ?: return@launch

        val settingsResult = wearDataRepo.loadSettings().getOrNull()
        val tagsResult = wearDataRepo.loadTagsForActivity(activityId).getOrNull()

        if (settingsResult != null && tagsResult != null) {
            settings = settingsResult
            tags = tagsResult
            _state.value = mapState()
        } else {
            showError()
        }
    }

    private suspend fun startActivity() {
        val activityId = this@TagsViewModel.activityId ?: return
        val result = currentActivitiesMediator.start(
            activityId = activityId,
            tags = tags.filter { it.id in selectedTagsIds },
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

    private fun mapState(): TagListState {
        return tagsViewDataMapper.mapState(
            tags = tags,
            selectedTagIds = selectedTagsIds,
            settings = settings,
        )
    }

    sealed interface Effect {
        object OnComplete : Effect
    }
}
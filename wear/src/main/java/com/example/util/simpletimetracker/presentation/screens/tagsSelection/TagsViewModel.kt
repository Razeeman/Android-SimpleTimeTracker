/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.tagsSelection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.presentation.components.TagListState
import com.example.util.simpletimetracker.domain.CurrentActivitiesMediator
import com.example.util.simpletimetracker.wear_api.WearSettings
import com.example.util.simpletimetracker.wear_api.WearTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val wearDataRepo: WearDataRepo,
    private val currentActivitiesMediator: CurrentActivitiesMediator,
    private val tagsViewDataMapper: TagsViewDataMapper,
) : ViewModel() {

    val state: MutableStateFlow<TagListState> = MutableStateFlow(TagListState.Loading)
    val effects = MutableSharedFlow<Effect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var isInitialized = false
    private var activityId: Long? = null
    private var tags: List<WearTag> = emptyList()
    private var selectedTags: List<WearTag> = emptyList()
    private var settings: WearSettings = WearSettings(
        allowMultitasking = false,
        showRecordTagSelection = false,
        recordTagSelectionCloseAfterOne = false,
        recordTagSelectionEvenForGeneralTags = false,
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
                selectedTags = emptyList()
                if (settings.recordTagSelectionCloseAfterOne) {
                    startActivity()
                } else {
                    state.value = mapState()
                }
            }
            is TagListState.Item.ButtonType.Complete -> {
                startActivity()
            }
        }
    }

    fun onToggleClick(tag: WearTag) = viewModelScope.launch {
        val currentSelectedTags = selectedTags.toMutableList()
        selectedTags = currentSelectedTags.apply {
            if (tag in this) remove(tag) else add(tag)
        }

        if (settings.recordTagSelectionCloseAfterOne) {
            startActivity()
        } else {
            state.value = mapState()
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
            state.value = mapState()
        } else {
            showError()
        }
    }

    private suspend fun startActivity() {
        val activityId = this@TagsViewModel.activityId ?: return
        val result = currentActivitiesMediator.start(
            activityId = activityId,
            tags = selectedTags,
        )
        if (result.isFailure) {
            showError()
        } else {
            effects.emit(Effect.OnComplete)
        }
    }

    private fun showError() {
        state.value = TagListState.Error(R.string.wear_loading_error)
    }

    private fun mapState(): TagListState {
        return tagsViewDataMapper.mapState(
            tags = tags,
            selectedTags = selectedTags,
            settings = settings,
        )
    }

    sealed interface Effect {
        object OnComplete : Effect
    }
}
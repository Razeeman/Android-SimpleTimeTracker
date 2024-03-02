/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.presentation.components.TagSelectionMode
import com.example.util.simpletimetracker.presentation.data.WearRPCClient
import com.example.util.simpletimetracker.presentation.mediators.CurrentActivitiesMediator
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
    private val rpc: WearRPCClient,
    private val currentActivitiesMediator: CurrentActivitiesMediator,
) : ViewModel() {

    val state = MutableStateFlow(State())
    val effects = MutableSharedFlow<Effect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var activityId: Long? = null

    fun init(activityId: Long) {
        this.activityId = activityId
        refresh()
    }

    fun onSelectionComplete(tags: List<WearTag>) {
        viewModelScope.launch {
            val activityId = this@TagsViewModel.activityId ?: return@launch
            currentActivitiesMediator.start(activityId, tags)
            effects.emit(Effect.OnComplete)
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            val activityId = this@TagsViewModel.activityId ?: return@launch
            val settings = rpc.querySettings()
            val newState = State(
                tags = rpc.queryTagsForActivity(activityId),
                mode = if (settings.recordTagSelectionCloseAfterOne) {
                    TagSelectionMode.SINGLE
                } else {
                    TagSelectionMode.MULTI
                },
                settings = settings,
            )
            state.emit(newState)
        }
    }

    data class State(
        val tags: List<WearTag> = emptyList(),
        val mode: TagSelectionMode = TagSelectionMode.SINGLE,
        val settings: WearSettings = WearSettings(
            allowMultitasking = false,
            showRecordTagSelection = false,
            recordTagSelectionCloseAfterOne = false,
            recordTagSelectionEvenForGeneralTags = false,
        ),
    )

    sealed interface Effect {
        object OnComplete : Effect
    }
}
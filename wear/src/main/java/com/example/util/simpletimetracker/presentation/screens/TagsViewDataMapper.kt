/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens

import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.presentation.components.TagListState
import com.example.util.simpletimetracker.presentation.components.TagSelectionMode
import com.example.util.simpletimetracker.presentation.theme.ColorActive
import com.example.util.simpletimetracker.presentation.theme.ColorInactive
import com.example.util.simpletimetracker.wear_api.WearSettings
import com.example.util.simpletimetracker.wear_api.WearTag
import javax.inject.Inject

class TagsViewDataMapper @Inject constructor() {

    fun mapState(
        tags: List<WearTag>,
        selectedTags: List<WearTag>,
        settings: WearSettings,
    ): TagsViewModel.State {
        val listState = if (tags.isEmpty()) {
            mapEmptyState()
        } else {
            mapContentState(
                tags = tags,
                selectedTags = selectedTags,
                settings = settings,
            )
        }

        return TagsViewModel.State(
            listState = listState,
            settings = settings,
        )
    }

    private fun mapEmptyState(): TagListState.Empty {
        return TagListState.Empty(R.string.no_tags)
    }

    private fun mapContentState(
        tags: List<WearTag>,
        selectedTags: List<WearTag>,
        settings: WearSettings,
    ): TagListState.Content {
        val selectedTagIds = selectedTags.map { it.id }

        val mode = if (settings.recordTagSelectionCloseAfterOne) {
            TagSelectionMode.SINGLE
        } else {
            TagSelectionMode.MULTI
        }

        val items = tags.map {
            TagListState.Item.Tag(
                tag = it,
                selected = it.id in selectedTagIds,
            )
        }

        val buttons = if (mode == TagSelectionMode.SINGLE) {
            listOf(
                TagListState.Item.Button(
                    textResId = R.string.untagged,
                    color = ColorInactive,
                    buttonType = TagListState.Item.ButtonType.Untagged,
                ),
            )
        } else {
            listOf(
                TagListState.Item.Button(
                    textResId = R.string.untagged,
                    color = ColorInactive,
                    buttonType = TagListState.Item.ButtonType.Untagged,
                ),
                TagListState.Item.Button(
                    textResId = R.string.save,
                    color = ColorActive,
                    buttonType = TagListState.Item.ButtonType.Complete,
                ),
            )
        }

        return TagListState.Content(
            items = items + buttons,
            mode = mode,
        )
    }
}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.tagsSelection

import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.presentation.components.TagChipState
import com.example.util.simpletimetracker.presentation.components.TagListState
import com.example.util.simpletimetracker.presentation.theme.ColorActive
import com.example.util.simpletimetracker.presentation.theme.ColorInactive
import com.example.util.simpletimetracker.wear_api.WearSettings
import com.example.util.simpletimetracker.wear_api.WearTag
import javax.inject.Inject

class TagsViewDataMapper @Inject constructor() {

    fun mapErrorState(): TagListState.Error {
        return TagListState.Error(R.string.wear_loading_error)
    }

    fun mapState(
        tags: List<WearTag>,
        selectedTagIds: List<Long>,
        settings: WearSettings,
    ): TagListState {
        val listState = if (tags.isEmpty()) {
            mapEmptyState()
        } else {
            mapContentState(
                tags = tags,
                selectedTagIds = selectedTagIds,
                settings = settings,
            )
        }

        return listState
    }

    private fun mapEmptyState(): TagListState.Empty {
        return TagListState.Empty(R.string.change_record_categories_empty)
    }

    private fun mapContentState(
        tags: List<WearTag>,
        selectedTagIds: List<Long>,
        settings: WearSettings,
    ): TagListState.Content {
        val mode = if (settings.recordTagSelectionCloseAfterOne) {
            TagChipState.TagSelectionMode.SINGLE
        } else {
            TagChipState.TagSelectionMode.MULTI
        }

        val items = tags.map {
            TagListState.Item.Tag(
                tag = TagChipState(
                    id = it.id,
                    name = it.name,
                    color = it.color,
                    checked = it.id in selectedTagIds,
                    mode = mode,
                ),
            )
        }

        val buttons = if (mode == TagChipState.TagSelectionMode.SINGLE) {
            listOf(
                TagListState.Item.Button(
                    textResId = R.string.change_record_untagged,
                    color = ColorInactive,
                    buttonType = TagListState.Item.ButtonType.Untagged,
                ),
            )
        } else {
            listOf(
                TagListState.Item.Button(
                    textResId = R.string.change_record_untagged,
                    color = ColorInactive,
                    buttonType = TagListState.Item.ButtonType.Untagged,
                ),
                TagListState.Item.Button(
                    textResId = R.string.duration_dialog_save,
                    color = ColorActive,
                    buttonType = TagListState.Item.ButtonType.Complete,
                ),
            )
        }

        return TagListState.Content(
            items = items + buttons,
        )
    }
}
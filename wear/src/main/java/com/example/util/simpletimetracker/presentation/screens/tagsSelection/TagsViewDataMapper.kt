/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.tagsSelection

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.data.WearResourceRepo
import com.example.util.simpletimetracker.presentation.theme.ColorActive
import com.example.util.simpletimetracker.presentation.theme.ColorInactive
import com.example.util.simpletimetracker.presentation.ui.components.TagChipState
import com.example.util.simpletimetracker.presentation.ui.components.TagListState
import com.example.util.simpletimetracker.presentation.ui.components.TagSelectionButtonState
import com.example.util.simpletimetracker.wear_api.WearSettings
import com.example.util.simpletimetracker.wear_api.WearTag
import javax.inject.Inject

class TagsViewDataMapper @Inject constructor(
    private val resourceRepo: WearResourceRepo,
) {

    fun mapErrorState(): TagListState.Error {
        return TagListState.Error(R.string.wear_loading_error)
    }

    fun mapState(
        tags: List<WearTag>,
        selectedTagIds: List<Long>,
        settings: WearSettings,
        loadingState: TagsLoadingState,
    ): TagListState {
        val listState = if (tags.isEmpty()) {
            mapEmptyState()
        } else {
            mapContentState(
                tags = tags,
                selectedTagIds = selectedTagIds,
                settings = settings,
                loadingState = loadingState,
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
        loadingState: TagsLoadingState,
    ): TagListState.Content {
        val mode = if (settings.recordTagSelectionCloseAfterOne) {
            TagChipState.TagSelectionMode.SINGLE
        } else {
            TagChipState.TagSelectionMode.MULTI
        }

        val items = tags.map {
            val isLoading = (loadingState as? TagsLoadingState.LoadingTag)
                ?.tagId == it.id

            TagListState.Item.Tag(
                tag = TagChipState(
                    id = it.id,
                    name = it.name,
                    color = it.color,
                    checked = it.id in selectedTagIds,
                    mode = mode,
                    isLoading = isLoading,
                ),
            )
        }

        val buttons = mutableListOf<TagListState.Item>()
        if (mode == TagChipState.TagSelectionMode.SINGLE) {
            buttons += mapButton(
                textResId = R.string.change_record_untagged,
                color = ColorInactive,
                buttonType = TagListState.Item.ButtonType.Untagged,
                loadingState = loadingState,
            )
        } else {
            buttons += mapButton(
                textResId = R.string.change_record_untagged,
                color = ColorInactive,
                buttonType = TagListState.Item.ButtonType.Untagged,
                loadingState = loadingState,
            )
            buttons += mapButton(
                textResId = R.string.duration_dialog_save,
                color = ColorActive,
                buttonType = TagListState.Item.ButtonType.Complete,
                loadingState = loadingState,
            )
        }

        return TagListState.Content(
            items = items + buttons,
        )
    }

    private fun mapButton(
        @StringRes textResId: Int,
        color: Color,
        buttonType: TagListState.Item.ButtonType,
        loadingState: TagsLoadingState,
    ): TagListState.Item.Button {
        val isLoading = (loadingState as? TagsLoadingState.LoadingButton)
            ?.buttonType == buttonType

        return TagListState.Item.Button(
            TagSelectionButtonState(
                text = resourceRepo.getString(textResId),
                color = color,
                buttonType = buttonType,
                isLoading = isLoading,
            ),
        )
    }
}
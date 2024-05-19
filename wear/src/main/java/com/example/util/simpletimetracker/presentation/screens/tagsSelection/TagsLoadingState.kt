package com.example.util.simpletimetracker.presentation.screens.tagsSelection

import com.example.util.simpletimetracker.presentation.ui.components.TagListState

sealed interface TagsLoadingState {
    object NotLoading : TagsLoadingState

    data class LoadingTag(
        val tagId: Long,
    ) : TagsLoadingState

    data class LoadingButton(
        val buttonType: TagListState.Item.ButtonType,
    ) : TagsLoadingState
}
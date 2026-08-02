package com.example.util.simpletimetracker.feature_change_record.api.view

import com.example.util.simpletimetracker.feature_change_record.api.ChangeRecordEditorDelegate
import com.example.util.simpletimetracker.feature_comment_selection.api.CommentSelectionViewDelegateProvider

interface ChangeRecordViewDelegateProvider {
    fun provide(
        viewModel: ChangeRecordEditorDelegate,
        commentDelegateProvider: CommentSelectionViewDelegateProvider,
    ): ChangeRecordViewDelegate
}
package com.example.util.simpletimetracker.feature_change_record.view

import com.example.util.simpletimetracker.feature_change_record.api.ChangeRecordEditorDelegate
import com.example.util.simpletimetracker.feature_change_record.api.view.ChangeRecordViewDelegate
import com.example.util.simpletimetracker.feature_change_record.api.view.ChangeRecordViewDelegateProvider
import com.example.util.simpletimetracker.feature_comment_selection.api.CommentSelectionViewDelegateProvider
import javax.inject.Inject

class ChangeRecordViewDelegateProviderImpl @Inject constructor() : ChangeRecordViewDelegateProvider {

    override fun provide(
        viewModel: ChangeRecordEditorDelegate,
        commentDelegateProvider: CommentSelectionViewDelegateProvider,
    ): ChangeRecordViewDelegate {
        return ChangeRecordViewDelegateImpl(
            viewModel = viewModel,
            commentDelegateProvider = commentDelegateProvider,
        )
    }
}
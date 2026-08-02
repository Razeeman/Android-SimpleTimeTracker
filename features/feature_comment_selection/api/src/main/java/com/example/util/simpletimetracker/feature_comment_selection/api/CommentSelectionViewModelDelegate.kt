package com.example.util.simpletimetracker.feature_comment_selection.api

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.recordComment.RecordCommentViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData

interface CommentSelectionViewModelDelegate {
    val comments: LiveData<List<ViewHolderType>>
    var newComment: String

    fun attach(parent: Parent)
    fun clearCommentDelegate()
    fun updateCommentsViewData(fromCommentChange: Boolean = false)
    fun onCommentChange(comment: String)
    fun onCommentFilterClick(item: FilterViewData)
    fun onFavouriteCommentClick()
    fun onCommentClick(item: RecordCommentViewData)
    fun onDelegateDataSelected(tag: String?, dataIds: List<Long>)

    interface Parent {
        fun getParams(): Params
        suspend fun onCommentClick()
        fun onCommentChange()

        data class Params(
            val recordTypeId: Long?,
        )
    }
}
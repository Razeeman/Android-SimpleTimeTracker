package com.example.util.simpletimetracker.feature_comment_selection.viewModelDelegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.feature_comment_selection.interactor.CommentSelectionDelegateViewDataInteractor
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.RecordCommentSearchViewDataInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.CommentFilterSettingsTypeViewData
import com.example.util.simpletimetracker.core.viewData.CommentFilterTypeViewData
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.favourite.interactor.FavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.favourite.interactor.IsCommentFavouriteInteractor
import com.example.util.simpletimetracker.domain.favourite.interactor.RecordTypeToFavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.favourite.model.FavouriteComment
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.R
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.recordComment.RecordCommentViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import com.example.util.simpletimetracker.feature_comment_selection.api.CommentSelectionViewModelDelegate
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class CommentSelectionViewModelDelegateImpl @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val commentSelectionDelegateViewDataInteractor: CommentSelectionDelegateViewDataInteractor,
    private val recordCommentSearchViewDataInteractor: RecordCommentSearchViewDataInteractor,
    private val favouriteCommentInteractor: FavouriteCommentInteractor,
    private val recordTypeToFavouriteCommentInteractor: RecordTypeToFavouriteCommentInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val isCommentFavouriteInteractor: IsCommentFavouriteInteractor,
) : CommentSelectionViewModelDelegate,
    ViewModelDelegate() {

    override val comments: LiveData<List<ViewHolderType>> = MutableLiveData(emptyList())

    override var newComment: String = ""

    private var parent: CommentSelectionViewModelDelegate.Parent? = null
    private var commentLoadJob: Job? = null

    override fun attach(parent: CommentSelectionViewModelDelegate.Parent) {
        this.parent = parent
    }

    override fun clearCommentDelegate() {
        clear()
    }

    override fun onCommentClick(item: RecordCommentViewData) {
        delegateScope.launch {
            if (item.text == newComment) return@launch
            newComment = item.text
            parent?.onCommentClick()
            updateCommentsViewData()
        }
    }

    override fun onCommentFilterClick(item: FilterViewData) {
        delegateScope.launch {
            if (item.type is CommentFilterSettingsTypeViewData) {
                openSettings()
                return@launch
            }
            val data = item.type as? CommentFilterTypeViewData ?: return@launch
            val type = recordCommentSearchViewDataInteractor.map(data)
            val newFilters = prefsInteractor.getHiddenCommentFilters().toMutableSet()
            newFilters.addOrRemove(type)
            prefsInteractor.setHiddenCommentFilters(newFilters.toSet())
            updateCommentsViewData()
        }
    }

    override fun onFavouriteCommentClick() {
        if (newComment.isEmpty()) return

        delegateScope.launch {
            val newTypeId = parent?.getParams()?.recordTypeId.orZero()
            val isFavourite = isCommentFavouriteInteractor.execute(newComment, newTypeId)
            if (isFavourite) {
                removeComment(newComment, newTypeId)
            } else {
                addComment(newComment, newTypeId)
            }
            updateCommentsViewData()
        }
    }

    override fun onCommentChange(comment: String) {
        if (comment == newComment) return
        newComment = comment
        parent?.onCommentChange()
        updateCommentsViewData(fromCommentChange = true)
    }

    private fun openSettings() {
        delegateScope.launch {
            val comment = favouriteCommentInteractor.get(newComment) ?: return@launch
            val selectedTypeIds = recordTypeToFavouriteCommentInteractor.getTypes(comment.id)

            TypesSelectionDialogParams(
                tag = COMMENT_SELECTION_FAV_ACTIVITIES_TAG,
                title = resourceRepo.getString(R.string.change_record_favourite_activity_selection),
                subtitle = "",
                type = TypesSelectionDialogParams.Type.Activity,
                selectedTypeIds = selectedTypeIds.toList(),
                selectedTagValues = emptyList(),
                selectedTagValueOnStart = emptyList(),
                isMultiSelectAvailable = true,
                idsShouldBeVisible = emptyList(),
                showHints = true,
                allowTagValueSelection = false,
            ).let(router::navigate)
        }
    }

    // If no activity selected - tag is general - remove it.
    // If assigned only to this activity - remove it.
    // If assigned to other activities - remove only this activity.
    private suspend fun removeComment(comment: String, typeId: Long) {
        val comment = favouriteCommentInteractor.get(comment) ?: return
        if (typeId == 0L) {
            // General comment - remove it.
            favouriteCommentInteractor.remove(comment.id)
            return
        }
        val newTypeId = parent?.getParams()?.recordTypeId.orZero()
        val typesToComments = recordTypeToFavouriteCommentInteractor.getTypes(comment.id)
        val otherRelations = typesToComments.filter { it != newTypeId }
        if (typesToComments.isEmpty() || otherRelations.isEmpty()) {
            // Comment is general - remove it.
            favouriteCommentInteractor.remove(comment.id)
        } else {
            // Comment assigned to other activities - remove only relation to this activity.
            recordTypeToFavouriteCommentInteractor.removeTypes(
                commentId = comment.id,
                typeIds = listOf(newTypeId),
            )
        }
    }

    // If not existing - add new as general.
    // If existing and general - shouldn't be possible.
    // If existing and not general and no type selected - make it general.
    // If existing and not general and type selected - add current type.
    private suspend fun addComment(comment: String, typeId: Long) {
        val existingComment = favouriteCommentInteractor.get(comment)
        val commentId = if (existingComment == null) {
            // Does not exist - create as general.
            val new = FavouriteComment(comment = comment)
            favouriteCommentInteractor.add(new)
            return
        } else {
            // Already exist.
            existingComment.id
        }
        if (typeId == 0L) {
            // Make it general.
            recordTypeToFavouriteCommentInteractor.removeAll(commentId)
        } else {
            // Assign only to this activity.
            recordTypeToFavouriteCommentInteractor.addTypes(
                commentId = commentId,
                typeIds = listOf(typeId),
            )
        }
    }

    override fun updateCommentsViewData(
        fromCommentChange: Boolean,
    ) {
        commentLoadJob?.cancel()
        commentLoadJob = delegateScope.launch {
            comments.set(loadCommentsViewData(fromCommentChange))
        }
    }

    override fun onDelegateDataSelected(
        tag: String?,
        dataIds: List<Long>,
    ) {
        delegateScope.launch {
            when (tag) {
                COMMENT_SELECTION_FAV_ACTIVITIES_TAG -> {
                    val comment = favouriteCommentInteractor.get(newComment) ?: return@launch
                    recordTypeToFavouriteCommentInteractor.removeAll(comment.id)
                    recordTypeToFavouriteCommentInteractor.addTypes(comment.id, dataIds)
                    updateCommentsViewData()
                }
            }
        }
    }

    private suspend fun loadCommentsViewData(
        fromCommentChange: Boolean,
    ): List<ViewHolderType> {
        return commentSelectionDelegateViewDataInteractor.getCommentsViewData(
            comment = newComment,
            typeId = parent?.getParams()?.recordTypeId.orZero(),
            fromCommentChange = fromCommentChange,
        )
    }

    companion object {
        private const val COMMENT_SELECTION_FAV_ACTIVITIES_TAG = "COMMENT_SELECTION_FAV_ACTIVITIES_TAG"
    }
}
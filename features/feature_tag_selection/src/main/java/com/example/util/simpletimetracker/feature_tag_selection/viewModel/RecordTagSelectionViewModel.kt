package com.example.util.simpletimetracker.feature_tag_selection.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.ShouldCloseAfterOneTagInteractor
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.LoadPreselectedTagsInteractor
import com.example.util.simpletimetracker.core.interactor.RecordCommentSearchViewDataInteractor
import com.example.util.simpletimetracker.core.viewData.CommentFilterTypeViewData
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.interactor.AddTagToTypeIfNotExistMediator
import com.example.util.simpletimetracker.domain.recordTag.interactor.NeedTagValueSelectionInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordComment.RecordCommentViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import com.example.util.simpletimetracker.feature_tag_selection.interactor.RecordTagSelectionViewDataInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagValueSelectionParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordTagSelectionViewModel @Inject constructor(
    private val router: Router,
    private val viewDataInteractor: RecordTagSelectionViewDataInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val prefsInteractor: PrefsInteractor,
    private val addTagToTypeIfNotExistMediator: AddTagToTypeIfNotExistMediator,
    private val needTagValueSelectionInteractor: NeedTagValueSelectionInteractor,
    private val shouldCloseAfterOneTagInteractor: ShouldCloseAfterOneTagInteractor,
    private val loadPreselectedTagsInteractor: LoadPreselectedTagsInteractor,
    private val recordCommentSearchViewDataInteractor: RecordCommentSearchViewDataInteractor,
) : BaseViewModel() {

    lateinit var extra: RecordTagSelectionParams
    var softInputMode: Int? = null

    val viewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadViewData(fromCommentChange = false)
            }
            initial
        }
    }
    val saveButtonVisibility: LiveData<Boolean> = MutableLiveData()
    val saveClicked: LiveData<Unit> = MutableLiveData()

    private var newComment: String = ""
    private var newTags: List<RecordBase.Tag> = emptyList()
    private var initialDataLoaded: Boolean = false
    private var searchLoadJob: Job? = null
    private var isMultipleChoiceAvailable: Boolean = true

    // Keep in mind that tags would be added to new types only if show all was selected before,
    // for optimisation reasons, to not call on every save.
    private var showAllTags: Boolean = false

    fun onCategoryClick(item: CategoryViewData) {
        viewModelScope.launch {
            when (item) {
                is CategoryViewData.Record.Tagged -> {
                    val needValueSelection = needTagValueSelectionInteractor.execute(
                        selectedTagIds = newTags.map { it.tagId },
                        clickedTagId = item.id,
                    )
                    if (needValueSelection) {
                        RecordTagValueSelectionParams(
                            tag = RECORD_TAG_SELECTION_TAG_VALUE_SELECTION,
                            tagId = item.id,
                        ).let(router::navigate)
                    } else {
                        newTags = newTags.addOrRemove(item.id)
                        onTagSelected()
                    }
                }
                is CategoryViewData.Record.Untagged -> {
                    newTags = emptyList()
                    onTagSelected()
                }
                else -> return@launch
            }
        }
    }

    fun onCategoryValueSelected(
        params: RecordTagValueSelectionParams,
        value: Double,
    ) {
        if (params.tag != RECORD_TAG_SELECTION_TAG_VALUE_SELECTION) return
        viewModelScope.launch {
            newTags = newTags + RecordBase.Tag(tagId = params.tagId, numericValue = value)
            onTagSelected()
        }
    }

    fun onShowAllTagsClick() = viewModelScope.launch {
        showAllTags = true
        updateViewData()
    }

    fun onSaveClick() {
        viewModelScope.launch {
            saveClicked()
        }
    }

    fun onCommentClick(item: RecordCommentViewData) {
        if (item.text != newComment) {
            newComment = item.text
            updateViewData()
        }
    }

    fun onCommentFilterClick(item: FilterViewData) = viewModelScope.launch {
        val data = item.type as? CommentFilterTypeViewData ?: return@launch
        val type = recordCommentSearchViewDataInteractor.map(data)
        val newFilters = prefsInteractor.getHiddenCommentFilters().toMutableSet()
        newFilters.addOrRemove(type)
        prefsInteractor.setHiddenCommentFilters(newFilters.toSet())
        updateViewData()
    }

    fun onCommentChange(comment: String) {
        if (comment != newComment) {
            newComment = comment
            updateViewData(fromCommentChange = true)
        }
    }

    private suspend fun onTagSelected() {
        if (isMultipleChoiceAvailable) updateViewData() else saveClicked()
    }

    private suspend fun saveClicked() {
        addRunningRecordMediator.startTimer(
            typeId = extra.typeId,
            tags = newTags,
            comment = newComment,
        )
        if (showAllTags) {
            addTagToTypeIfNotExistMediator.execute(
                typeId = extra.typeId,
                tagIds = newTags.map(RecordBase.Tag::tagId),
            )
        }
        saveClicked.set(Unit)
    }

    // TODO TAG add check retroactive mode to loaded preselected tags?
    // TODO TAG ability to deselect preselected tags
    private suspend fun initializeData() {
        if (initialDataLoaded) return
        val initialIds = loadPreselectedTagsInteractor.execute(extra.typeId)
        if (initialIds.isNotEmpty()) {
            newTags = initialIds.map { RecordBase.Tag(tagId = it, numericValue = null) }
        }
        val shouldCloseAfterOne = shouldCloseAfterOneTagInteractor.execute(
            typeId = extra.typeId,
            closeAfterOne = prefsInteractor.getRecordTagSelectionCloseAfterOne(),
            excludedActivities = prefsInteractor.getCloseAfterOneTagExcludeActivities().toSet(),
        )
        // If there are preselected tags - ignore setting.
        isMultipleChoiceAvailable = newTags.isNotEmpty() || !shouldCloseAfterOne
        updateButtonVisibility()
        initialDataLoaded = true
    }

    private fun updateButtonVisibility() {
        saveButtonVisibility.set(loadButtonVisibility())
    }

    private fun loadButtonVisibility(): Boolean {
        val showTags = RecordTagSelectionParams.Field.Tags in extra.fields
        val showCommentInput = RecordTagSelectionParams.Field.Comment in extra.fields

        return when {
            showTags -> isMultipleChoiceAvailable
            showCommentInput -> true
            else -> false
        }
    }

    private fun updateViewData(
        fromCommentChange: Boolean = false,
    ) {
        searchLoadJob?.cancel()
        searchLoadJob = viewModelScope.launch {
            val data = loadViewData(fromCommentChange)
            viewData.set(data)
        }
    }

    private suspend fun loadViewData(
        fromCommentChange: Boolean,
    ): List<ViewHolderType> {
        initializeData()

        return viewDataInteractor.getViewData(
            extra = extra,
            selectedTags = newTags,
            showAllTags = showAllTags,
            multipleChoiceAvailable = isMultipleChoiceAvailable,
            comment = newComment,
            fromCommentChange = fromCommentChange,
        )
    }

    companion object {
        private const val RECORD_TAG_SELECTION_TAG_VALUE_SELECTION = "RECORD_TAG_SELECTION_TAG_VALUE_SELECTION"
    }
}

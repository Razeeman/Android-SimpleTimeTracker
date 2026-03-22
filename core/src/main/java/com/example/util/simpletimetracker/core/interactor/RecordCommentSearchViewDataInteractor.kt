package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.CommentFilterTypeViewData
import com.example.util.simpletimetracker.domain.base.CommentFilterType
import com.example.util.simpletimetracker.domain.favourite.interactor.FavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.favourite.interactor.RecordTypeToFavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.EmptySpaceViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordComment.RecordCommentViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecordCommentSearchViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val colorMapper: ColorMapper,
    private val prefsInteractor: PrefsInteractor,
    private val recordInteractor: RecordInteractor,
    private val favouriteCommentInteractor: FavouriteCommentInteractor,
    private val recordTypeToFavouriteCommentInteractor: RecordTypeToFavouriteCommentInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
) {

    suspend fun getViewData(
        comment: String,
        typeId: Long,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val disabledFilters = prefsInteractor.getHiddenCommentFilters()
        val result = mutableListOf<ViewHolderType>()

        val similar = getSimilarData(comment)
        val favourite = getFavouriteData(typeId)
        val last = getLastCommentsData(typeId)

        val filters = getFilters(
            similar = similar,
            favourite = favourite,
            last = last,
            disabledFilters = disabledFilters,
        )

        val needToShowHint = filters.filter { it.selected }.size > 1

        result += filters
        if (CommentFilterType.Similar !in disabledFilters) {
            result += addHint(similar, needToShowHint, R.string.change_record_similar_comments_hint)
        }
        if (CommentFilterType.Favourite !in disabledFilters) {
            result += addHint(favourite, needToShowHint, R.string.change_record_favourite_comments_hint)
        }
        if (CommentFilterType.Last !in disabledFilters) {
            result += addHint(last, needToShowHint, R.string.change_record_last_comments_hint)
        }

        return@withContext result
    }

    fun map(data: CommentFilterTypeViewData): CommentFilterType {
        return when (data) {
            is CommentFilterTypeViewData.Similar -> CommentFilterType.Similar
            is CommentFilterTypeViewData.Favourite -> CommentFilterType.Favourite
            is CommentFilterTypeViewData.Last -> CommentFilterType.Last
        }
    }

    private suspend fun getFilters(
        similar: List<ViewHolderType>,
        favourite: List<ViewHolderType>,
        last: List<ViewHolderType>,
        disabledFilters: Set<CommentFilterType>,
    ): List<FilterViewData> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        val result = mutableListOf<FilterViewData>()

        if (similar.isNotEmpty()) {
            result += mapFilterViewData(
                type = CommentFilterType.Similar,
                disabledFilters = disabledFilters,
                isDarkTheme = isDarkTheme,
            )
        }
        if (favourite.isNotEmpty()) {
            result += mapFilterViewData(
                type = CommentFilterType.Favourite,
                disabledFilters = disabledFilters,
                isDarkTheme = isDarkTheme,
            )
        }
        if (last.isNotEmpty()) {
            result += mapFilterViewData(
                type = CommentFilterType.Last,
                disabledFilters = disabledFilters,
                isDarkTheme = isDarkTheme,
            )
        }

        return result
    }

    private suspend fun getSimilarData(
        comment: String,
    ): List<ViewHolderType> {
        return if (comment.isNotEmpty()) {
            recordInteractor.searchComment(comment)
                .sortedByDescending { it.timeStarted }
                .distinctBy { it.comment }
                .take(SIMILAR_COMMENTS_TO_SHOW)
                .mapNotNull {
                    if (it.comment == comment) return@mapNotNull null
                    RecordCommentViewData.Last(it.comment)
                }
        } else {
            emptyList()
        }
    }

    private suspend fun getFavouriteData(
        typeId: Long,
    ): List<ViewHolderType> {
        val comments = favouriteCommentInteractor.getAll()
        return recordTypeToFavouriteCommentInteractor.filterFavourites(typeId, comments)
            .map { RecordCommentViewData.Favourite(it.comment) }
    }

    private suspend fun getLastCommentsData(
        typeId: Long,
    ): List<ViewHolderType> {
        data class Data(val timeStarted: Long, val comment: String)

        val records = recordInteractor.getByTypeWithAnyComment(listOf(typeId))
            .map { Data(it.timeStarted, it.comment) }
        val runningRecords = runningRecordInteractor.getAll()
            .filter { it.id == typeId && it.comment.isNotEmpty() }
            .map { Data(it.timeStarted, it.comment) }

        return (records + runningRecords)
            .sortedByDescending { it.timeStarted }
            .distinctBy { it.comment }
            .take(LAST_COMMENTS_TO_SHOW)
            .map { RecordCommentViewData.Last(it.comment) }
    }

    private fun mapFilterViewData(
        type: CommentFilterType,
        disabledFilters: Set<CommentFilterType>,
        isDarkTheme: Boolean,
    ): FilterViewData {
        val selected = type !in disabledFilters

        val name = when (type) {
            is CommentFilterType.Similar -> R.string.change_record_similar_comments_hint
            is CommentFilterType.Favourite -> R.string.change_record_favourite_comments_hint
            is CommentFilterType.Last -> R.string.change_record_last_comments_hint
        }.let(resourceRepo::getString)

        return FilterViewData(
            id = type.hashCode().toLong(),
            type = mapTypeViewData(type),
            name = name,
            color = if (selected) {
                colorMapper.toActiveColor(isDarkTheme)
            } else {
                colorMapper.toInactiveColor(isDarkTheme)
            },
            selected = selected,
            removeBtnVisible = false,
        )
    }

    private fun mapTypeViewData(
        type: CommentFilterType,
    ): CommentFilterTypeViewData {
        return when (type) {
            is CommentFilterType.Similar -> CommentFilterTypeViewData.Similar
            is CommentFilterType.Favourite -> CommentFilterTypeViewData.Favourite
            is CommentFilterType.Last -> CommentFilterTypeViewData.Last
        }
    }

    private fun addHint(
        data: List<ViewHolderType>,
        needToShowHint: Boolean,
        hintResId: Int,
    ): List<ViewHolderType> {
        val hint = if (needToShowHint) {
            val text = resourceRepo.getString(hintResId)
            HintViewData(text)
        } else {
            EmptySpaceViewData(
                id = "comment_filters_empty_space".hashCode().toLong(),
                width = EmptySpaceViewData.ViewDimension.MatchParent,
                height = EmptySpaceViewData.ViewDimension.ExactSizeDp(8),
            )
        }
        return data.takeUnless { it.isEmpty() }?.let { listOf(hint) + it }.orEmpty()
    }

    companion object {
        private const val LAST_COMMENTS_TO_SHOW = 20
        private const val SIMILAR_COMMENTS_TO_SHOW = 100
    }
}
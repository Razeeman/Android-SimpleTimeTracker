package com.example.util.simpletimetracker.feature_categories.interactor

import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.category.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.category.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToDefaultTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.EmptySpaceViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_categories.R
import com.example.util.simpletimetracker.feature_categories.viewData.CategoriesViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoriesViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val recordTypeToDefaultTagInteractor: RecordTypeToDefaultTagInteractor,
    private val recordTypeToTagInteractor: RecordTypeToTagInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
) {

    suspend fun getViewData(
        selectedTypeIds: List<Long>,
        searchEnabled: Boolean,
        searchText: String,
        navBarHeightDp: Int,
    ): CategoriesViewData = coroutineScope {
        val isSearching: Boolean = searchEnabled && searchText.isNotEmpty()

        val typeTags = async {
            getCategoriesViewData(
                selectedTypeIds = selectedTypeIds,
                searchText = searchText,
                isSearching = isSearching,
            )
        }
        val recordTags = async {
            getRecordTagViewData(
                selectedTypeIds = selectedTypeIds,
                searchText = searchText,
                isSearching = isSearching,
            )
        }

        val items = typeTags.await().items +
            DividerViewData(1) +
            recordTags.await().items +
            getBottomEmptySpace(navBarHeightDp)

        val showHint = typeTags.await().showHint ||
            recordTags.await().showHint

        return@coroutineScope CategoriesViewData(
            items = items,
            showHint = showHint,
        )
    }

    private suspend fun getCategoriesViewData(
        selectedTypeIds: List<Long>,
        searchText: String,
        isSearching: Boolean,
    ): CategoriesViewData = withContext(Dispatchers.Default) {
        val result: MutableList<ViewHolderType> = mutableListOf()

        val isDarkTheme = prefsInteractor.getDarkMode()
        val categories = categoryInteractor.getAll()
        val filteredCategories = filterCategories(
            selectedTypeIds = selectedTypeIds,
            categories = categories,
        ).let {
            searchCategories(
                categories = it,
                isSearching = isSearching,
                searchText = searchText,
            )
        }

        result += if (filteredCategories.isEmpty() && isSearching) {
            mapSearchEmpty()
        } else {
            categoryViewDataMapper.mapToCategoryHint()
        }

        result += filteredCategories.map { category ->
            categoryViewDataMapper.mapCategory(
                category = category,
                isDarkTheme = isDarkTheme,
            )
        }

        result += categoryViewDataMapper.mapToTypeTagAddItem(isDarkTheme)

        return@withContext CategoriesViewData(
            items = result,
            showHint = categories.isNotEmpty(),
        )
    }

    private suspend fun getRecordTagViewData(
        selectedTypeIds: List<Long>,
        searchText: String,
        isSearching: Boolean,
    ): CategoriesViewData = withContext(Dispatchers.Default) {
        val result: MutableList<ViewHolderType> = mutableListOf()

        val isDarkTheme = prefsInteractor.getDarkMode()
        val tags = recordTagInteractor.getAll().filterNot { it.archived }
        val types = recordTypeInteractor.getAll()
        val typesMap = types.associateBy(RecordType::id)
        val filteredTags = filterTags(
            selectedTypeIds = selectedTypeIds,
            tags = tags,
            types = types,
        ).let {
            searchTags(
                tags = it,
                isSearching = isSearching,
                searchText = searchText,
            )
        }

        result += if (filteredTags.isEmpty() && isSearching) {
            mapSearchEmpty()
        } else {
            categoryViewDataMapper.mapToRecordTagHint()
        }

        result += filteredTags.map { tag ->
            categoryViewDataMapper.mapRecordTag(
                tag = tag,
                types = typesMap,
                isDarkTheme = isDarkTheme,
            )
        }

        result += categoryViewDataMapper.mapToRecordTagAddItem(isDarkTheme)

        return@withContext CategoriesViewData(
            items = result,
            showHint = tags.isNotEmpty(),
        )
    }

    private fun getBottomEmptySpace(
        navBarHeightDp: Int,
    ): ViewHolderType {
        val optionsButtonHeight = resourceRepo.getDimenInDp(R.dimen.button_height)
        val size = optionsButtonHeight + navBarHeightDp
        return EmptySpaceViewData(
            id = "categories_bottom_space".hashCode().toLong(),
            height = EmptySpaceViewData.ViewDimension.ExactSizeDp(size),
            wrapBefore = true,
        )
    }

    private fun mapSearchEmpty(): ViewHolderType {
        return HintViewData(text = resourceRepo.getString(R.string.widget_load_error))
    }

    private suspend fun filterCategories(
        selectedTypeIds: List<Long>,
        categories: List<Category>,
    ): List<Category> {
        if (selectedTypeIds.isEmpty()) return categories

        val types = recordTypeInteractor.getAll()
        val archivedTypeIds = types.filter { it.hidden }.map { it.id }
        val recordTypeCategories = recordTypeCategoryInteractor.getAll()
        val categoriesToTypeIds = recordTypeCategories
            .groupBy { it.categoryId }
            .mapValues { (_, value) -> value.map { it.recordTypeId } }

        return categories.filter { category ->
            val assignedTypes = categoriesToTypeIds[category.id].orEmpty()
            // Archived is hidden.
            assignedTypes.any { it !in archivedTypeIds && it in selectedTypeIds }
        }
    }

    private suspend fun filterTags(
        selectedTypeIds: List<Long>,
        tags: List<RecordTag>,
        types: List<RecordType>,
    ): List<RecordTag> {
        if (selectedTypeIds.isEmpty()) return tags

        val archivedTypeIds = types.filter { it.hidden }.map { it.id }
        val tagsToTypes = recordTypeToTagInteractor.getAll()
            .groupBy { it.tagId }
            .mapValues { (_, value) -> value.map { it.recordTypeId } }
        val tagsToDefaultTypes = recordTypeToDefaultTagInteractor.getAll()
            .groupBy { it.tagId }
            .mapValues { (_, value) -> value.map { it.recordTypeId } }

        return tags.filter { tag ->
            val hasIconColorSource = tag.iconColorSource in selectedTypeIds
            val hasAssignedType = tagsToTypes[tag.id].orEmpty()
                .any { it in selectedTypeIds }
            // Archived is hidden.
            val hasAssignedDefaultType = tagsToDefaultTypes[tag.id].orEmpty()
                .any { it !in archivedTypeIds && it in selectedTypeIds }

            hasIconColorSource || hasAssignedType || hasAssignedDefaultType
        }
    }

    private fun searchCategories(
        categories: List<Category>,
        isSearching: Boolean,
        searchText: String,
    ): List<Category> {
        return if (isSearching) {
            categories.filter { it.name.lowercase().contains(searchText) }
        } else {
            categories
        }
    }

    private fun searchTags(
        tags: List<RecordTag>,
        isSearching: Boolean,
        searchText: String,
    ): List<RecordTag> {
        return if (isSearching) {
            tags.filter { it.name.lowercase().contains(searchText) }
        } else {
            tags
        }
    }
}
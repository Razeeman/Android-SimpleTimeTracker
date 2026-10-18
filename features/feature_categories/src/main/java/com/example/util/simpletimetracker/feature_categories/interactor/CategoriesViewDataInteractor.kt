package com.example.util.simpletimetracker.feature_categories.interactor

import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.category.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.category.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.category.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToDefaultTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTypeToDefaultTag
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTypeToTag
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.EmptySpaceViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeRelation.ActivitySuggestionListViewData
import com.example.util.simpletimetracker.feature_categories.R
import com.example.util.simpletimetracker.feature_categories.adapter.CategoriesRelationSpecialViewData
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
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
) {

    suspend fun hasData(): Boolean {
        val categories = categoryInteractor.getAll()
        val tags = recordTagInteractor.getAll().filterNot { it.archived }

        return categories.isNotEmpty() || tags.isNotEmpty()
    }

    suspend fun getViewData(
        selectedTypeIds: List<Long>,
        searchEnabled: Boolean,
        searchText: String,
        navBarHeightDp: Int,
        showRelations: Boolean,
        tagIdToRecordCount: Map<Long, Int>,
    ): CategoriesViewData = coroutineScope {
        val isSearching: Boolean = searchEnabled && searchText.isNotEmpty()

        if (showRelations) {
            return@coroutineScope getRelationsViewData(
                selectedTypeIds = selectedTypeIds,
                searchText = searchText,
                isSearching = isSearching,
                navBarHeightDp = navBarHeightDp,
                tagIdToRecordCount = tagIdToRecordCount,
            )
        }

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

        val items = typeTags.await() +
            DividerViewData(1) +
            recordTags.await() +
            getBottomEmptySpace(navBarHeightDp)

        return@coroutineScope CategoriesViewData(
            items = items,
            centerContent = true,
        )
    }

    private suspend fun getRelationsViewData(
        selectedTypeIds: List<Long>,
        searchText: String,
        isSearching: Boolean,
        navBarHeightDp: Int,
        tagIdToRecordCount: Map<Long, Int>,
    ): CategoriesViewData = withContext(Dispatchers.Default) {
        val data = getRelationsData(selectedTypeIds)
        val searchQuery = searchText.lowercase()

        val categoriesData = getCategoryRelationsSection(
            searchQuery = searchQuery,
            isSearching = isSearching,
            data = data,
        )
        val tagsData = getTagRelationsSection(
            searchQuery = searchQuery,
            isSearching = isSearching,
            data = data,
            tagIdToRecordCount = tagIdToRecordCount,
        )

        val items = categoriesData +
            DividerViewData(1) +
            tagsData +
            getBottomEmptySpace(navBarHeightDp)

        return@withContext CategoriesViewData(
            items = items,
            centerContent = false,
        )
    }

    private suspend fun getCategoriesViewData(
        selectedTypeIds: List<Long>,
        searchText: String,
        isSearching: Boolean,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val result: MutableList<ViewHolderType> = mutableListOf()

        val isDarkTheme = prefsInteractor.getDarkMode()
        val categories = categoryInteractor.getAll()
        val filteredCategories = filterCategories(
            selectedTypeIds = selectedTypeIds,
            categories = categories,
            recordTypeCategories = { recordTypeCategoryInteractor.getAll() },
        ).let { data ->
            if (isSearching) {
                data.filter { it.name.lowercase().contains(searchText) }
            } else {
                data
            }
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

        result += categoryViewDataMapper.mapToTypeTagAddItem(
            useShortName = false,
            isDarkTheme = isDarkTheme,
        )

        return@withContext result
    }

    private suspend fun getRecordTagViewData(
        selectedTypeIds: List<Long>,
        searchText: String,
        isSearching: Boolean,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        val result: MutableList<ViewHolderType> = mutableListOf()

        val isDarkTheme = prefsInteractor.getDarkMode()
        val tags = recordTagInteractor.getAll().filterNot { it.archived }
        val types = recordTypeInteractor.getAll()
        val typesMap = types.associateBy(RecordType::id)
        val filteredTags = filterTags(
            selectedTypeIds = selectedTypeIds,
            tags = tags,
            types = { types },
            tagsToTypes = { recordTypeToTagInteractor.getAll() },
            tagsToDefaultTypes = { recordTypeToDefaultTagInteractor.getAll() },
        ).let { data ->
            if (isSearching) {
                data.filter { it.name.lowercase().contains(searchText) }
            } else {
                data
            }
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

        result += categoryViewDataMapper.mapToRecordTagAddItem(
            useShortName = false,
            isDarkTheme = isDarkTheme,
        )

        return@withContext result
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

    private fun mapSearchEmpty(): HintViewData {
        return HintViewData(text = resourceRepo.getString(R.string.widget_load_error))
    }

    private suspend fun filterCategories(
        selectedTypeIds: List<Long>,
        categories: List<Category>,
        recordTypeCategories: suspend () -> List<RecordTypeCategory>,
    ): List<Category> {
        if (selectedTypeIds.isEmpty()) return categories

        val types = recordTypeInteractor.getAll()
        val archivedTypeIds = types.filter { it.hidden }.map { it.id }
        val categoriesToTypeIds = recordTypeCategories()
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
        types: suspend () -> List<RecordType>,
        tagsToTypes: suspend () -> List<RecordTypeToTag>,
        tagsToDefaultTypes: suspend () -> List<RecordTypeToDefaultTag>,
    ): List<RecordTag> {
        if (selectedTypeIds.isEmpty()) return tags

        val archivedTypeIds = types().filter { it.hidden }.map { it.id }
        val tagsToTypesMap = tagsToTypes()
            .groupBy { it.tagId }
            .mapValues { (_, value) -> value.map { it.recordTypeId } }
        val tagsToDefaultTypesMap = tagsToDefaultTypes()
            .groupBy { it.tagId }
            .mapValues { (_, value) -> value.map { it.recordTypeId } }

        return tags.filter { tag ->
            val hasIconColorSource = tag.iconColorSource in selectedTypeIds
            val hasAssignedType = tagsToTypesMap[tag.id].orEmpty()
                .any { it in selectedTypeIds }
            // Archived is hidden.
            val hasAssignedDefaultType = tagsToDefaultTypesMap[tag.id].orEmpty()
                .any { it !in archivedTypeIds && it in selectedTypeIds }

            hasIconColorSource || hasAssignedType || hasAssignedDefaultType
        }
    }

    private suspend fun getRelationsData(
        selectedTypeIds: List<Long>,
    ): RelationsData = coroutineScope {
        val types = recordTypeInteractor.getAll().filterNot(RecordType::hidden)

        val categories = async { categoryInteractor.getAll() }
        val tags = async { recordTagInteractor.getAll().filterNot(RecordTag::archived) }
        val categoryRelations = async { recordTypeCategoryInteractor.getAll() }
        val tagRelations = async { recordTypeToTagInteractor.getAll() }
        val defaultTagRelations = async { recordTypeToDefaultTagInteractor.getAll() }
        val isDarkTheme = async { prefsInteractor.getDarkMode() }

        val visibleTypesMap = types.associateBy(RecordType::id)
        val visibleTypeIds = visibleTypesMap.keys

        return@coroutineScope RelationsData(
            isDarkTheme = isDarkTheme.await(),
            categories = filterCategories(
                selectedTypeIds = selectedTypeIds,
                categories = categories.await(),
                recordTypeCategories = { categoryRelations.await() },
            ),
            tags = filterTags(
                selectedTypeIds = selectedTypeIds,
                tags = tags.await(),
                types = { types },
                tagsToTypes = { tagRelations.await() },
                tagsToDefaultTypes = { defaultTagRelations.await() },
            ),
            typesMap = visibleTypesMap,
            categoriesToTypeIds = categoryRelations.await()
                .groupBy { it.categoryId }
                .mapValues { (_, value) ->
                    value.map { it.recordTypeId }.filter { it in visibleTypeIds }
                },
            tagsToTypeIds = tagRelations.await()
                .groupBy { it.tagId }
                .mapValues { (_, value) ->
                    value.map { it.recordTypeId }.filter { it in visibleTypeIds }
                },
            tagsToDefaultTypeIds = defaultTagRelations.await()
                .groupBy { it.tagId }
                .mapValues { (_, value) ->
                    value.map { it.recordTypeId }.filter { it in visibleTypeIds }
                },
        )
    }

    private fun getCategoryRelationsSection(
        searchQuery: String,
        isSearching: Boolean,
        data: RelationsData,
    ): List<ViewHolderType> {
        data class CategoryRelations(
            val category: Category,
            val relationTypeIds: List<Long>,
        )

        val items: MutableList<ViewHolderType> = mutableListOf()

        val categories = data.categories.mapNotNull { category ->
            val relationTypeIds = data.categoriesToTypeIds[category.id].orEmpty()
            val matchesSearch = matchesSearch(
                parentName = category.name,
                relationNames = relationTypeIds.mapNotNull { data.typesMap[it]?.name },
                isSearching = isSearching,
                searchQuery = searchQuery,
            )
            if (matchesSearch) {
                CategoryRelations(category = category, relationTypeIds = relationTypeIds)
            } else {
                null
            }
        }

        items += if (categories.isEmpty() && isSearching) {
            mapSearchEmpty()
        } else {
            categoryViewDataMapper.mapToCategoryHint()
        }.copy(gravity = HintViewData.Gravity.START)

        categories.forEach { categoryRelations ->
            val categoryId = categoryRelations.category.id

            items += categoryViewDataMapper.mapCategory(
                category = categoryRelations.category,
                isDarkTheme = data.isDarkTheme,
            )

            items += EmptySpaceViewData(
                id = ("category_divider$categoryId").hashCode().toLong(),
                wrapBefore = true,
            )

            val relationCards = categoryRelations.relationTypeIds.mapNotNull { relationTypeId ->
                mapRelationType(
                    suggestionTypeId = relationTypeId,
                    forTypeId = categoryId,
                    typesMap = data.typesMap,
                    isDarkTheme = data.isDarkTheme,
                )
            }
            if (relationCards.isEmpty()) {
                items += mapSpecialRelationCard(
                    forId = categoryId,
                    type = CategoriesRelationSpecialViewData.Type.CategoriesEmpty,
                    isDarkTheme = data.isDarkTheme,
                    text = resourceRepo.getString(R.string.record_types_empty),
                    colorAttrResId = R.attr.colorAccent,
                )
            } else {
                items += relationCards
            }

            items += EmptySpaceViewData(
                id = ("category_relations_divider$categoryId").hashCode().toLong(),
                width = EmptySpaceViewData.ViewDimension.MatchParent,
                height = EmptySpaceViewData.ViewDimension.ExactSizeDp(16),
            )
        }

        items += categoryViewDataMapper.mapToTypeTagAddItem(
            useShortName = false,
            isDarkTheme = data.isDarkTheme,
        )
        return items
    }

    private fun getTagRelationsSection(
        searchQuery: String,
        isSearching: Boolean,
        data: RelationsData,
        tagIdToRecordCount: Map<Long, Int>,
    ): List<ViewHolderType> {
        data class TagRelations(
            val tag: RecordTag,
            val assignableTypeIds: List<Long>,
            val defaultTypeIds: List<Long>,
        )

        val items: MutableList<ViewHolderType> = mutableListOf()

        val tags = data.tags.mapNotNull { tag ->
            val tagId = tag.id

            val assignableTypeIds = data.tagsToTypeIds[tagId].orEmpty()
            val defaultTypeIds = data.tagsToDefaultTypeIds[tagId].orEmpty()
            val relationNames = (assignableTypeIds + defaultTypeIds)
                .distinct()
                .mapNotNull { data.typesMap[it]?.name }
            val matchesSearch = matchesSearch(
                parentName = tag.name,
                relationNames = relationNames,
                isSearching = isSearching,
                searchQuery = searchQuery,
            )
            if (matchesSearch) {
                TagRelations(
                    tag = tag,
                    assignableTypeIds = assignableTypeIds,
                    defaultTypeIds = defaultTypeIds,
                )
            } else {
                null
            }
        }

        items += if (tags.isEmpty() && isSearching) {
            mapSearchEmpty()
        } else {
            categoryViewDataMapper.mapToRecordTagHint()
        }.copy(gravity = HintViewData.Gravity.START)

        tags.forEach { tagRelations ->
            val tagId = tagRelations.tag.id

            items += categoryViewDataMapper.mapRecordTag(
                tag = tagRelations.tag,
                types = data.typesMap,
                isDarkTheme = data.isDarkTheme,
            )

            items += EmptySpaceViewData(
                id = ("tag_divider$tagId").hashCode().toLong(),
                wrapBefore = true,
            )

            if (tagRelations.assignableTypeIds.isEmpty()) {
                items += mapSpecialRelationCard(
                    forId = tagId,
                    type = CategoriesRelationSpecialViewData.Type.TagCommon,
                    isDarkTheme = data.isDarkTheme,
                    text = resourceRepo.getString(R.string.change_record_tag_type_general),
                    colorAttrResId = R.attr.appInactiveColor,
                )
            } else {
                items += tagRelations.assignableTypeIds.mapNotNull { relationTypeId ->
                    mapRelationType(
                        suggestionTypeId = relationTypeId,
                        forTypeId = tagId,
                        typesMap = data.typesMap,
                        isDarkTheme = data.isDarkTheme,
                    )
                }
            }

            if (tagRelations.defaultTypeIds.isNotEmpty()) {
                items += EmptySpaceViewData(
                    id = ("tag_default_divider$tagId").hashCode().toLong(),
                    wrapBefore = true,
                )
                items += mapSpecialRelationCard(
                    forId = tagId,
                    type = CategoriesRelationSpecialViewData.Type.TagDefaultHint,
                    isDarkTheme = data.isDarkTheme,
                    text = resourceRepo.getString(R.string.change_record_tag_default_hint),
                    colorAttrResId = R.attr.appInactiveColor,
                )
                items += tagRelations.defaultTypeIds.mapNotNull { relationTypeId ->
                    CategoriesRelationSpecialViewData(
                        id = CategoriesRelationSpecialViewData.Id(
                            forTypeId = tagId,
                            type = CategoriesRelationSpecialViewData.Type.TagDefault(relationTypeId),
                        ),
                        data = mapRelationType(
                            suggestionTypeId = relationTypeId,
                            forTypeId = tagId,
                            typesMap = data.typesMap,
                            isDarkTheme = data.isDarkTheme,
                        ) ?: return@mapNotNull null,
                    )
                }
            }

            val count = tagIdToRecordCount[tagId]
            items += EmptySpaceViewData(
                id = ("tag_records_count_divider$tagId").hashCode().toLong(),
                wrapBefore = true,
            )
            items += mapSpecialRelationCard(
                forId = tagId,
                type = CategoriesRelationSpecialViewData.Type.TagRecordsCount,
                isDarkTheme = data.isDarkTheme,
                text = resourceRepo.getString(
                    R.string.separator_template,
                    resourceRepo.getString(R.string.archive_tagged_records_count),
                    count ?: "",
                ),
                colorAttrResId = if (count == 0) R.attr.colorAccent else R.attr.appInactiveColor,
            )

            items += EmptySpaceViewData(
                id = ("tag_relations_divider$tagId").hashCode().toLong(),
                width = EmptySpaceViewData.ViewDimension.MatchParent,
                height = EmptySpaceViewData.ViewDimension.ExactSizeDp(16),
            )
        }

        items += categoryViewDataMapper.mapToRecordTagAddItem(
            useShortName = false,
            isDarkTheme = data.isDarkTheme,
        )
        return items
    }

    private fun mapRelationType(
        suggestionTypeId: Long,
        forTypeId: Long,
        typesMap: Map<Long, RecordType>,
        isDarkTheme: Boolean,
    ): ActivitySuggestionListViewData? {
        return recordTypeViewDataMapper.map(
            recordType = typesMap[suggestionTypeId] ?: return null,
            numberOfCards = 0, // Use default size.
            isDarkTheme = isDarkTheme,
        ).let {
            ActivitySuggestionListViewData(
                id = ActivitySuggestionListViewData.Id(
                    suggestionTypeId = suggestionTypeId,
                    forTypeId = forTypeId,
                ),
                text = it.name,
                icon = it.iconId,
                // TODO GRADIENT
                color = it.color.primaryColor,
            )
        }
    }

    private fun mapSpecialRelationCard(
        forId: Long,
        type: CategoriesRelationSpecialViewData.Type,
        isDarkTheme: Boolean,
        text: String,
        colorAttrResId: Int,
    ): ViewHolderType {
        return CategoriesRelationSpecialViewData(
            id = CategoriesRelationSpecialViewData.Id(
                forTypeId = forId,
                type = type,
            ),
            data = ActivitySuggestionListViewData(
                id = ActivitySuggestionListViewData.Id(
                    suggestionTypeId = forId,
                    forTypeId = forId,
                ),
                text = text,
                icon = null,
                color = resourceRepo.getThemedAttr(
                    attrId = colorAttrResId,
                    isDarkTheme = isDarkTheme,
                ),
            ),
        )
    }

    private fun matchesSearch(
        parentName: String,
        relationNames: List<String>,
        isSearching: Boolean,
        searchQuery: String,
    ): Boolean {
        if (!isSearching) return true
        return parentName.lowercase().contains(searchQuery) ||
            relationNames.any { it.lowercase().contains(searchQuery) }
    }

    private data class RelationsData(
        val isDarkTheme: Boolean,
        val categories: List<Category>,
        val tags: List<RecordTag>,
        val typesMap: Map<Long, RecordType>,
        val categoriesToTypeIds: Map<Long, List<Long>>,
        val tagsToTypeIds: Map<Long, List<Long>>,
        val tagsToDefaultTypeIds: Map<Long, List<Long>>,
    )
}
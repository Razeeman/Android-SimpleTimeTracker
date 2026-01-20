package com.example.util.simpletimetracker.feature_categories.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.fromHtml
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryAddViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.TagType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_categories.R
import com.example.util.simpletimetracker.feature_categories.interactor.CategoriesViewDataInteractor
import com.example.util.simpletimetracker.feature_categories.mapper.CategoriesOptionsListMapper
import com.example.util.simpletimetracker.feature_categories.model.CategoriesOptionsListItem
import com.example.util.simpletimetracker.feature_categories.utils.CustomTagHandler
import com.example.util.simpletimetracker.feature_categories.viewData.CategoriesSearchState
import com.example.util.simpletimetracker.feature_categories.viewData.CategoriesViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeCategoryFromTagsParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromTagsParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.example.util.simpletimetracker.navigation.params.screen.HelpDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val customTagHandler: CustomTagHandler,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoriesViewDataInteractor: CategoriesViewDataInteractor,
    private val categoriesOptionsListMapper: CategoriesOptionsListMapper,
) : BaseViewModel() {

    val categories: LiveData<CategoriesViewData> by lazySuspend {
        updateCategories()
        CategoriesViewData(listOf(LoaderViewData()))
    }
    val searchState: LiveData<CategoriesSearchState> by lazySuspend {
        loadSearchState()
    }
    val showHint: LiveData<Boolean> by lazySuspend {
        categoriesViewDataInteractor.getHintViewData()
    }

    private var navBarHeightDp: Int = 0
    private var selectedTypeIds: List<Long> = emptyList()
    private var searchText: String = ""
    private var searchJob: Job? = null
    private var loadJob: Job? = null

    fun onCategoryClick(item: CategoryViewData, sharedElements: Pair<Any, String>) {
        val params = when (item) {
            is CategoryViewData.Category -> ::ChangeCategoryFromTagsParams
            is CategoryViewData.Record -> ::ChangeRecordTagFromTagsParams
        }
        val icon = (item as? CategoryViewData.Record)?.icon?.toParams()

        router.navigate(
            data = params(
                ChangeTagData.Change(
                    transitionName = sharedElements.second,
                    id = item.id,
                    preview = ChangeTagData.Change.Preview(
                        name = item.name,
                        color = item.color,
                        icon = icon,
                    ),
                ),
            ),
            sharedElements = mapOf(sharedElements),
        )
    }

    fun onAddCategoryClick(viewData: CategoryAddViewData) {
        val params = when (viewData.type) {
            TagType.RECORD_TYPE -> ::ChangeCategoryFromTagsParams
            TagType.RECORD -> ::ChangeRecordTagFromTagsParams
        }

        router.navigate(
            data = params(ChangeTagData.New()),
        )
    }

    fun onVisible() {
        updateCategories()
    }

    fun onOptionsClick() = viewModelScope.launch {
        val items = categoriesOptionsListMapper.map(selectedTypeIds)
        router.navigate(OptionsListParams(items))
    }

    fun onOptionsLongClick() {
        onFilterClick()
    }

    fun onOptionsItemClick(id: OptionsListParams.Item.Id) = viewModelScope.launch {
        if (id !is CategoriesOptionsListItem) return@launch
        when (id) {
            is CategoriesOptionsListItem.Filter -> onFilterClick()
            is CategoriesOptionsListItem.EnabledSearch -> onSearchToggled()
            is CategoriesOptionsListItem.Help -> onHelpClick()
        }
    }

    fun onChangeInsets(navBarHeight: Int) {
        if (navBarHeightDp != navBarHeight) {
            navBarHeightDp = navBarHeight
            updateCategories()
        }
    }

    fun onFilterApplied(
        chartFilterType: ChartFilterType,
        dataIds: List<Long>,
    ) = viewModelScope.launch {
        // Filtering available only by activity.
        if (chartFilterType != ChartFilterType.ACTIVITY) return@launch
        val allTypeIds = recordTypeInteractor.getAll().map(RecordType::id)
        selectedTypeIds = allTypeIds.filter { it !in dataIds }
    }

    fun onDataSelected(dataIds: List<Long>, tag: String?) {
        if (tag != CATEGORIES_TYPE_SELECTION_TAG) return
        selectedTypeIds = dataIds
        updateCategories()
    }

    fun onFilterClosed() {
        updateCategories()
    }

    fun onSearchChange(search: String) {
        if (search != searchText) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                searchText = search
                // Do not delay on clear.
                if (search.isNotEmpty()) delay(500)
                updateSearchState()
                updateCategories()
            }
        }
    }

    private fun onFilterClick() = viewModelScope.launch {
        // TODO add to dialog param?
        val allTypeIds = recordTypeInteractor.getAll()
        val archivedTypeIds = allTypeIds.filter(RecordType::hidden).map(RecordType::id)
        TypesSelectionDialogParams(
            tag = CATEGORIES_TYPE_SELECTION_TAG,
            title = resourceRepo.getString(R.string.change_record_message_choose_type),
            subtitle = "",
            type = TypesSelectionDialogParams.Type.Activity,
            selectedTypeIds = selectedTypeIds,
            selectedTagValues = emptyList(),
            isMultiSelectAvailable = true,
            idsShouldBeVisible = archivedTypeIds,
            showHints = true,
            allowTagValueSelection = false,
        ).let(router::navigate)
    }

    private suspend fun onSearchToggled() {
        val current = prefsInteractor.getIsCategoriesSearchEnabled()
        prefsInteractor.setIsCategoriesSearchEnabled(!current)
        updateSearchState()
        updateCategories()
    }

    private suspend fun onHelpClick() {
        customTagHandler.isDarkTheme = prefsInteractor.getDarkMode()
        val text = resourceRepo.getString(R.string.categories_and_tags_hint)
            .fromHtml(customTagHandler)
        HelpDialogParams(
            title = resourceRepo.getString(R.string.categories_title),
            text = text,
        ).let(router::navigate)
    }

    private fun updateSearchState() = viewModelScope.launch {
        searchState.set(loadSearchState())
    }

    private suspend fun loadSearchState(): CategoriesSearchState {
        return CategoriesSearchState(
            isVisible = prefsInteractor.getIsCategoriesSearchEnabled(),
            text = searchText,
        )
    }

    private fun updateCategories() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val data = loadCategoriesViewData()
            delayLoad()
            categories.set(data)
        }
    }

    private suspend fun loadCategoriesViewData(): CategoriesViewData {
        return categoriesViewDataInteractor.getViewData(
            selectedTypeIds = selectedTypeIds,
            searchEnabled = prefsInteractor.getIsCategoriesSearchEnabled(),
            searchText = searchText,
            navBarHeightDp = navBarHeightDp,
        )
    }

    companion object {
        private const val CATEGORIES_TYPE_SELECTION_TAG = "CATEGORIES_TYPE_SELECTION_TAG"
    }
}

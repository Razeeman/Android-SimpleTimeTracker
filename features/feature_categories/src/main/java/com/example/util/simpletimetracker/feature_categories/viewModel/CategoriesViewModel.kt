package com.example.util.simpletimetracker.feature_categories.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryAddViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.TagType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_categories.interactor.CategoriesViewDataInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeCategoryFromTagsParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromTagsParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import kotlinx.coroutines.launch
import javax.inject.Inject

class CategoriesViewModel @Inject constructor(
    private val router: Router,
    private val categoriesViewDataInteractor: CategoriesViewDataInteractor,
) : ViewModel() {

    val categories: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadCategoriesViewData()
            }
            initial
        }
    }

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
                        icon = icon
                    )
                )
            ),
            sharedElements = mapOf(sharedElements)
        )
    }

    fun onAddCategoryClick(viewData: CategoryAddViewData) {
        val params = when (viewData.type) {
            TagType.RECORD_TYPE -> ::ChangeCategoryFromTagsParams
            TagType.RECORD -> ::ChangeRecordTagFromTagsParams
        }

        router.navigate(
            data = params(ChangeTagData.New())
        )
    }

    fun onVisible() {
        updateCategories()
    }

    private fun updateCategories() = viewModelScope.launch {
        val data = loadCategoriesViewData()
        (categories as MutableLiveData).value = data
    }

    private suspend fun loadCategoriesViewData(): List<ViewHolderType> {
        return categoriesViewDataInteractor.getViewData()
    }
}

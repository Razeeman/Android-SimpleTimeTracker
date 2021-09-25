package com.example.util.simpletimetracker.feature_categories.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.domain.model.TagType
import com.example.util.simpletimetracker.feature_categories.interactor.CategoriesViewDataInteractor
import com.example.util.simpletimetracker.feature_categories.viewData.CategoryAddViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.ChangeCategoryParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class CategoriesViewModel @Inject constructor(
    private val router: Router,
    private val categoriesViewDataInteractor: CategoriesViewDataInteractor
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

    fun onCategoryClick(item: CategoryViewData, sharedElements: Map<Any, String>) {
        val screen = when (item) {
            is CategoryViewData.Activity -> Screen.CHANGE_CATEGORY
            is CategoryViewData.Record -> Screen.CHANGE_RECORD_TAG
        }
        val icon = (item as? CategoryViewData.Record)?.icon?.toParams()
        val transitionName = when (item) {
            is CategoryViewData.Activity -> TransitionNames.ACTIVITY_TAG
            is CategoryViewData.Record -> TransitionNames.RECORD_TAG
        } + item.id

        router.navigate(
            screen = screen,
            data = ChangeCategoryParams.Change(
                transitionName = transitionName,
                id = item.id,
                preview = ChangeCategoryParams.Change.Preview(
                    name = item.name,
                    color = item.color,
                    icon = icon
                )
            ),
            sharedElements = sharedElements
        )
    }

    fun onAddCategoryClick(viewData: CategoryAddViewData) {
        val screen = when (viewData.type) {
            TagType.RECORD_TYPE -> Screen.CHANGE_CATEGORY
            TagType.RECORD -> Screen.CHANGE_RECORD_TAG
        }

        router.navigate(
            screen = screen,
            data = ChangeCategoryParams.New
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

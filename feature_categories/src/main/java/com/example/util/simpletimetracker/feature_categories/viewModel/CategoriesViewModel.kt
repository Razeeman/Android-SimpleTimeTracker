package com.example.util.simpletimetracker.feature_categories.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.feature_categories.R
import com.example.util.simpletimetracker.feature_categories.viewData.CategoryAddViewData
import com.example.util.simpletimetracker.feature_categories.viewData.CategoryViewData
import com.example.util.simpletimetracker.navigation.Router
import kotlinx.coroutines.launch
import javax.inject.Inject

class CategoriesViewModel @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val router: Router,
    private val categoryInteractor: CategoryInteractor
) : ViewModel() {

    val categories: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    fun onCategoryLongClick(item: CategoryViewData, sharedElements: Map<Any, String>) {
//        router.navigate(
//            screen = Screen.CHANGE_RECORD_TYPE,
//            data = ChangeRecordTypeParams(item.id),
//            sharedElements = sharedElements
//        )
    }

    fun onAddRecordTypeClick() {
//        router.navigate(
//            screen = Screen.CHANGE_RECORD_TYPE,
//            data = ChangeRecordTypeParams(0)
//        )
    }

    fun onVisible() {
        updateCategories()
    }

    private fun updateCategories() = viewModelScope.launch {
        val data = loadCategoriesViewData()
        (categories as MutableLiveData).value = data
    }

    private suspend fun loadCategoriesViewData(): List<ViewHolderType> {
        return categoryInteractor.getAll().map {
            CategoryViewData(
                id = it.id,
                name = it.name,
                textColor = resourceRepo.getColor(R.color.white),
                color = resourceRepo.getColor(R.color.black)
            )
        } + CategoryAddViewData(
            name = "Add new category",
            color = resourceRepo.getColor(R.color.colorInactive)
        )
    }
}

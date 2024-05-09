package com.example.util.simpletimetracker.feature_dialogs.cardOrder.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.CardTagOrder
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.navigation.params.screen.CardOrderDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardOrderViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
) : ViewModel() {

    lateinit var extra: CardOrderDialogParams

    val viewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadViewData()
            }
            initial
        }
    }

    fun onDismiss(newList: List<ViewHolderType>) = GlobalScope.launch {
        val dataIds: List<Long> = when (extra.type) {
            is CardOrderDialogParams.Type.RecordType -> {
                newList.filterIsInstance<RecordTypeViewData>().map { it.id }
            }
            is CardOrderDialogParams.Type.Category -> {
                newList.filterIsInstance<CategoryViewData.Category>().map { it.id }
            }
            is CardOrderDialogParams.Type.Tag -> {
                newList.filterIsInstance<CategoryViewData.Record>().map { it.id }
            }
        }
        val setter: suspend (Map<Long, Long>) -> Unit = when (extra.type) {
            is CardOrderDialogParams.Type.RecordType -> {
                {
                    prefsInteractor.setCardOrder(CardOrder.MANUAL)
                    prefsInteractor.setCardOrderManual(it)
                }
            }
            is CardOrderDialogParams.Type.Category -> {
                {
                    prefsInteractor.setCategoryOrder(CardOrder.MANUAL)
                    prefsInteractor.setCategoryOrderManual(it)
                }
            }
            is CardOrderDialogParams.Type.Tag -> {
                {
                    prefsInteractor.setTagOrder(CardTagOrder.MANUAL)
                    prefsInteractor.setTagOrderManual(it)
                }
            }
        }

        dataIds
            .takeIf { it.isNotEmpty() }
            ?.mapIndexed { index, id -> id to index.toLong() }
            ?.toMap()
            ?.let { setter(it) }
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        return when (val type = extra.type) {
            is CardOrderDialogParams.Type.RecordType -> loadTypesViewData(type.order)
            is CardOrderDialogParams.Type.Category -> loadCategoriesViewData(type.order)
            is CardOrderDialogParams.Type.Tag -> loadTagsViewData(type.order)
        }
    }

    private suspend fun loadTypesViewData(
        initialOrder: CardOrder,
    ): List<ViewHolderType> {
        val numberOfCards: Int = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        return recordTypeInteractor.getAll(initialOrder)
            .filter { !it.hidden }
            .takeUnless { it.isEmpty() }
            ?.map { type ->
                recordTypeViewDataMapper.map(
                    recordType = type,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme,
                    isChecked = null,
                )
            }
            ?: recordTypeViewDataMapper.mapToEmpty()
    }

    private suspend fun loadCategoriesViewData(
        initialOrder: CardOrder,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return categoryInteractor.getAll(initialOrder)
            .takeUnless { it.isEmpty() }
            ?.map { category ->
                categoryViewDataMapper.mapCategory(
                    category = category,
                    isDarkTheme = isDarkTheme,
                )
            }
            ?: listOf(categoryViewDataMapper.mapToCategoriesEmpty())
    }

    private suspend fun loadTagsViewData(
        initialOrder: CardTagOrder,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val types = recordTypeInteractor.getAll().associateBy { it.id }

        return recordTagInteractor.getAll(initialOrder)
            .filter { !it.archived }
            .takeUnless { it.isEmpty() }
            ?.map { tag ->
                categoryViewDataMapper.mapRecordTag(
                    tag = tag,
                    type = types[tag.iconColorSource],
                    isDarkTheme = isDarkTheme,
                )
            }
            ?: listOf(categoryViewDataMapper.mapToRecordTagsEmpty())
    }
}

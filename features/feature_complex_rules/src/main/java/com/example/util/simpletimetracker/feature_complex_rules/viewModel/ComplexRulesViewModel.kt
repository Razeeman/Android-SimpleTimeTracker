package com.example.util.simpletimetracker.feature_complex_rules.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.interactor.ComplexRuleInteractor
import com.example.util.simpletimetracker.domain.interactor.ComplexRulesDataUpdateInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_complex_rules.adapter.ComplexRuleViewData
import com.example.util.simpletimetracker.feature_complex_rules.interactor.ComplexRulesViewDataInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeComplexRuleParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComplexRulesViewModel @Inject constructor(
    private val router: Router,
    private val complexRuleInteractor: ComplexRuleInteractor,
    private val complexRulesViewDataInteractor: ComplexRulesViewDataInteractor,
    private val complexRulesDataUpdateInteractor: ComplexRulesDataUpdateInteractor,
) : BaseViewModel() {

    val viewData: LiveData<List<ViewHolderType>> by lazySuspend {
        listOf(LoaderViewData()).also { updateViewData() }
    }

    init {
        subscribeToUpdates()
    }

    fun onRuleClick(item: ComplexRuleViewData) {
        router.navigate(
            data = ChangeComplexRuleParams.Change(
                id = item.id,
            ),
        )
    }

    fun onRuleDisableClick(item: ComplexRuleViewData) {
        viewModelScope.launch {
            val rule = complexRuleInteractor.get(item.id) ?: return@launch
            if (rule.disabled) {
                complexRuleInteractor.enable(item.id)
            } else {
                complexRuleInteractor.disable(item.id)
            }
            updateViewData()
        }
    }

    fun onAddRuleClick() {
        router.navigate(data = ChangeComplexRuleParams.New)
    }

    private fun subscribeToUpdates() {
        viewModelScope.launch {
            complexRulesDataUpdateInteractor.dataUpdated.collect {
                updateViewData()
            }
        }
    }

    private fun updateViewData() = viewModelScope.launch {
        val data = loadViewData()
        viewData.set(data)
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        return complexRulesViewDataInteractor.getViewData()
    }
}

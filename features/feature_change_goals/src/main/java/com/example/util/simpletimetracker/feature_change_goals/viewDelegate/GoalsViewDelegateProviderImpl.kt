package com.example.util.simpletimetracker.feature_change_goals.viewDelegate

import com.example.util.simpletimetracker.feature_change_goals.api.GoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_change_goals.api.databinding.ChangeGoalsLayoutBinding
import com.example.util.simpletimetracker.feature_change_goals.api.viewDelegate.GoalsViewDelegate
import com.example.util.simpletimetracker.feature_change_goals.api.viewDelegate.GoalsViewDelegateProvider
import javax.inject.Inject

class GoalsViewDelegateProviderImpl @Inject constructor() : GoalsViewDelegateProvider {

    override fun provide(
        viewModel: GoalsViewModelDelegate,
        binding: ChangeGoalsLayoutBinding,
    ): GoalsViewDelegate {
        return GoalsViewDelegateImpl(viewModel, binding)
    }
}

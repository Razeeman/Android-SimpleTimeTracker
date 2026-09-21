package com.example.util.simpletimetracker.feature_change_goals.api.viewDelegate

import com.example.util.simpletimetracker.feature_change_goals.api.GoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_change_goals.api.databinding.ChangeGoalsLayoutBinding

interface GoalsViewDelegateProvider {

    fun provide(
        viewModel: GoalsViewModelDelegate,
        binding: ChangeGoalsLayoutBinding,
    ): GoalsViewDelegate
}

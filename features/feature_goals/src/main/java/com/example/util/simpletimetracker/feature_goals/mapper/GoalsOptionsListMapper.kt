package com.example.util.simpletimetracker.feature_goals.mapper

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_goals.R
import com.example.util.simpletimetracker.feature_goals.model.GoalsOptionsListItem
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import javax.inject.Inject

class GoalsOptionsListMapper @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val resourceRepo: ResourceRepo,
) {

    suspend fun map(): List<OptionsListParams.Item> {
        return listOf(
            OptionsListParams.Item(
                id = GoalsOptionsListItem.HideFinished,
                text = resourceRepo.getString(R.string.hide_finished_goals),
                icon = R.drawable.hide,
                isIconCheckVisible = prefsInteractor.getHideFinishedGoals(),
            ),
        )
    }
}

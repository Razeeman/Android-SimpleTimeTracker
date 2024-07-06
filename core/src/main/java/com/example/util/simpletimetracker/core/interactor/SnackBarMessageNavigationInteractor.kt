package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveParams
import javax.inject.Inject

class SnackBarMessageNavigationInteractor @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
) {

    fun showMessage(stringResId: Int) {
        val params = SnackBarParams(
            message = resourceRepo.getString(stringResId),
            duration = SnackBarParams.Duration.Short,
            marginBottomDp = resourceRepo.getDimenInDp(R.dimen.button_height),
        )
        router.show(params)
    }

    fun showArchiveMessage(stringResId: Int) {
        val message = resourceRepo.getString(stringResId) +
            "\n" +
            resourceRepo.getString(R.string.something_was_archived_hint)

        val params = SnackBarParams(
            message = message,
            duration = SnackBarParams.Duration.Long,
            marginBottomDp = resourceRepo.getDimenInDp(R.dimen.button_height),
            actionText = resourceRepo.getString(R.string.settings_archive),
            actionListener = {
                router.navigate(ArchiveParams)
            },
        )
        router.show(params)
    }
}
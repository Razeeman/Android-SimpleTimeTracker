package com.example.util.simpletimetracker.core.interactor

import android.view.View
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.resolver.SharingRepo
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureForSharing
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.action.ShareImageParams
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import javax.inject.Inject

class SharingInteractor @Inject constructor(
    private val sharingRepo: SharingRepo,
    private val router: Router,
    private val resourceRepo: ResourceRepo,
) {

    @Suppress("MoveVariableDeclarationIntoWhen")
    suspend fun execute(
        view: Any,
        filename: String,
    ) {
        if (view !is View) return

        val bitmap = view.measureForSharing().getBitmapFromView()
        val result = sharingRepo.saveBitmap(bitmap, filename)

        when (result) {
            is SharingRepo.Result.Success -> {
                ShareImageParams(
                    uriString = result.uriString,
                    notHandledCallback = { R.string.message_app_not_found.let(::showMessage) },
                ).let(router::execute)
            }
            is SharingRepo.Result.Error -> {
                showMessage(R.string.message_export_error)
            }
        }
    }

    private fun showMessage(stringResId: Int) {
        val params = SnackBarParams(
            message = resourceRepo.getString(stringResId),
            margins = SnackBarParams.Margins(
                bottom = resourceRepo.getDimenInDp(R.dimen.button_height),
            ),
        )
        router.show(params)
    }
}
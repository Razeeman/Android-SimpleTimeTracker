package com.example.util.simpletimetracker.feature_dialogs.helpDialog

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.HelpDialogParams
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_dialogs.databinding.HelpDialogFragmentBinding as Binding

@AndroidEntryPoint
class HelpDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val params: HelpDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = HelpDialogParams.Empty,
    )

    override fun initDialog() {
        setSkipCollapsed()
        if (params.isFullscreen) setFullScreen()
    }

    override fun initUi() = with(binding) {
        tvHelpDialogTitle.text = params.title
        tvHelpDialogDescription.text = params.text
        tvHelpDialogDescription.movementMethod = LinkMovementMethod.getInstance()
    }

    companion object {
        fun createBundle(data: HelpDialogParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}
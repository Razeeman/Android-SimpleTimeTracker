package com.example.util.simpletimetracker.feature_dialogs.recordTagSelection

import com.example.util.simpletimetracker.feature_dialogs.databinding.RecordTagSelectionDialogFragmentBinding as Binding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.commit
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.dialog.OnTagSelectedListener
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.navigation.ScreenFactory
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecordTagSelectionDialogFragment :
    BaseBottomSheetFragment<Binding>(),
    OnTagSelectedListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var screenFactory: ScreenFactory

    private val params: RecordTagSelectionParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = RecordTagSelectionParams.Empty,
    )

    override fun onTagSelected() {
        dismiss()
    }

    override fun initDialog() {
        setSkipCollapsed()
    }

    override fun initUi() {
        screenFactory.getFragment(params)?.let {
            childFragmentManager.commit {
                replace(R.id.containerRecordTagSelection, it)
            }
        }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: RecordTagSelectionParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}
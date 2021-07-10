package com.example.util.simpletimetracker.feature_dialogs.recordTagSelection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.commit
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.dialog.OnTagSelectedListener
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.ScreenFactory
import com.example.util.simpletimetracker.navigation.params.RecordTagSelectionParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_dialogs.databinding.RecordTagSelectionDialogFragmentBinding as Binding

@AndroidEntryPoint
class RecordTagSelectionDialogFragment :
    BaseBottomSheetFragment<Binding>(),
    OnTagSelectedListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var screenFactory: ScreenFactory

    private val params: RecordTagSelectionParams by lazy {
        arguments?.getParcelable(ARGS_PARAMS) ?: RecordTagSelectionParams()
    }

    override fun onTagSelected() {
        dismiss()
    }

    override fun initDialog() {
        setSkipCollapsed()
    }

    override fun initUi() {
        screenFactory.getFragment(
            screen = Screen.RECORD_TAG_SELECTION,
            data = params
        )?.let {
            childFragmentManager.commit {
                replace(R.id.containerRecordTagSelection, it)
            }
        }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is RecordTagSelectionParams -> putParcelable(ARGS_PARAMS, data)
            }
        }
    }
}
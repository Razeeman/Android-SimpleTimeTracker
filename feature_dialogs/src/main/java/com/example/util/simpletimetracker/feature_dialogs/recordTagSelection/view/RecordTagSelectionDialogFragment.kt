package com.example.util.simpletimetracker.feature_dialogs.recordTagSelection.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.commit
import com.example.util.simpletimetracker.core.dialog.OnTagSelectedListener
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.recordTagSelection.di.RecordTagSelectionDialogComponentProvider
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.ScreenFactory
import com.example.util.simpletimetracker.navigation.params.RecordTagSelectionParams
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class RecordTagSelectionDialogFragment : BottomSheetDialogFragment(),
    OnTagSelectedListener {

    @Inject
    lateinit var screenFactory: ScreenFactory

    private val params: RecordTagSelectionParams by lazy {
        arguments?.getParcelable(ARGS_PARAMS) ?: RecordTagSelectionParams()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.record_tag_selection_dialog_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDialog()
        initDi()
        initUi()
    }

    override fun onTagSelected() {
        dismiss()
    }

    private fun initDialog() {
        setSkipCollapsed()
    }

    private fun initDi() {
        (activity?.application as RecordTagSelectionDialogComponentProvider)
            .recordTagSelectionDialogComponent
            ?.inject(this)
    }

    private fun initUi() {
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
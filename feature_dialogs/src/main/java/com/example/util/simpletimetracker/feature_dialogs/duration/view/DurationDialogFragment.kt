package com.example.util.simpletimetracker.feature_dialogs.duration.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.duration.extra.DurationPickerExtra
import com.example.util.simpletimetracker.feature_dialogs.duration.viewModel.DurationPickerViewModel
import com.example.util.simpletimetracker.navigation.params.DurationDialogParams
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.duration_dialog_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class DurationDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<DurationPickerViewModel>

    private val viewModel: DurationPickerViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private var dialogListener: DurationDialogListener? = null
    private val dialogTag: String? by lazy { arguments?.getString(ARGS_TAG) }

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
            R.layout.duration_dialog_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDialog()
        initUi()
        initUx()
        initViewModel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is DurationDialogListener -> {
                dialogListener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is DurationDialogListener && it.isResumed }
                    ?.let { dialogListener = it as? DurationDialogListener }
            }
        }
    }

    private fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
    }

    private fun initUi() {
        // Do nothing
    }

    private fun initUx() {
        btnDurationPickerSave.setOnClick(::onSaveClick)
        btnDurationPickerDisable.setOnClick(::onDisableClick)
        viewDurationPickerNumberKeyboard.listener = viewModel::onNumberPressed
        ivDurationPickerDelete.setOnClick(viewModel::onNumberDelete)
    }

    private fun initViewModel(): Unit = with(viewModel) {
        extra = DurationPickerExtra(arguments?.getLong(ARGS_DURATION).orZero())
        durationViewData.observe(viewLifecycleOwner, viewDurationPickerValue::setData)
    }

    private fun onSaveClick() {
        viewModel.durationViewData.observeOnce(viewLifecycleOwner) { data ->
            val duration = data.seconds + data.minutes * 60L + data.hours * 3600L
            dialogListener?.onDurationSet(duration, dialogTag)
            dismiss()
        }
    }

    private fun onDisableClick() {
        dialogListener?.onDisable(dialogTag)
        dismiss()
    }

    companion object {
        private const val ARGS_TAG = "tag"
        private const val ARGS_DURATION = "duration"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is DurationDialogParams -> {
                    putString(ARGS_TAG, data.tag)
                    putLong(ARGS_DURATION, data.duration)
                }
            }
        }
    }
}
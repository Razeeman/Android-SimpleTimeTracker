package com.example.util.simpletimetracker.feature_archive.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_archive.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.archive_dialog_fragment.*

class ArchiveDialogFragment : BottomSheetDialogFragment() {

    private val params: ArchiveDialogParams? by lazy {
        arguments?.getParcelable<ArchiveDialogParams>(ARGS_PARAMS)
    }
    private var archiveDialogListener: ArchiveDialogListener? = null

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
            R.layout.archive_dialog_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDialog()
        initUx()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is ArchiveDialogListener -> {
                archiveDialogListener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is ArchiveDialogListener && it.isResumed }
                    ?.let { archiveDialogListener = it as? ArchiveDialogListener }
            }
        }
    }

    private fun initDialog() {
        setSkipCollapsed()
    }

    private fun initUx() {
        btnArchiveDialogDelete.setOnClick {
            archiveDialogListener?.onDeleteClick(params)
            dismiss()
        }
        btnArchiveDialogRestore.setOnClick {
            archiveDialogListener?.onRestoreClick(params)
            dismiss()
        }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is ArchiveDialogParams -> putParcelable(ARGS_PARAMS, data)
            }
        }
    }
}
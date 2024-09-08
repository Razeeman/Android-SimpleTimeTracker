package com.example.util.simpletimetracker.feature_dialogs.standard

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.extension.findListeners
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class StandardDialogFragment :
    AppCompatDialogFragment(),
    DialogInterface.OnClickListener {

    private var listeners: List<StandardDialogListener> = emptyList()
    private val params: StandardDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS,
        default = StandardDialogParams(),
    )

    private val dialogTag: String? by lazy { params.tag }
    private val data: Any? by lazy { params.data }

    private val title: String by lazy { params.title }
    private val message: String by lazy { params.message }
    private val btnPositive: String by lazy { params.btnPositive }
    private val btnNegative: String by lazy { params.btnNegative }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return context?.let {
            MaterialAlertDialogBuilder(it, R.style.AlertDialogRounded).apply {
                if (title.isNotEmpty()) setTitle(title)
                if (message.isNotEmpty()) setMessage(message)
                if (btnPositive.isNotEmpty()) {
                    setPositiveButton(btnPositive, this@StandardDialogFragment)
                }
                if (btnNegative.isNotEmpty()) {
                    setNegativeButton(btnNegative, this@StandardDialogFragment)
                }
            }.create()
        } ?: throw IllegalStateException("Dialog context cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listeners += context.findListeners<StandardDialogListener>()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        listeners.forEach {
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> it.onPositiveClick(dialogTag, data)
                DialogInterface.BUTTON_NEGATIVE -> it.onNegativeClick(dialogTag, data)
            }
        }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: StandardDialogParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}
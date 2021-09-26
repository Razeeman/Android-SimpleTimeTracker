package com.example.util.simpletimetracker.feature_dialogs.standard

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams

class StandardDialogFragment :
    AppCompatDialogFragment(),
    DialogInterface.OnClickListener {

    private var standardDialogListener: StandardDialogListener? = null
    private val params: StandardDialogParams? by lazy { arguments?.getParcelable<StandardDialogParams>(ARGS_PARAMS) }

    private val dialogTag: String? by lazy { params?.tag }
    private val data: Any? by lazy { params?.data }

    private val title: String by lazy { params?.title.orEmpty() }
    private val message: String by lazy { params?.message.orEmpty() }
    private val btnPositive: String by lazy { params?.btnPositive.orEmpty() }
    private val btnNegative: String by lazy { params?.btnNegative.orEmpty() }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return context?.let {
            AlertDialog.Builder(it).apply {
                if (title.isNotEmpty()) setTitle(title)
                if (message.isNotEmpty()) setMessage(message)
                if (btnPositive.isNotEmpty()) setPositiveButton(
                    btnPositive, this@StandardDialogFragment
                )
                if (btnNegative.isNotEmpty()) setNegativeButton(
                    btnNegative, this@StandardDialogFragment
                )
            }.create()
        } ?: throw IllegalStateException("Dialog context cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is StandardDialogListener -> {
                standardDialogListener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is StandardDialogListener }
                    ?.let { standardDialogListener = it as? StandardDialogListener }
            }
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                standardDialogListener?.onPositiveClick(dialogTag, data)
            }
            DialogInterface.BUTTON_NEGATIVE -> {
                standardDialogListener?.onNegativeClick(dialogTag, data)
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
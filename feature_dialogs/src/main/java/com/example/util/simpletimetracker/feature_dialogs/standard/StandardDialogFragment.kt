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
import com.example.util.simpletimetracker.navigation.params.StandardDialogParams

class StandardDialogFragment : AppCompatDialogFragment(),
    DialogInterface.OnClickListener {

    private var standardDialogListener: StandardDialogListener? = null
    private val dialogTag: String? by lazy { arguments?.getString(ARGS_TAG) }
    private val title: String by lazy { arguments?.getString(ARGS_TITLE).orEmpty() }
    private val message: String by lazy { arguments?.getString(ARGS_MESSAGE).orEmpty() }
    private val btnPositive: String by lazy { arguments?.getString(ARGS_BTN_POSITIVE).orEmpty() }
    private val btnNegative: String by lazy { arguments?.getString(ARGS_BTN_NEGATIVE).orEmpty() }

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
                standardDialogListener?.onPositiveClick(dialogTag)
            }
            DialogInterface.BUTTON_NEGATIVE -> {
                standardDialogListener?.onNegativeClick(dialogTag)
            }
        }
    }

    companion object {
        private const val ARGS_TAG = "args_tag"
        private const val ARGS_TITLE = "args_title"
        private const val ARGS_MESSAGE = "args_message"
        private const val ARGS_BTN_POSITIVE = "args_btn_positive"
        private const val ARGS_BTN_NEGATIVE = "args_btn_negative"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is StandardDialogParams -> {
                    putString(ARGS_TAG, data.tag)
                    putString(ARGS_TITLE, data.title)
                    putString(ARGS_MESSAGE, data.message)
                    putString(ARGS_BTN_POSITIVE, data.btnPositive)
                    putString(ARGS_BTN_NEGATIVE, data.btnNegative)
                }
            }
        }
    }
}
package com.example.util.simpletimetracker.feature_dialogs.colorSelection.view

import com.example.util.simpletimetracker.feature_dialogs.databinding.ColorSelectionDialogFragmentBinding as Binding
import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.dialog.ColorSelectionDialogListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_dialogs.colorSelection.customView.ColorSelectionView
import com.example.util.simpletimetracker.feature_dialogs.colorSelection.model.HSVUpdate
import com.example.util.simpletimetracker.feature_dialogs.colorSelection.model.RGBUpdate
import com.example.util.simpletimetracker.feature_dialogs.colorSelection.viewModel.ColorSelectionViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.ColorSelectionDialogParams
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ColorSelectionDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: ColorSelectionViewModel by viewModels()

    private val params: ColorSelectionDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ColorSelectionDialogParams(),
    )
    private var colorSelectionDialogListener: ColorSelectionDialogListener? = null

    // TODO do better?
    private var textWatcherHex: TextWatcher? = null
    private var textWatcherR: TextWatcher? = null
    private var textWatcherG: TextWatcher? = null
    private var textWatcherB: TextWatcher? = null
    private var textWatcherH: TextWatcher? = null
    private var textWatcherS: TextWatcher? = null
    private var textWatcherV: TextWatcher? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is ColorSelectionDialogListener -> {
                colorSelectionDialogListener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is ColorSelectionDialogListener && it.isResumed }
                    ?.let { colorSelectionDialogListener = it as? ColorSelectionDialogListener }
            }
        }
    }

    override fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
    }

    override fun initUx() = with(binding) {
        sliderColorSelectionHue.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.onHueChanged(value)
            }
        }
        viewColorSelectionView.setListener(
            object : ColorSelectionView.ColorSelectedListener {
                override fun onColorSelected(saturation: Float, value: Float) {
                    viewModel.onColorChanged(saturation, value)
                }
            },
        )

        textWatcherHex = etColorSelectionHex
            .doAfterTextChanged { viewModel.onHexFieldChanged(it.toString()) }

        textWatcherR = etColorSelectionRed
            .doAfterTextChanged { viewModel.onRGBFieldsChanged(it.toString(), RGBUpdate.R) }
        textWatcherG = etColorSelectionGreen
            .doAfterTextChanged { viewModel.onRGBFieldsChanged(it.toString(), RGBUpdate.G) }
        textWatcherB = etColorSelectionBlue
            .doAfterTextChanged { viewModel.onRGBFieldsChanged(it.toString(), RGBUpdate.B) }

        textWatcherH = etColorSelectionHue
            .doAfterTextChanged { viewModel.onHSVFieldsChanged(it.toString(), HSVUpdate.H) }
        textWatcherS = etColorSelectionSaturation
            .doAfterTextChanged { viewModel.onHSVFieldsChanged(it.toString(), HSVUpdate.S) }
        textWatcherV = etColorSelectionValue
            .doAfterTextChanged { viewModel.onHSVFieldsChanged(it.toString(), HSVUpdate.V) }

        btnColorSelectionSave.setOnClick(viewModel::onSaveClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        colorData.observe {
            binding.sliderColorSelectionHue.value = it.colorHue

            binding.viewColorSelectionView.setHue(
                hue = it.colorHue,
                saturation = it.colorSaturation,
                value = it.colorValue,
            )

            binding.cardColorSelectionSelectedColor.setCardBackgroundColor(it.selectedColor)

            binding.etColorSelectionHex.setColorText(it.colorHex, textWatcherHex)
            binding.etColorSelectionRed.setColorText(it.colorRedString, textWatcherR)
            binding.etColorSelectionGreen.setColorText(it.colorGreenString, textWatcherG)
            binding.etColorSelectionBlue.setColorText(it.colorBlueString, textWatcherB)
            binding.etColorSelectionHue.setColorText(it.colorHueString, textWatcherH)
            binding.etColorSelectionSaturation.setColorText(it.colorSaturationString, textWatcherS)
            binding.etColorSelectionValue.setColorText(it.colorValueString, textWatcherV)
        }

        colorSelected.observe(::onColorSelected)
    }

    private fun TextInputEditText.setColorText(text: String, textWatcher: TextWatcher?) {
        if (text == this.text.toString()) return

        textWatcher?.let(::removeTextChangedListener)
        setText(text)
        setSelection(text.length)
        textWatcher?.let(::addTextChangedListener)
    }

    private fun onColorSelected(colorInt: Int) {
        colorSelectionDialogListener?.onColorSelected(colorInt)
        dismiss()
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: ColorSelectionDialogParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}
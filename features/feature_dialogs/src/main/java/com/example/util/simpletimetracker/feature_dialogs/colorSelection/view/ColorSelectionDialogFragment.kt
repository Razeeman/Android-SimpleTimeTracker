package com.example.util.simpletimetracker.feature_dialogs.colorSelection.view

import android.content.Context
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.ColorSelectionDialogListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_dialogs.colorSelection.customView.ColorSelectionView
import com.example.util.simpletimetracker.feature_dialogs.colorSelection.viewModel.ColorSelectionViewModel
import com.example.util.simpletimetracker.feature_dialogs.colorSelection.model.HSVUpdate
import com.example.util.simpletimetracker.feature_dialogs.colorSelection.model.RGBUpdate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_dialogs.databinding.ColorSelectionDialogFragmentBinding as Binding

@AndroidEntryPoint
class ColorSelectionDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<ColorSelectionViewModel>

    private val viewModel: ColorSelectionViewModel by viewModels(
        factoryProducer = { viewModelFactory }
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
            }
        )

        textWatcherHex = etColorSelectionHex
            .doAfterTextChanged { viewModel.onHexChanged(it.toString()) }

        textWatcherR = etColorSelectionRed
            .doAfterTextChanged { viewModel.onRGBChanged(it.toString(), RGBUpdate.R) }
        textWatcherG = etColorSelectionGreen
            .doAfterTextChanged { viewModel.onRGBChanged(it.toString(), RGBUpdate.G) }
        textWatcherB = etColorSelectionBlue
            .doAfterTextChanged { viewModel.onRGBChanged(it.toString(), RGBUpdate.B) }

        textWatcherH = etColorSelectionHue
            .doAfterTextChanged { viewModel.onHSVChanged(it.toString(), HSVUpdate.H) }
        textWatcherS = etColorSelectionSaturation
            .doAfterTextChanged { viewModel.onHSVChanged(it.toString(), HSVUpdate.S) }
        textWatcherV = etColorSelectionValue
            .doAfterTextChanged { viewModel.onHSVChanged(it.toString(), HSVUpdate.V) }

        btnColorSelectionSave.setOnClick(viewModel::onSaveClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        colorData.observe {
            binding.viewColorSelectionView.setHue(
                hue = it.colorHue,
                saturation = it.colorSaturation,
                value = it.colorValue
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

    private fun onColorSelected(colorHex: String) {
        colorSelectionDialogListener?.onColorSelected(colorHex)
        dismiss()
    }
}
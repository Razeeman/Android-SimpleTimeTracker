package com.example.util.simpletimetracker.feature_dialogs.colorSelection

import android.content.Context
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

        etColorSelectionRed.doAfterTextChanged { viewModel.onRGBChanged(it.toString(), RGBUpdate.R) }
        etColorSelectionGreen.doAfterTextChanged { viewModel.onRGBChanged(it.toString(), RGBUpdate.G) }
        etColorSelectionBlue.doAfterTextChanged { viewModel.onRGBChanged(it.toString(), RGBUpdate.B) }

        etColorSelectionHue.doAfterTextChanged { viewModel.onHSVChanged(it.toString(), HSVUpdate.H) }
        etColorSelectionSaturation.doAfterTextChanged { viewModel.onHSVChanged(it.toString(), HSVUpdate.S) }
        etColorSelectionValue.doAfterTextChanged { viewModel.onHSVChanged(it.toString(), HSVUpdate.V) }

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

            // TODO set text silently
            binding.etColorSelectionRed.setColorText(it.colorRedString)
            binding.etColorSelectionGreen.setColorText(it.colorGreenString)
            binding.etColorSelectionBlue.setColorText(it.colorBlueString)
            binding.etColorSelectionHue.setColorText(it.colorHueString)
            binding.etColorSelectionSaturation.setColorText(it.colorSaturationString)
            binding.etColorSelectionValue.setColorText(it.colorValueString)
        }

        colorSelected.observe(::onColorSelected)
    }

    private fun TextInputEditText.setColorText(text: String) {
        if (text == this.text.toString()) return

        setText(text)
        setSelection(text.length)
    }

    private fun onColorSelected(colorHex: String) {
        colorSelectionDialogListener?.onColorSelected(colorHex)
        dismiss()
    }
}
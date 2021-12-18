package com.example.util.simpletimetracker.feature_dialogs.colorSelection

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.ColorSelectionDialogListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
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
    }

    override fun initViewModel(): Unit = with(viewModel) {
        // TODO add RGB and HSV value fields
        colorIntData.observe(binding.ivColorSelectionSelectedColor::setBackgroundColor)
        colorData.observe {
            binding.viewColorSelectionView.setHue(
                hue = it.colorHue,
                saturation = it.colorSaturation,
                value = it.colorValue
            )
        }
        colorSelected.observe(::onColorSelected)
    }

    private fun onColorSelected(colorHex: String) {
        colorSelectionDialogListener?.onColorSelected(colorHex)
        dismiss()
    }
}
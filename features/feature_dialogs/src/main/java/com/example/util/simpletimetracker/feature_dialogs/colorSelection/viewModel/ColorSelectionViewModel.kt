package com.example.util.simpletimetracker.feature_dialogs.colorSelection.viewModel

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.extension.padDuration
import com.example.util.simpletimetracker.feature_dialogs.colorSelection.model.HSVUpdate
import com.example.util.simpletimetracker.feature_dialogs.colorSelection.model.RGBUpdate
import com.example.util.simpletimetracker.feature_dialogs.colorSelection.viewData.ColorSelectionViewData
import com.example.util.simpletimetracker.navigation.params.screen.ColorSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ColorSelectionViewModel @Inject constructor() : BaseViewModel() {

    lateinit var extra: ColorSelectionDialogParams

    val colorData: LiveData<ColorSelectionViewData> by lazySuspend {
        initialize()
        loadColorData()
    }
    val colorSelected: LiveData<Int> = MutableLiveData()

    private var colorHex: String = "#ff0000"
    private var colorRed: Int = 255 // 0..255
    private var colorBlue: Int = 0 // 0..255
    private var colorGreen: Int = 0 // 0..255
    private var colorHue: Float = 0f // 0..360
    private var colorSaturation: Float = 1f // 0..1
    private var colorValue: Float = 1f // 0..1
    private var colorUpdateJob: Job? = null

    fun onHueChanged(hue: Float) {
        colorHue = hue.coerceIn(0f, 360f)
        onHSVChanged()
    }

    fun onColorChanged(saturation: Float, value: Float) {
        colorSaturation = saturation.coerceIn(0f, 1f)
        colorValue = value.coerceIn(0f, 1f)
        onHSVChanged()
    }

    fun onHexFieldChanged(colorHex: String) {
        runCatching {
            (if (colorHex.startsWith("#")) colorHex else "#$colorHex")
                .let(Color::parseColor)
                .let(::onHexChanged)
        }
    }

    fun onRGBFieldsChanged(colorString: String, update: RGBUpdate) {
        val newColor = colorString.toIntOrNull()?.coerceIn(0, 255) ?: return

        when (update) {
            RGBUpdate.R -> colorRed = newColor
            RGBUpdate.G -> colorGreen = newColor
            RGBUpdate.B -> colorBlue = newColor
        }

        onRGBChanged()
    }

    fun onHSVFieldsChanged(colorString: String, update: HSVUpdate) {
        val newColor = colorString.toIntOrNull() ?: return

        newColor.apply {
            when (update) {
                HSVUpdate.H -> coerceIn(0, 360).let {
                    colorHue = it.toFloat()
                }
                HSVUpdate.S -> coerceIn(0, 100).let {
                    colorSaturation = it.toFloat() / 100f
                }
                HSVUpdate.V -> coerceIn(0, 100).let {
                    colorValue = it.toFloat() / 100f
                }
            }
        }

        onHSVChanged()
    }

    fun onSaveClick() {
        getCurrentColorInt().let(colorSelected::set)
    }

    fun onRandomClick() {
        colorRed = (0..255).random()
        colorGreen = (0..255).random()
        colorBlue = (0..255).random()

        onRGBChanged()
    }

    private fun onHexChanged(@ColorInt newHexColor: Int) {
        val hsv = FloatArray(3)
        Color.colorToHSV(newHexColor, hsv)

        colorHex = mapColorToHex(newHexColor)
        colorRed = newHexColor.red
        colorGreen = newHexColor.green
        colorBlue = newHexColor.blue
        colorHue = hsv[0]
        colorSaturation = hsv[1]
        colorValue = hsv[2]

        updateColorData()
    }

    private fun onHSVChanged() {
        val newHSVColor = floatArrayOf(colorHue, colorSaturation, colorValue)
            .let(Color::HSVToColor)

        colorHex = mapColorToHex(newHSVColor)
        colorRed = Color.red(newHSVColor)
        colorGreen = Color.green(newHSVColor)
        colorBlue = Color.blue(newHSVColor)

        updateColorData()
    }

    private fun onRGBChanged() {
        val newRGBColor = Color.rgb(colorRed, colorGreen, colorBlue)
        val hsv = FloatArray(3)
        Color.RGBToHSV(colorRed, colorGreen, colorBlue, hsv)

        colorHex = mapColorToHex(newRGBColor)
        colorHue = hsv[0]
        colorSaturation = hsv[1]
        colorValue = hsv[2]

        updateColorData()
    }

    private fun initialize() {
        val hsv = FloatArray(3)
        Color.colorToHSV(extra.preselectedColor, hsv)

        colorHex = mapColorToHex(extra.preselectedColor)
        colorRed = Color.red(extra.preselectedColor)
        colorGreen = Color.green(extra.preselectedColor)
        colorBlue = Color.blue(extra.preselectedColor)
        colorHue = hsv[0]
        colorSaturation = hsv[1]
        colorValue = hsv[2]
    }

    private fun updateColorData() {
        colorUpdateJob?.cancel()
        colorUpdateJob = viewModelScope.launch {
            val data = loadColorData()
            colorData.set(data)
        }
    }

    private fun loadColorData(): ColorSelectionViewData {
        return ColorSelectionViewData(
            selectedColor = getCurrentColorInt(),
            colorHue = colorHue,
            colorSaturation = colorSaturation,
            colorValue = colorValue,
            colorHex = colorHex,
            colorRedString = colorRed.toString(),
            colorGreenString = colorGreen.toString(),
            colorBlueString = colorBlue.toString(),
            colorHueString = colorHue.toInt().toString(),
            colorSaturationString = (colorSaturation * 100).toInt().toString(),
            colorValueString = (colorValue * 100).toInt().toString(),
        )
    }

    private fun mapColorToHex(@ColorInt colorInt: Int): String {
        val currentRed = Color.red(colorInt)
            .let(Integer::toHexString).padDuration()
        val currentGreen = Color.green(colorInt)
            .let(Integer::toHexString).padDuration()
        val currentBlue = Color.blue(colorInt)
            .let(Integer::toHexString).padDuration()

        return "#$currentRed$currentGreen$currentBlue"
    }

    @ColorInt
    private fun getCurrentColorInt(): Int {
        return Color.rgb(colorRed, colorGreen, colorBlue)
    }
}

package com.example.util.simpletimetracker.feature_dialogs.colorSelection

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.util.simpletimetracker.core.extension.set
import javax.inject.Inject

class ColorSelectionViewModel @Inject constructor() : ViewModel() {

    val colorIntData: LiveData<Int> by lazy { MutableLiveData(loadColorInt()) }
    val colorData: LiveData<ColorSelectionViewData> by lazy { MutableLiveData(loadColorData()) }
    val colorSelected: LiveData<String> = MutableLiveData()

    private var colorHue: Float = 0f // 0..360
    private var colorSaturation: Float = 1f // 0..1
    private var colorValue: Float = 1f // 0..1

    fun onHueChanged(hue: Float) {
        colorHue = hue.coerceIn(0f, 360f)
        updateColorData()
        updateColorInt()
    }

    fun onColorChanged(saturation: Float, value: Float) {
        colorSaturation = saturation.coerceIn(0f, 1f)
        colorValue = value.coerceIn(0f, 1f)
        updateColorData()
        updateColorInt()
    }

    // TODO add save
    fun onSaveClick() {
        // TODO format color to HEX
        colorSelected.set("")
    }

    private fun updateColorInt() {
        val data = loadColorInt()
        colorIntData.set(data)
    }

    @ColorInt private fun loadColorInt(): Int {
        return floatArrayOf(colorHue, colorSaturation, colorValue)
            .let(Color::HSVToColor)
    }

    private fun updateColorData() {
        val data = loadColorData()
        colorData.set(data)
    }

    private fun loadColorData(): ColorSelectionViewData {
        return ColorSelectionViewData(
            colorHue = colorHue,
            colorSaturation = colorSaturation,
            colorValue = colorValue
        )
    }
}

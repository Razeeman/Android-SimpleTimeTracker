package com.example.util.simpletimetracker.feature_color_selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.ColorViewDataInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.favourite.interactor.FavouriteColorInteractor
import com.example.util.simpletimetracker.domain.favourite.model.FavouriteColor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorViewData
import com.example.util.simpletimetracker.feature_color_selection.api.ColorSelectionViewModelDelegate
import com.example.util.simpletimetracker.feature_color_selection.api.GradientControlsViewData
import com.example.util.simpletimetracker.feature_color_selection.api.GradientEndpoint
import com.example.util.simpletimetracker.feature_color_selection.mapper.ColorSelectionMapper
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ColorSelectionDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

class ColorSelectionViewModelDelegateImpl @Inject constructor(
    private val router: Router,
    private val colorMapper: ColorMapper,
    private val colorSelectionMapper: ColorSelectionMapper,
    private val colorViewDataInteractor: ColorViewDataInteractor,
    private val favouriteColorInteractor: FavouriteColorInteractor,
) : ColorSelectionViewModelDelegate,
    ViewModelDelegate() {

    override val colors: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            delegateScope.launch { initial.value = loadColorsViewData() }
            initial
        }
    }
    override var newColor: AppColor = AppColor(
        colorId = ColorMapper.getAvailableColors().indices.random(),
        colorInt = "",
        gradient = null,
    )

    // Keep the end color and angle while gradient mode is disabled during this edit.
    private var retainedGradient: AppColor.Gradient? = null
    private var selectedEndpoint = GradientEndpoint.Start
    private var parent: ColorSelectionViewModelDelegate.Parent? = null

    override fun attach(parent: ColorSelectionViewModelDelegate.Parent) {
        this.parent = parent
    }

    override fun clearColorDelegate() {
        clear()
    }

    override suspend fun updateColorViewData() {
        updateColors()
    }

    override fun onColorClick(item: ColorViewData) {
        val color = when (item.type) {
            is ColorViewData.Type.Base -> AppColor(item.colorId.toInt(), "", null)
            is ColorViewData.Type.Favourite -> AppColor(0, item.colorInt.toString(), null)
        }
        selectColor(color)
    }

    override fun onColorPaletteClick() {
        ColorSelectionDialogParams(
            tag = parent?.getDialogTag() ?: return,
            preselectedColor = colorMapper.mapToColorInt(
                color = getEndpointColor(selectedEndpoint),
                isDarkTheme = false, // Pass original, not darkened color.
            ),
        ).let(router::navigate)
    }

    override fun onColorFavouriteClick() {
        delegateScope.launch {
            val currentColor = getEndpointColor(selectedEndpoint).colorInt
                .takeUnless { it.isEmpty() } ?: return@launch
            val currentFavourite = favouriteColorInteractor.get(currentColor)
            if (currentFavourite != null) {
                favouriteColorInteractor.remove(currentFavourite.id)
            } else {
                // Zero id creates new record
                FavouriteColor(
                    id = 0L,
                    colorInt = currentColor,
                ).let { favouriteColorInteractor.add(it) }
            }
            updateColors()
        }
    }

    override fun onGradientClick() {
        if (parent?.supportsGradients != true) return
        val gradient = newColor.gradient
        if (gradient != null) {
            retainedGradient = gradient
            val newGradient = null
            newColor = newColor.copy(gradient = newGradient)
        } else {
            val newGradient = retainedGradient ?: colorSelectionMapper.createDefaultGradient(newColor)
            newColor = newColor.copy(gradient = newGradient)
            retainedGradient = null
        }
        selectedEndpoint = GradientEndpoint.Start
        onColorChanged()
    }

    override fun onGradientEndpointClick(endpoint: GradientEndpoint) {
        if (newColor.gradient == null || parent?.supportsGradients != true) return
        if (selectedEndpoint == endpoint) return
        selectedEndpoint = endpoint
        delegateScope.launch { updateColors() }
    }

    override fun onGradientAngleChange(angle: Int) {
        if (parent?.supportsGradients != true) return
        val gradient = newColor.gradient ?: return
        val steppedAngle = (angle.coerceIn(0, 360) / 5f).roundToInt() * 5
        if (gradient.angleDegrees == steppedAngle) return
        val newGradient = gradient.copy(angleDegrees = steppedAngle)
        newColor = newColor.copy(gradient = newGradient)
        // The palette and its selection do not change when rotating the gradient.
        val newData = colors.value.orEmpty().map { item ->
            if (item is GradientControlsViewData) loadGradientControlsViewData() else item
        }
        colors.set(newData)
        delegateScope.launch { parent?.update() }
    }

    override fun onCustomColorSelected(tag: String, colorInt: Int) {
        if (tag != parent?.getDialogTag()) return
        val newColor = AppColor(colorId = 0, colorInt = colorInt.toString(), gradient = null)
        selectColor(newColor)
    }

    private fun selectColor(color: AppColor) {
        if (getEndpointColor(selectedEndpoint) == color) return
        when (selectedEndpoint) {
            GradientEndpoint.Start -> {
                newColor = color.copy(gradient = newColor.gradient)
            }
            GradientEndpoint.End -> {
                val gradient = (newColor.gradient ?: retainedGradient)?.copy(
                    endColorId = color.colorId,
                    endColorInt = color.colorInt,
                )
                if (newColor.gradient != null) {
                    newColor = newColor.copy(gradient = gradient)
                } else {
                    retainedGradient = gradient
                }
            }
        }
        onColorChanged()
    }

    private fun getEndpointColor(endpoint: GradientEndpoint): AppColor {
        val gradient = newColor.gradient ?: retainedGradient
        return if (endpoint == GradientEndpoint.End && gradient != null) {
            AppColor(gradient.endColorId, gradient.endColorInt, null)
        } else {
            newColor.copy(gradient = null)
        }
    }

    private fun onColorChanged() {
        delegateScope.launch {
            parent?.onColorSelected()
            parent?.update()
            updateColors()
        }
    }

    private fun loadGradientControlsViewData(): GradientControlsViewData {
        val startColor = getEndpointColor(GradientEndpoint.Start)
        val endColor = getEndpointColor(GradientEndpoint.End)
        return GradientControlsViewData(
            endpoint = selectedEndpoint,
            angleDegrees = newColor.gradient?.angleDegrees ?: ColorSelectionMapper.DEFAULT_GRADIENT_ANGLE,
            startColor = colorMapper.mapToColorInt(startColor, isDarkTheme = false),
            endColor = colorMapper.mapToColorInt(endColor, isDarkTheme = false),
        )
    }

    private suspend fun updateColors() {
        val data = loadColorsViewData()
        colors.set(data)
    }

    private suspend fun loadColorsViewData(): List<ViewHolderType> {
        val currentColor = getEndpointColor(selectedEndpoint)
            .takeIf { parent?.isColorSelectedCheck().orFalse() }
        val gradientSelected = if (parent?.supportsGradients == true) {
            newColor.gradient != null
        } else {
            null
        }

        val data = mutableListOf<ViewHolderType>()

        if (parent?.supportsGradients == true && newColor.gradient != null) {
            data += loadGradientControlsViewData()
        }
        data += colorViewDataInteractor.getColorsViewData(
            currentColor = currentColor,
            gradientSelected = gradientSelected,
        )
        return data
    }
}

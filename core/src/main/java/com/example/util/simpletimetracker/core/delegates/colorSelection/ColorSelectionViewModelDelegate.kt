package com.example.util.simpletimetracker.core.delegates.colorSelection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.ColorViewDataInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.interactor.FavouriteColorInteractor
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.FavouriteColor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ColorSelectionDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO move to module
interface ColorSelectionViewModelDelegate {
    val colors: LiveData<List<ViewHolderType>>

    fun attach(parent: Parent)
    fun onColorClick(item: ColorViewData)
    fun onColorPaletteClick()
    fun onColorFavouriteClick()
    fun onCustomColorSelected(colorInt: Int)

    interface Parent {
        suspend fun update()
        fun onColorSelected() = Unit
        suspend fun isColorSelectedCheck(): Boolean = true
    }
}

class ColorSelectionViewModelDelegateImpl @Inject constructor(
    private val router: Router,
    private val colorMapper: ColorMapper,
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
    var newColor: AppColor = AppColor(
        colorId = (0..ColorMapper.colorsNumber).random(),
        colorInt = "",
    )

    private var parent: ColorSelectionViewModelDelegate.Parent? = null

    override fun attach(parent: ColorSelectionViewModelDelegate.Parent) {
        this.parent = parent
    }

    suspend fun update() {
        updateColors()
    }

    override fun onColorClick(item: ColorViewData) {
        delegateScope.launch {
            when (item.type) {
                is ColorViewData.Type.Base -> onBaseColorSelected(item.colorId.toInt())
                is ColorViewData.Type.Favourite -> onCustomColorSelected(item.colorInt)
            }
        }
    }

    override fun onColorPaletteClick() {
        ColorSelectionDialogParams(
            preselectedColor = colorMapper.mapToColorInt(
                color = newColor,
                isDarkTheme = false, // Pass original, not darkened color.
            ),
        ).let(router::navigate)
    }

    override fun onColorFavouriteClick() {
        delegateScope.launch {
            val currentColor = newColor.colorInt
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

    private fun onBaseColorSelected(colorId: Int) {
        delegateScope.launch {
            if (colorId != newColor.colorId || newColor.colorInt.isNotEmpty()) {
                newColor = AppColor(colorId = colorId, colorInt = "")
                parent?.onColorSelected()
                parent?.update()
                updateColors()
            }
        }
    }

    override fun onCustomColorSelected(colorInt: Int) {
        delegateScope.launch {
            if (colorInt.toString() != newColor.colorInt) {
                newColor = AppColor(colorId = 0, colorInt = colorInt.toString())
                parent?.onColorSelected()
                parent?.update()
                updateColors()
            }
        }
    }

    private suspend fun updateColors() {
        val data = loadColorsViewData()
        colors.set(data)
    }

    private suspend fun loadColorsViewData(): List<ViewHolderType> {
        val currentColor = newColor.takeIf { parent?.isColorSelectedCheck().orFalse() }
        return colorViewDataInteractor.getColorsViewData(currentColor)
    }
}

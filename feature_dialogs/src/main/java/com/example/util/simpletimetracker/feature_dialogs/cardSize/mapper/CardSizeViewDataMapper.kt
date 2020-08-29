package com.example.util.simpletimetracker.feature_dialogs.cardSize.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.repo.DeviceRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.cardSize.viewData.CardSizeButtonsViewData
import javax.inject.Inject

class CardSizeViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val deviceRepo: DeviceRepo
) {

    private val screenWidth: Int by lazy {
        deviceRepo.getScreenWidthInDp()
    }
    private val minCardWidth: Int by lazy {
        resourceRepo.getDimenInDp(R.dimen.record_type_card_min_width)
    }
    private val defaultCardWidth: Int by lazy {
        resourceRepo.getDimenInDp(R.dimen.record_type_card_width)
    }

    fun progressToWidth(progress: Int): Int {
        // TODO different interpolation
        return minCardWidth + (screenWidth - minCardWidth) * progress / 100
    }

    fun widthToProgress(width: Int): Int {
        // TODO different interpolation
        val normalized = width.coerceIn(minCardWidth, screenWidth)
        return (normalized - minCardWidth) * 100 / (screenWidth - minCardWidth)
    }

    fun buttonTypeToWidth(type: CardSizeButtonsViewData.Type): Int {
        return when (type) {
            CardSizeButtonsViewData.Type.MIN -> minCardWidth
            CardSizeButtonsViewData.Type.DEFAULT -> defaultCardWidth
            CardSizeButtonsViewData.Type.MAX -> screenWidth
        }
    }

    fun mapToButtonsViewData(width: Int): List<ViewHolderType> {
        return listOf(
            CardSizeButtonsViewData.Type.MIN,
            CardSizeButtonsViewData.Type.DEFAULT,
            CardSizeButtonsViewData.Type.MAX
        ).map {
            CardSizeButtonsViewData(
                type = it,
                name = mapToGroupingName(it),
                isSelected = mapToIsSelected(width, it)
            )
        }
    }

    private fun mapToGroupingName(type: CardSizeButtonsViewData.Type): String {
        return when (type) {
            CardSizeButtonsViewData.Type.MIN -> R.string.card_size_min
            CardSizeButtonsViewData.Type.DEFAULT -> R.string.card_size_default
            CardSizeButtonsViewData.Type.MAX -> R.string.card_size_max
        }.let(resourceRepo::getString)
    }

    private fun mapToIsSelected(width: Int, type: CardSizeButtonsViewData.Type): Boolean {
        return when {
            width <= minCardWidth && type == CardSizeButtonsViewData.Type.MIN -> true
            width == defaultCardWidth && type == CardSizeButtonsViewData.Type.DEFAULT -> true
            width >= screenWidth && type == CardSizeButtonsViewData.Type.MAX -> true
            else -> false
        }
    }
}
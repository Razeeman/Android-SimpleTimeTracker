package com.example.util.simpletimetracker.feature_shortcuts.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_shortcuts.viewData.ShortcutsButtonViewData
import javax.inject.Inject

class ShortcutsViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun mapReorderHint(): HintViewData {
        return HintViewData(
            text = resourceRepo.getString(R.string.card_order_hint),
        )
    }

    fun mapAddItem(
        isDarkTheme: Boolean,
    ): ButtonViewData {
        return ButtonViewData(
            id = ShortcutsButtonViewData(
                block = ShortcutsButtonViewData.Block.ADD,
            ),
            text = resourceRepo.getString(R.string.running_records_add_type),
            icon = ButtonViewData.Icon.Hidden,
            backgroundColor = resourceRepo.getThemedAttr(
                R.attr.appInactiveColor,
                isDarkTheme,
            ),
            isEnabled = true,
            marginHorizontalDp = 4,
        )
    }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.settings

import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.data.WearResourceRepo
import com.example.util.simpletimetracker.presentation.ui.components.SettingsItem
import com.example.util.simpletimetracker.presentation.ui.components.SettingsListState
import com.example.util.simpletimetracker.wear_api.WearSettings
import javax.inject.Inject

class SettingsViewDataMapper @Inject constructor(
    private val resourceRepo: WearResourceRepo,
) {

    fun mapErrorState(): SettingsListState.Error {
        return SettingsListState.Error(R.string.wear_loading_error)
    }

    fun mapContentState(
        showCompactList: Boolean,
        wearSettings: WearSettings,
    ): SettingsListState.Content {
        val items = mutableListOf<SettingsItem>()

        items += SettingsItem.CheckBox(
            type = SettingsItemType.AllowMultitasking,
            text = resourceRepo.getString(R.string.settings_allow_multitasking),
            hint = resourceRepo.getString(R.string.settings_allow_multitasking_hint),
            checked = wearSettings.allowMultitasking,
        )

        items += SettingsItem.CheckBox(
            type = SettingsItemType.ShowCompactList,
            text = resourceRepo.getString(R.string.wear_settings_title_show_compact_list),
            hint = "",
            checked = showCompactList,
        )

        return SettingsListState.Content(
            items = items,
        )
    }
}
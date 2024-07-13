/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.CheckboxDefaults
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.util.simpletimetracker.presentation.screens.settings.SettingsItemType
import com.example.util.simpletimetracker.presentation.theme.ColorAccent

// TODO split into separate files
sealed interface SettingsItem {
    val type: SettingsItemType

    data class CheckBox(
        override val type: SettingsItemType,
        val text: String,
        val checked: Boolean,
    ) : SettingsItem

    data class Hint(
        override val type: SettingsItemType,
        val hint: String,
    ) : SettingsItem
}

@Composable
fun SettingsCheckbox(
    state: SettingsItem.CheckBox,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            Modifier
                .padding(vertical = 3.dp)
                .padding(horizontal = 4.dp)
                .fillMaxWidth()
                .weight(1f),
        ) {
            Text(
                text = state.text,
                fontWeight = FontWeight.Medium,
            )
        }
        Checkbox(
            modifier = Modifier,
            checked = state.checked,
            colors = CheckboxDefaults.colors(
                checkedBoxColor = ColorAccent,
                checkedCheckmarkColor = ColorAccent,
                uncheckedBoxColor = Color.White,
                uncheckedCheckmarkColor = Color.White,
            ),
        )
    }
}

@Composable
fun SettingsHint(
    state: SettingsItem.Hint,
) {
    Column(
        Modifier
            .padding(bottom = 3.dp)
            .padding(horizontal = 4.dp)
            .fillMaxWidth(),
    ) {
        Text(
            text = state.hint,
            fontWeight = FontWeight.Light,
            fontSize = 11.sp,
            lineHeight = 11.sp,
        )
    }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SettingsCheckboxPreview() {
    SettingsCheckbox(
        state = SettingsItem.CheckBox(
            type = SettingsItemType.ShowCompactList,
            text = "Check box",
            checked = false,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SettingsCheckboxCheckedPreview() {
    SettingsCheckbox(
        state = SettingsItem.CheckBox(
            type = SettingsItemType.ShowCompactList,
            text = "Check box",
            checked = true,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SettingsCheckboxHintPreview() {
    SettingsCheckbox(
        state = SettingsItem.CheckBox(
            type = SettingsItemType.ShowCompactList,
            text = "Check box",
            checked = false,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SettingsCheckboxLongPreview() {
    SettingsCheckbox(
        state = SettingsItem.CheckBox(
            type = SettingsItemType.ShowCompactList,
            text = "Check box Check box Check box Check box Check box Check box Check box Check box ",
            checked = false,
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SettingsHintPreview() {
    SettingsHint(
        state = SettingsItem.Hint(
            type = SettingsItemType.AllowMultitaskingHint,
            hint = "Check box hint",
        ),
    )
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
private fun SettingsHintLongPreview() {
    SettingsHint(
        state = SettingsItem.Hint(
            type = SettingsItemType.AllowMultitaskingHint,
            hint = "Check box hint Check box hint Check box hint Check box hint Check box hint ",
        ),
    )
}
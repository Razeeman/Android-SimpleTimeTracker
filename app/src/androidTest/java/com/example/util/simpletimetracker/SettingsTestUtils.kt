package com.example.util.simpletimetracker

import android.view.View
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.unconstrainedClickOnView
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import com.example.util.simpletimetracker.feature_settings.R as settingsR

// Scroll

fun scrollSettingsRecyclerToText(@StringRes id: Int) {
    scrollSettingsRecyclerToView(hasDescendant(withText(id)))
}

fun scrollSettingsRecyclerToText(text: String) {
    scrollSettingsRecyclerToView(hasDescendant(withText(text)))
}

// Click

fun clickOnSettingsRecyclerText(@StringRes id: Int) {
    clickOnSettingsRecyclerItem(withText(id))
}

fun clickOnSettingsCheckboxBesideText(@StringRes id: Int) {
    unconstrainedClickOnView(settingsCheckboxBesideText(id))
}

fun clickOnSettingsButtonBesideText(@StringRes id: Int) {
    clickOnView(allOf(hasSibling(withText(id)), withId(settingsR.id.btnItemSettings)))
}

fun clickOnSettingsSpinnerButtonBesideText(@StringRes id: Int) {
    clickOnView(allOf(hasSibling(withText(id)), withId(settingsR.id.btnItemSettings)))
}

fun clickOnSettingsSpinnerBesideText(@StringRes id: Int) {
    // Double click to avoid failure on low api small screens
    clickOnSettingsRecyclerItem(
        allOf(hasSibling(withText(id)), withId(settingsR.id.spinnerItemSettings)),
    )
    pressBack()
    clickOnSettingsRecyclerItem(
        allOf(hasSibling(withText(id)), withId(settingsR.id.spinnerItemSettings)),
    )
}

fun clickOnSettingsSelectorBesideText(@StringRes id: Int) {
    clickOnView(allOf(hasSibling(withText(id)), withId(settingsR.id.groupItemSettingsSelector)))
}

fun clickOnSettingsRangeStartBesideText(@StringRes id: Int) {
    clickOnView(allOf(hasSibling(withText(id)), withId(settingsR.id.tvItemSettingsStart)))
}

fun clickOnSettingsRangeEndBesideText(@StringRes id: Int) {
    clickOnView(allOf(hasSibling(withText(id)), withId(settingsR.id.tvItemSettingsEnd)))
}

// Matchers

fun settingsSubtitleBesideText(
    @StringRes id: Int,
    matcher: Matcher<View>,
): Matcher<View> {
    return settingsViewBesideTextMatcher(
        id = id,
        matcher = allOf(withId(settingsR.id.tvItemSettingsSubtitle), matcher),
    )
}

fun settingsButtonBesideText(
    @StringRes id: Int,
    matcher: Matcher<View>? = null,
): Matcher<View> {
    val matchers = listOfNotNull(
        withId(settingsR.id.btnItemSettings),
        matcher,
    )
    return settingsViewBesideTextMatcher(
        id = id,
        matcher = allOf(matchers),
    )
}

fun settingsCheckboxBesideText(
    @StringRes id: Int,
): Matcher<View> {
    return settingsViewBesideTextMatcher(
        id = id,
        matcher = withId(settingsR.id.checkboxItemSettings),
    )
}

fun settingsSelectorValueBesideText(
    @StringRes id: Int,
    matcher: Matcher<View>,
): Matcher<View> {
    return settingsViewBesideTextMatcher(
        id = id,
        matcher = allOf(
            withId(settingsR.id.groupItemSettingsSelector),
            hasDescendant(allOf(withId(settingsR.id.tvItemSettingsSelectorValue), matcher)),
        ),
    )
}

fun settingsSpinnerValueBesideText(
    @StringRes id: Int,
    matcher: Matcher<View>,
): Matcher<View> {
    return settingsViewBesideTextMatcher(
        id = id,
        matcher = allOf(withId(settingsR.id.tvItemSettingsValue), matcher),
    )
}

fun settingsRangeStartBesideText(
    @StringRes id: Int,
    matcher: Matcher<View>? = null,
): Matcher<View> {
    val matchers = listOfNotNull(
        withId(settingsR.id.tvItemSettingsStart),
        matcher,
    )
    return settingsViewBesideTextMatcher(
        id = id,
        matcher = allOf(matchers),
    )
}

fun settingsRangeEndBesideText(
    @StringRes id: Int,
    matcher: Matcher<View>? = null,
): Matcher<View> {
    val matchers = listOfNotNull(
        withId(settingsR.id.tvItemSettingsEnd),
        matcher,
    )
    return settingsViewBesideTextMatcher(
        id = id,
        matcher = allOf(matchers),
    )
}

// Private

private fun scrollSettingsRecyclerToView(matcher: Matcher<View>) {
    onView(withId(settingsR.id.rvSettingsContent))
        .perform(scrollTo<BaseRecyclerViewHolder>(matcher))
}

private fun clickOnSettingsRecyclerItem(matcher: Matcher<View>) {
    clickOnView(allOf(isDescendantOfA(withId(settingsR.id.rvSettingsContent)), matcher))
}

private fun settingsViewBesideTextMatcher(@StringRes id: Int, matcher: Matcher<View>): Matcher<View> {
    return allOf(
        isDescendantOfA(withId(settingsR.id.rvSettingsContent)),
        hasSibling(withText(id)),
        matcher,
    )
}

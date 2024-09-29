package com.example.util.simpletimetracker

import android.view.View
import androidx.annotation.StringRes
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import com.example.util.simpletimetracker.feature_complex_rules.R as complexRulesR

object ComplexRulesTestUtils {

    fun checkListView(
        @StringRes actionStringResId: Int,
        assignTagNames: List<String> = emptyList(),
        startingTypeNames: List<String> = emptyList(),
        currentTypeNames: List<String> = emptyList(),
        daysOfWeek: List<DayOfWeek> = emptyList(),
        timeMapper: TimeMapper,
    ) {
        fun getElementMatcher(
            name: String,
            forConditions: Boolean,
        ): Matcher<View> {
            val containerId = if (forConditions) {
                complexRulesR.id.rvComplexRuleItemConditions
            } else {
                complexRulesR.id.rvComplexRuleItemActions
            }
            return hasDescendant(
                allOf(
                    withId(containerId),
                    hasDescendant(withText(name)),
                ),
            )
        }

        val matchers = mutableListOf<Matcher<View>>()
        matchers += withId(complexRulesR.id.containerComplexRuleItem)
        matchers += hasDescendant(
            allOf(
                withId(complexRulesR.id.rvComplexRuleItemActions),
                hasDescendant(withText(actionStringResId)),
            ),
        )
        matchers += assignTagNames.map {
            getElementMatcher(
                name = it,
                forConditions = false,
            )
        }
        matchers += startingTypeNames.map {
            getElementMatcher(
                name = it,
                forConditions = true,
            )
        }
        matchers += currentTypeNames.map {
            getElementMatcher(
                name = it,
                forConditions = true,
            )
        }
        matchers += daysOfWeek.map(timeMapper::toShortDayOfWeekName).map {
            getElementMatcher(
                name = it,
                forConditions = true,
            )
        }

        checkViewIsDisplayed(allOf(matchers))
    }
}

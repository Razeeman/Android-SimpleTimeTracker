package com.example.util.simpletimetracker.feature_change_record_type.adapter

import androidx.core.view.doOnLayout
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_color_selection.api.GradientEndpoint
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttrRes
import com.example.util.simpletimetracker.feature_views.extension.safeUpdateLayoutParams
import com.example.util.simpletimetracker.feature_views.extension.setRounded
import com.google.android.flexbox.FlexboxLayoutManager
import kotlin.math.roundToInt
import com.example.util.simpletimetracker.feature_change_record_type.databinding.ItemChangeRecordTypeGradientControlsLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_color_selection.api.GradientControlsViewData as ViewData

fun createChangeRecordTypeGradientControlsAdapterDelegate(
    onEndpointClick: (GradientEndpoint) -> Unit,
    onAngleChange: (Int) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->
    with(binding) {
        item as ViewData

        root.safeUpdateLayoutParams<FlexboxLayoutManager.LayoutParams> { isWrapBefore = true }
        sliderChangeRecordTypeGradientAngle.clearOnChangeListeners()

        val borderResId = root.context.getThemedAttrRes(R.attr.appInputFieldBorder)
        btnChangeRecordTypeGradientStart
            .setBackgroundResource(if (item.endpoint == GradientEndpoint.Start) borderResId else 0)
        btnChangeRecordTypeGradientEnd
            .setBackgroundResource(if (item.endpoint == GradientEndpoint.End) borderResId else 0)

        // Rounded outlines need the measured bounds, which are not available on the first bind.
        btnChangeRecordTypeGradientStart.doOnLayout { it.setRounded(8) }
        viewChangeRecordTypeGradientStartColor.doOnLayout { it.setRounded(6) }
        viewChangeRecordTypeGradientStartColor.setBackgroundColor(item.startColor)
        btnChangeRecordTypeGradientEnd.doOnLayout { it.setRounded(8) }
        viewChangeRecordTypeGradientEndColor.doOnLayout { it.setRounded(6) }
        viewChangeRecordTypeGradientEndColor.setBackgroundColor(item.endColor)

        tvChangeRecordTypeGradientAngle.text = item.angleDegrees.toString()
        // A saved angle may be between steps. Only user input changes the saved value.
        val sliderAngle = (item.angleDegrees.coerceIn(0, 360) / 5f).roundToInt() * 5f
        // Async row binds must not move the thumb while the user is dragging it.
        if (!sliderChangeRecordTypeGradientAngle.isPressed &&
            sliderChangeRecordTypeGradientAngle.value != sliderAngle
        ) {
            sliderChangeRecordTypeGradientAngle.value = sliderAngle
        }

        btnChangeRecordTypeGradientStart.setOnClickListener { onEndpointClick(GradientEndpoint.Start) }
        btnChangeRecordTypeGradientEnd.setOnClickListener { onEndpointClick(GradientEndpoint.End) }
        sliderChangeRecordTypeGradientAngle.addOnChangeListener { _, value, fromUser ->
            if (fromUser) onAngleChange(value.roundToInt())
        }
    }
}

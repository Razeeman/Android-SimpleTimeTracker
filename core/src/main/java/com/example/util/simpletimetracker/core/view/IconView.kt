package com.example.util.simpletimetracker.core.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon
import kotlinx.android.synthetic.main.icon_view_layout.view.ivIconViewImage
import kotlinx.android.synthetic.main.icon_view_layout.view.tvIconViewEmoji

class IconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {

    init {
        View.inflate(context, R.layout.icon_view_layout, this)
        context
            .obtainStyledAttributes(attrs, R.styleable.IconView, defStyleAttr, 0)
            .run {
                if (hasValue(R.styleable.IconView_itemIcon)) itemIcon =
                    getResourceId(R.styleable.IconView_itemIcon, R.drawable.unknown).let(RecordTypeIcon::Image)

                if (hasValue(R.styleable.IconView_itemEmoji)) itemIcon =
                    getString(R.styleable.IconView_itemEmoji).orEmpty().let(RecordTypeIcon::Emoji)

                recycle()
            }
    }

    var itemIcon: RecordTypeIcon = RecordTypeIcon.Image(0)
        set(value) {
            when (value) {
                is RecordTypeIcon.Image -> setImageIcon(value.iconId)
                is RecordTypeIcon.Emoji -> setEmojiIcon(value.emojiText)
            }
            field = value
        }

    var itemIconColor: Int = 0
        set(value) {
            ViewCompat.setBackgroundTintList(ivIconViewImage, ColorStateList.valueOf(value))
            tvIconViewEmoji.setTextColor(value)
            field = value
        }

    private fun setImageIcon(value: Int) {
        ivIconViewImage.setBackgroundResource(value)
        ivIconViewImage.tag = value

        ivIconViewImage.visible = true
        tvIconViewEmoji.visible = false
    }

    private fun setEmojiIcon(value: String) {
        tvIconViewEmoji.text = value

        tvIconViewEmoji.visible = true
        ivIconViewImage.visible = false
    }
}
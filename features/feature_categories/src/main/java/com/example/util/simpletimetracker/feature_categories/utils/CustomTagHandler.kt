package com.example.util.simpletimetracker.feature_categories.utils

import android.graphics.Typeface
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_categories.R
import com.example.util.simpletimetracker.feature_views.TextViewRoundedSpans
import org.xml.sax.XMLReader
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject

class CustomTagHandler @Inject constructor(
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
) : Html.TagHandler {

    var isDarkTheme: Boolean = false

    private val sForegroundColorPattern: Pattern by lazy {
        Pattern.compile("$CUSTOM_COLOR_TAG-(\\d*)")
    }

    override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader?) {
        when {
            tag.contains(CUSTOM_COLOR_TAG) -> if (opening) {
                output.insertSpace()
                output.setSpan(
                    TagMark(getColorId(tag)),
                    output.length,
                    output.length,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE,
                )
            } else {
                val markSpan = getLastMarkSpan(output) ?: return
                val textColor = resourceRepo.getThemedAttr(
                    attrId = R.attr.appLightTextColor,
                    isDarkTheme = isDarkTheme,
                )
                val backgroundColor = colorMapper.mapToColorInt(
                    color = AppColor(colorId = markSpan.colorId, colorInt = "", gradient = null),
                    isDarkTheme = isDarkTheme,
                )
                setSpanFromMark(
                    text = output,
                    mark = markSpan,
                    ForegroundColorSpan(textColor),
                    StyleSpan(Typeface.BOLD),
                    TextViewRoundedSpans.MarkerSpan(backgroundColor),
                )
                output.insertSpace()
            }
        }
    }

    private fun getLastMarkSpan(output: Editable): TagMark? {
        return output.getSpans(
            0,
            output.length,
            TagMark::class.java,
        ).lastOrNull()
    }

    private fun getColorId(tag: String): Int {
        val matcher: Matcher = sForegroundColorPattern.matcher(tag)
        val colorId = if (matcher.find() && matcher.groupCount() > 0) {
            matcher.group(1)?.toIntOrNull().orZero()
        } else {
            0
        }
        return colorId
    }

    private fun setSpanFromMark(text: Spannable, mark: Any, vararg spans: Any) {
        val where = text.getSpanStart(mark)
        text.removeSpan(mark)
        val len = text.length
        if (where != len && where > 0) {
            for (span in spans) {
                text.setSpan(span, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private fun Editable.insertSpace() {
        // Increase drawing space for rounded borders.
        insert(length, " ")
    }

    private class TagMark(val colorId: Int)

    companion object {
        private const val CUSTOM_COLOR_TAG = "custom-color"
    }
}
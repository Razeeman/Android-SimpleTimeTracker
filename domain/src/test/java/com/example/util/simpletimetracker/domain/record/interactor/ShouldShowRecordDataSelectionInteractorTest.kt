package com.example.util.simpletimetracker.domain.record.interactor

import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.base.ResultContainer
import com.example.util.simpletimetracker.domain.complexRule.interactor.ComplexRuleProcessActionInteractor
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordDataSelectionDialogResult
import com.example.util.simpletimetracker.domain.recordTag.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTagValueType
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ShouldShowRecordDataSelectionInteractorTest {

    private val prefsInteractor: PrefsInteractor = mock()
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor = mock()
    private val recordInteractor: RecordInteractor = mock()
    private val recordTagInteractor: RecordTagInteractor = mock()
    private val currentTimestampProvider: CurrentTimestampProvider = mock()
    private val processRulesInteractor: ProcessRulesInteractor = mock()

    private val subject = ShouldShowRecordDataSelectionInteractor(
        prefsInteractor = prefsInteractor,
        getSelectableTagsInteractor = getSelectableTagsInteractor,
        recordInteractor = recordInteractor,
        recordTagInteractor = recordTagInteractor,
        currentTimestampProvider = currentTimestampProvider,
        processRulesInteractor = processRulesInteractor,
    )

    private val currentTimestamp = 1_234_567L

    @Before
    fun setUp(): Unit = runBlocking {
        whenever(currentTimestampProvider.get()).thenReturn(currentTimestamp)
        whenever(prefsInteractor.getRetroactiveTrackingMode()).thenReturn(false)
    }

    @Test
    fun includesTagsCommentAndRequiredNumericValues(): Unit = runBlocking {
        val typeId = 11L
        val rulesResult = ComplexRuleProcessActionInteractor.Result(
            isMultitaskingAllowed = ResultContainer.Undefined,
            disallowOnlyPreviousTypeIds = emptySet(),
            tags = listOf(recordBaseTag(tagId = 100L, numericValue = 2.0)),
            tagIdsToSelectValueOnStart = setOf(1L, 2L),
        )

        whenever(prefsInteractor.getShowRecordTagSelection()).thenReturn(true)
        whenever(prefsInteractor.getRecordTagSelectionExcludeActivities()).thenReturn(emptyList())
        whenever(prefsInteractor.getShowCommentInput()).thenReturn(true)
        whenever(prefsInteractor.getCommentInputExcludeActivities()).thenReturn(emptyList())
        whenever(getSelectableTagsInteractor.execute(typeId)).thenReturn(
            listOf(recordTag(tagId = 5L, valueType = RecordTagValueType.NONE)),
        )
        whenever(recordTagInteractor.getAll()).thenReturn(
            listOf(
                recordTag(tagId = 1L, valueType = RecordTagValueType.NUMERIC),
                recordTag(tagId = 2L, valueType = RecordTagValueType.NONE),
            ),
        )
        whenever(
            processRulesInteractor.getRulesResultForStart(any(), eq(currentTimestamp), any(), eq(false)),
        ).thenReturn(rulesResult)
        val preselectedTags = listOf(recordBaseTag(tagId = 200L, numericValue = null))
        whenever(
            processRulesInteractor.getAllTags(any(), any(), eq(rulesResult.tags)),
        ).thenReturn(preselectedTags)

        val result = subject.execute(typeId = typeId, commentInputAvailable = true)

        assertEquals(
            listOf(
                RecordDataSelectionDialogResult.Field.Tags,
                RecordDataSelectionDialogResult.Field.Comment,
            ),
            result.fields,
        )
        assertEquals(preselectedTags, result.preselectedTags)
        assertEquals(listOf(1L), result.requiredValueSelectionTagIds)
    }

    @Test
    fun forcesTagFieldWhenValueSelectionRequired(): Unit = runBlocking {
        val typeId = 22L
        val rulesResult = ComplexRuleProcessActionInteractor.Result(
            isMultitaskingAllowed = ResultContainer.Undefined,
            disallowOnlyPreviousTypeIds = emptySet(),
            tags = emptyList(),
            tagIdsToSelectValueOnStart = setOf(3L),
        )

        whenever(prefsInteractor.getShowRecordTagSelection()).thenReturn(false)
        whenever(prefsInteractor.getRecordTagSelectionExcludeActivities()).thenReturn(emptyList())
        whenever(prefsInteractor.getShowCommentInput()).thenReturn(false)
        whenever(prefsInteractor.getCommentInputExcludeActivities()).thenReturn(emptyList())
        whenever(recordTagInteractor.getAll()).thenReturn(
            listOf(recordTag(tagId = 3L, valueType = RecordTagValueType.NUMERIC)),
        )
        whenever(
            processRulesInteractor.getRulesResultForStart(any(), eq(currentTimestamp), any(), eq(false)),
        ).thenReturn(rulesResult)
        whenever(
            processRulesInteractor.getAllTags(any(), any(), any()),
        ).thenReturn(emptyList())

        val result = subject.execute(typeId = typeId, commentInputAvailable = true)

        assertEquals(
            listOf(
                RecordDataSelectionDialogResult.Field.Tags,
            ),
            result.fields,
        )
        assertEquals(listOf(3L), result.requiredValueSelectionTagIds)
    }

    private fun recordTag(
        tagId: Long,
        valueType: RecordTagValueType,
        archived: Boolean = false,
    ): RecordTag {
        return RecordTag(
            id = tagId,
            name = "tag-$tagId",
            icon = "icon",
            color = AppColor(colorId = 0, colorInt = "#000000", null),
            iconColorSource = 0,
            note = "",
            archived = archived,
            valueType = valueType,
            valueSuffix = "",
        )
    }

    private fun recordBaseTag(tagId: Long, numericValue: Double?): RecordBase.Tag {
        return RecordBase.Tag(tagId = tagId, numericValue = numericValue)
    }
}

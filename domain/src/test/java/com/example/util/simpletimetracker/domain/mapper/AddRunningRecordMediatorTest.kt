package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.notifications.interactor.ActivityStartedStoppedBroadcastInteractor
import com.example.util.simpletimetracker.domain.record.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.complexRule.interactor.ComplexRuleProcessActionInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationGoalCountInteractor
import com.example.util.simpletimetracker.domain.pomodoro.interactor.PomodoroStartInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToDefaultTagInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.ShouldShowRecordDataSelectionInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordDataSelectionDialogResult
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.base.ResultContainer
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import java.util.concurrent.TimeUnit

class AddRunningRecordMediatorTest {

    private val prefsInteractor: PrefsInteractor = mock()
    private val removeRunningRecordMediator: RemoveRunningRecordMediator = mock()
    private val recordInteractor: RecordInteractor = mock()
    private val runningRecordInteractor: RunningRecordInteractor = mock()
    private val recordTypeInteractor: RecordTypeInteractor = mock()
    private val addRecordMediator: AddRecordMediator = mock()
    private val recordTypeToDefaultTagInteractor: RecordTypeToDefaultTagInteractor = mock()
    private val notificationGoalCountInteractor: NotificationGoalCountInteractor = mock()
    private val activityStartedStoppedBroadcastInteractor: ActivityStartedStoppedBroadcastInteractor = mock()
    private val shouldShowRecordDataSelectionInteractor: ShouldShowRecordDataSelectionInteractor = mock()
    private val pomodoroStartInteractor: PomodoroStartInteractor = mock()
    private val complexRuleProcessActionInteractor: ComplexRuleProcessActionInteractor = mock()
    private val updateExternalViewsInteractor: UpdateExternalViewsInteractor = mock()
    private val currentTimestampProvider: CurrentTimestampProvider = mock()

    private val subject = AddRunningRecordMediator(
        prefsInteractor = prefsInteractor,
        removeRunningRecordMediator = removeRunningRecordMediator,
        recordInteractor = recordInteractor,
        runningRecordInteractor = runningRecordInteractor,
        recordTypeInteractor = recordTypeInteractor,
        addRecordMediator = addRecordMediator,
        recordTypeToDefaultTagInteractor = recordTypeToDefaultTagInteractor,
        notificationGoalCountInteractor = notificationGoalCountInteractor,
        activityStartedStoppedBroadcastInteractor = activityStartedStoppedBroadcastInteractor,
        shouldShowRecordDataSelectionInteractor = shouldShowRecordDataSelectionInteractor,
        pomodoroStartInteractor = pomodoroStartInteractor,
        complexRuleProcessActionInteractor = complexRuleProcessActionInteractor,
        updateExternalViewsInteractor = updateExternalViewsInteractor,
        currentTimestampProvider = currentTimestampProvider,
    ).let(::spy)

    private val currentTime = TimeUnit.HOURS.toMillis(1)
    private val typeId = 1L
    private val typeId2 = 2L
    private val typeId3 = 3L
    private val typeId4 = 4L
    private val type = RecordType(
        id = typeId,
        name = "type_name",
        icon = "type_icon",
        color = AppColor(1, ""),
        defaultDuration = 0,
        note = "",
    )
    private val tagId = 100L
    private val tagId2 = 101L
    private val tagId3 = 103L
    private val runningRecords = listOf(
        RunningRecord(
            id = typeId2,
            timeStarted = 2,
            comment = "comment2",
            tags = listOf(tag(tagId2)),
        ),
        RunningRecord(
            id = typeId3,
            timeStarted = 3,
            comment = "comment3",
            tags = listOf(tag(tagId3)),
        ),
    )

    @Before
    fun before() {
        runBlocking {
            `when`(currentTimestampProvider.get()).thenReturn(currentTime)
            `when`(prefsInteractor.getRetroactiveTrackingMode()).thenReturn(false)
            `when`(prefsInteractor.getAllowMultitasking()).thenReturn(false)
            `when`(complexRuleProcessActionInteractor.hasRules()).thenReturn(false)
            `when`(recordTypeToDefaultTagInteractor.getTags(typeId)).thenReturn(emptySet())
            `when`(recordTypeInteractor.get(typeId)).thenReturn(type)
            `when`(runningRecordInteractor.getAll()).thenReturn(emptyList())
        }
    }

    @Test
    fun tryStartTimerAlreadyTracking(): Unit = runBlocking {
        // Given
        val tagSelectionResult: ((RecordDataSelectionDialogResult) -> Unit) = mock()

        // Already tracking
        `when`(runningRecordInteractor.get(typeId)).thenReturn(runningRecords[0])
        subject.tryStartTimer(
            typeId = typeId,
            updateNotificationSwitch = true,
            commentInputAvailable = true,
            onNeedToShowTagSelection = { tagSelectionResult.invoke(it) },
        )
        verify(shouldShowRecordDataSelectionInteractor, never()).execute(any(), any())
        verify(tagSelectionResult, never()).invoke(any())
    }

    @Test
    fun tryStartTimerDoNotShowTagSelection(): Unit = runBlocking {
        // Given
        val tagSelectionResult: ((RecordDataSelectionDialogResult) -> Unit) = mock()

        `when`(runningRecordInteractor.get(typeId)).thenReturn(null)
        `when`(shouldShowRecordDataSelectionInteractor.execute(any(), any())).thenReturn(
            RecordDataSelectionDialogResult(emptyList()),
        )
        subject.tryStartTimer(
            typeId = typeId,
            updateNotificationSwitch = true,
            commentInputAvailable = true,
            onNeedToShowTagSelection = { tagSelectionResult.invoke(it) },
        )
        verify(shouldShowRecordDataSelectionInteractor).execute(typeId, true)
        verify(tagSelectionResult, never()).invoke(any())
        verify(subject).startTimer(
            typeId = eq(typeId),
            tags = eq(emptyList()),
            comment = eq(""),
            timeStarted = eq(AddRunningRecordMediator.StartTime.TakeCurrent),
            updateNotificationSwitch = eq(true),
            checkDefaultDuration = eq(true),
        )
    }

    @Test
    fun tryStartTimerShowTagSelection(): Unit = runBlocking {
        // Given
        val tagSelectionResult: ((RecordDataSelectionDialogResult) -> Unit) = mock()
        val result = RecordDataSelectionDialogResult(
            listOf(
                RecordDataSelectionDialogResult.Field.Tags,
                RecordDataSelectionDialogResult.Field.Comment,
            ),
        )

        `when`(runningRecordInteractor.get(typeId)).thenReturn(null)
        `when`(shouldShowRecordDataSelectionInteractor.execute(any(), any())).thenReturn(result)
        subject.tryStartTimer(
            typeId = typeId,
            updateNotificationSwitch = true,
            commentInputAvailable = true,
            onNeedToShowTagSelection = { tagSelectionResult.invoke(it) },
        )
        verify(shouldShowRecordDataSelectionInteractor).execute(typeId, true)
        verify(tagSelectionResult).invoke(eq(result))
        verify(subject, never()).startTimer(
            typeId = any(),
            tags = any(),
            comment = any(),
            timeStarted = any(),
            updateNotificationSwitch = any(),
            checkDefaultDuration = any(),
        )
    }

    @Test
    fun tryStartTimerParams(): Unit = runBlocking {
        // Given
        val tagSelectionResult: ((RecordDataSelectionDialogResult) -> Unit) = mock()

        `when`(runningRecordInteractor.get(typeId)).thenReturn(null)
        `when`(shouldShowRecordDataSelectionInteractor.execute(any(), any())).thenReturn(
            RecordDataSelectionDialogResult(emptyList()),
        )
        subject.tryStartTimer(
            typeId = typeId,
            updateNotificationSwitch = false,
            commentInputAvailable = false,
            onNeedToShowTagSelection = { tagSelectionResult.invoke(it) },
        )
        verify(shouldShowRecordDataSelectionInteractor).execute(typeId, false)
        verify(tagSelectionResult, never()).invoke(any())
        verify(subject).startTimer(
            typeId = eq(typeId),
            tags = eq(emptyList()),
            comment = eq(""),
            timeStarted = eq(AddRunningRecordMediator.StartTime.TakeCurrent),
            updateNotificationSwitch = eq(false),
            checkDefaultDuration = eq(true),
        )
    }

    @Test
    fun default(): Unit = runBlocking {
        // When
        subject.startTimer(
            typeId = typeId,
            tags = emptyList(),
            comment = "",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Then
        verify(runningRecordInteractor).add(
            RunningRecord(
                id = typeId,
                timeStarted = currentTime,
                comment = "",
                tags = emptyList(),
            ),
        )
        verify(updateExternalViewsInteractor).onRunningRecordAdd(
            typeId = typeId,
            tagIds = emptyList(),
            updateNotificationSwitch = true,
        )
    }

    @Test
    fun startTimerAdditional() = runBlocking {
        // When
        subject.startTimer(
            typeId = 1,
            tags = listOf(tag(tagId)),
            comment = "comment",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Then
        verify(activityStartedStoppedBroadcastInteractor).onActionActivityStarted(
            typeId = typeId,
            tagIds = listOf(tagId),
            comment = "comment",
        )
        verify(notificationGoalCountInteractor).checkAndShow(typeId)
        verify(pomodoroStartInteractor).checkAndStart(typeId)
    }

    @Test
    fun multitaskingEnabled(): Unit = runBlocking {
        // Given
        `when`(prefsInteractor.getAllowMultitasking()).thenReturn(true)

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId)),
            comment = "comment",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Then
        verify(runningRecordInteractor, never()).getAll()
        verify(removeRunningRecordMediator, never()).removeWithRecordAdd(
            runningRecord = any(),
            updateWidgets = any(),
            updateNotificationSwitch = any(),
            timeEnded = anyOrNull(),
        )
    }

    @Test
    fun multitaskingDisabled(): Unit = runBlocking {
        // Given
        `when`(prefsInteractor.getAllowMultitasking()).thenReturn(false)
        `when`(runningRecordInteractor.getAll()).thenReturn(runningRecords)

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId)),
            comment = "comment",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Then
        verify(runningRecordInteractor).getAll()
        verify(removeRunningRecordMediator).removeWithRecordAdd(
            runningRecord = eq(runningRecords[0]),
            updateWidgets = eq(false),
            updateNotificationSwitch = eq(false),
            timeEnded = eq(currentTime),
        )
        verify(removeRunningRecordMediator).removeWithRecordAdd(
            runningRecord = eq(runningRecords[1]),
            updateWidgets = eq(false),
            updateNotificationSwitch = eq(false),
            timeEnded = eq(currentTime),
        )
    }

    @Test
    fun defaultTag(): Unit = runBlocking {
        // Given
        `when`(recordTypeToDefaultTagInteractor.getTags(typeId)).thenReturn(setOf(tagId2))

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId)),
            comment = "comment",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Then
        verify(runningRecordInteractor).add(
            RunningRecord(
                id = typeId,
                timeStarted = currentTime,
                comment = "comment",
                tags = listOf(tag(tagId), tag(tagId2)),
            ),
        )
    }

    @Test
    fun rulesMultitaskingEnabled(): Unit = runBlocking {
        // Given
        `when`(prefsInteractor.getAllowMultitasking()).thenReturn(false)
        `when`(complexRuleProcessActionInteractor.hasRules()).thenReturn(true)
        `when`(runningRecordInteractor.getAll()).thenReturn(runningRecords)
        `when`(complexRuleProcessActionInteractor.processRules(any(), any(), any())).thenReturn(
            ComplexRuleProcessActionInteractor.Result(
                isMultitaskingAllowed = ResultContainer.Defined(true),
                disallowOnlyPreviousTypeIds = emptySet(),
                tagsIds = emptySet(),
            ),
        )

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId)),
            comment = "comment",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Than
        verify(complexRuleProcessActionInteractor).processRules(
            timeStarted = eq(value = currentTime),
            startingTypeId = eq(typeId),
            currentTypeIds = eq(setOf(typeId2, typeId3)),
        )
        verify(removeRunningRecordMediator, never()).removeWithRecordAdd(
            runningRecord = any(),
            updateWidgets = any(),
            updateNotificationSwitch = any(),
            timeEnded = anyOrNull(),
        )
    }

    @Test
    fun rulesMultitaskingDisabled(): Unit = runBlocking {
        // Given
        `when`(prefsInteractor.getAllowMultitasking()).thenReturn(true)
        `when`(complexRuleProcessActionInteractor.hasRules()).thenReturn(true)
        `when`(runningRecordInteractor.getAll()).thenReturn(runningRecords)
        `when`(complexRuleProcessActionInteractor.processRules(any(), any(), any())).thenReturn(
            ComplexRuleProcessActionInteractor.Result(
                isMultitaskingAllowed = ResultContainer.Defined(false),
                disallowOnlyPreviousTypeIds = emptySet(),
                tagsIds = emptySet(),
            ),
        )

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId)),
            comment = "comment",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Than
        verify(complexRuleProcessActionInteractor).processRules(
            timeStarted = eq(value = currentTime),
            startingTypeId = eq(typeId),
            currentTypeIds = eq(setOf(typeId2, typeId3)),
        )
        verify(removeRunningRecordMediator, times(2)).removeWithRecordAdd(
            runningRecord = any(),
            updateWidgets = any(),
            updateNotificationSwitch = any(),
            timeEnded = anyOrNull(),
        )
    }

    @Test
    fun rulesDisallowOnlyPreviousStopsSubset(): Unit = runBlocking {
        // Given
        `when`(prefsInteractor.getAllowMultitasking()).thenReturn(true)
        `when`(complexRuleProcessActionInteractor.hasRules()).thenReturn(true)
        `when`(runningRecordInteractor.getAll()).thenReturn(runningRecords)
        `when`(complexRuleProcessActionInteractor.processRules(any(), any(), any())).thenReturn(
            ComplexRuleProcessActionInteractor.Result(
                isMultitaskingAllowed = ResultContainer.Defined(false),
                disallowOnlyPreviousTypeIds = setOf(typeId2),
                tagsIds = emptySet(),
            ),
        )

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId)),
            comment = "comment",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Than
        verify(complexRuleProcessActionInteractor).processRules(
            timeStarted = eq(value = currentTime),
            startingTypeId = eq(typeId),
            currentTypeIds = eq(setOf(typeId2, typeId3)),
        )
        verify(removeRunningRecordMediator).removeWithRecordAdd(
            runningRecord = eq(runningRecords[0]),
            updateWidgets = eq(false),
            updateNotificationSwitch = eq(false),
            timeEnded = eq(currentTime),
        )
        verify(removeRunningRecordMediator, never()).removeWithRecordAdd(
            runningRecord = eq(runningRecords[1]),
            updateWidgets = any(),
            updateNotificationSwitch = any(),
            timeEnded = anyOrNull(),
        )
    }

    @Test
    fun rulesTags(): Unit = runBlocking {
        // Given
        `when`(complexRuleProcessActionInteractor.hasRules()).thenReturn(true)
        `when`(runningRecordInteractor.getAll()).thenReturn(runningRecords)
        `when`(complexRuleProcessActionInteractor.processRules(any(), any(), any())).thenReturn(
            ComplexRuleProcessActionInteractor.Result(
                isMultitaskingAllowed = ResultContainer.Undefined,
                disallowOnlyPreviousTypeIds = emptySet(),
                tagsIds = setOf(tagId2),
            ),
        )

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId)),
            comment = "comment",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Than
        verify(complexRuleProcessActionInteractor).processRules(
            timeStarted = eq(value = currentTime),
            startingTypeId = eq(typeId),
            currentTypeIds = eq(setOf(typeId2, typeId3)),
        )
        verify(runningRecordInteractor).add(
            RunningRecord(
                id = typeId,
                timeStarted = currentTime,
                comment = "comment",
                tags = listOf(tag(tagId), tag(tagId2)),
            ),
        )
    }

    @Test
    fun rulesWithPrevRecords(): Unit = runBlocking {
        // Given
        `when`(complexRuleProcessActionInteractor.hasRules()).thenReturn(true)
        `when`(runningRecordInteractor.getAll()).thenReturn(emptyList())
        `when`(recordInteractor.getAllPrev(any())).thenReturn(
            listOf(
                Record(
                    id = 0L,
                    typeId = typeId2,
                    timeStarted = 0,
                    timeEnded = 0,
                    comment = "",
                    tags = emptyList(),
                ),
                Record(
                    id = 0L,
                    typeId = typeId3,
                    timeStarted = 0,
                    timeEnded = 0,
                    comment = "",
                    tags = emptyList(),
                ),
            ),
        )
        `when`(complexRuleProcessActionInteractor.processRules(any(), any(), any())).thenReturn(
            ComplexRuleProcessActionInteractor.Result(
                isMultitaskingAllowed = ResultContainer.Undefined,
                disallowOnlyPreviousTypeIds = emptySet(),
                tagsIds = emptySet(),
            ),
        )

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId)),
            comment = "comment",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Than
        verify(complexRuleProcessActionInteractor).processRules(
            timeStarted = eq(value = currentTime),
            startingTypeId = eq(typeId),
            currentTypeIds = eq(setOf(typeId2, typeId3)),
        )
    }

    @Test
    fun rulesWithPastTimestamp(): Unit = runBlocking {
        // Given
        val timestamp = 1L
        `when`(complexRuleProcessActionInteractor.hasRules()).thenReturn(true)
        `when`(runningRecordInteractor.getAll()).thenReturn(runningRecords)
        `when`(recordInteractor.getAllPrev(any())).thenReturn(
            listOf(
                Record(
                    id = 0L,
                    typeId = typeId2,
                    timeStarted = 0,
                    timeEnded = 0,
                    comment = "",
                    tags = emptyList(),
                ),
                Record(
                    id = 0L,
                    typeId = typeId3,
                    timeStarted = 0,
                    timeEnded = 0,
                    comment = "",
                    tags = emptyList(),
                ),
            ),
        )
        `when`(complexRuleProcessActionInteractor.processRules(any(), any(), any())).thenReturn(
            ComplexRuleProcessActionInteractor.Result(
                isMultitaskingAllowed = ResultContainer.Undefined,
                disallowOnlyPreviousTypeIds = emptySet(),
                tagsIds = emptySet(),
            ),
        )

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId)),
            comment = "comment",
            timeStarted = AddRunningRecordMediator.StartTime.Timestamp(timestamp),
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Than
        verify(complexRuleProcessActionInteractor).processRules(
            timeStarted = eq(timestamp),
            startingTypeId = eq(typeId),
            currentTypeIds = eq(setOf(typeId2, typeId3)),
        )
    }

    @Test
    fun defaultDuration(): Unit = runBlocking {
        // Given
        `when`(recordTypeInteractor.get(typeId)).thenReturn(
            type.copy(defaultDuration = 1),
        )

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId)),
            comment = "comment",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Than
        verify(runningRecordInteractor, never()).add(any())
        verify(addRecordMediator).add(
            record = eq(
                Record(
                    id = 0L,
                    typeId = typeId,
                    timeStarted = currentTime,
                    timeEnded = currentTime + 1000L,
                    comment = "comment",
                    tags = listOf(tag(tagId)),
                ),
            ),
            updateNotificationSwitch = eq(true),
        )
    }

    @Test
    fun defaultDurationWithCheckDisabled(): Unit = runBlocking {
        // Given
        `when`(recordTypeInteractor.get(typeId)).thenReturn(
            type.copy(defaultDuration = 1),
        )

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId)),
            comment = "comment",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = false,
        )

        // Than
        verify(runningRecordInteractor).add(
            RunningRecord(
                id = typeId,
                timeStarted = currentTime,
                comment = "comment",
                tags = listOf(tag(tagId)),
            ),
        )
        verify(addRecordMediator, never()).add(
            record = any(),
            updateNotificationSwitch = any(),
        )
    }

    @Test
    fun retroactiveNoPrev(): Unit = runBlocking {
        // Given
        `when`(prefsInteractor.getRetroactiveTrackingMode()).thenReturn(true)
        `when`(recordInteractor.getAllPrev(any())).thenReturn(emptyList())

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId)),
            comment = "comment",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Then
        verify(runningRecordInteractor, never()).add(any())
        verify(addRecordMediator).add(
            record = eq(
                Record(
                    id = 0L,
                    typeId = typeId,
                    timeStarted = currentTime - TimeUnit.MINUTES.toMillis(5),
                    timeEnded = currentTime,
                    comment = "comment",
                    tags = listOf(tag(tagId)),
                ),
            ),
            updateNotificationSwitch = eq(true),
        )
    }

    @Test
    fun retroactive(): Unit = runBlocking {
        // Given
        `when`(prefsInteractor.getRetroactiveTrackingMode()).thenReturn(true)
        `when`(recordInteractor.getAllPrev(any())).thenReturn(
            listOf(
                Record(
                    id = 10L,
                    typeId = typeId2,
                    timeStarted = 1000,
                    timeEnded = 2000,
                    comment = "",
                    tags = emptyList(),
                ),
            ),
        )

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId)),
            comment = "comment",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Then
        verify(runningRecordInteractor, never()).add(any())
        verify(addRecordMediator).add(
            record = eq(
                Record(
                    id = 0L,
                    typeId = typeId,
                    timeStarted = 2000,
                    timeEnded = currentTime,
                    comment = "comment",
                    tags = listOf(tag(tagId)),
                ),
            ),
            updateNotificationSwitch = eq(true),
        )
    }

    @Test
    fun retroactiveNoMerge(): Unit = runBlocking {
        // Given
        `when`(prefsInteractor.getRetroactiveTrackingMode()).thenReturn(true)
        `when`(recordInteractor.getAllPrev(any())).thenReturn(
            listOf(
                Record(
                    id = 10L,
                    typeId = typeId,
                    timeStarted = 1000,
                    timeEnded = 2000,
                    comment = "comment1",
                    tags = listOf(tag(tagId)),
                ),
            ),
        )

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId2)),
            comment = "comment2",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Then
        verify(runningRecordInteractor, never()).add(any())
        verify(addRecordMediator).add(
            record = eq(
                Record(
                    id = 0L,
                    typeId = typeId,
                    timeStarted = 2000,
                    timeEnded = currentTime,
                    comment = "comment2",
                    tags = listOf(tag(tagId2)),
                ),
            ),
            updateNotificationSwitch = eq(true),
        )
    }

    @Test
    fun retroactiveMergeKeepData(): Unit = runBlocking {
        // Given
        `when`(prefsInteractor.getRetroactiveTrackingMode()).thenReturn(true)
        `when`(recordInteractor.getAllPrev(any())).thenReturn(
            listOf(
                Record(
                    id = 10L,
                    typeId = typeId,
                    timeStarted = 1000,
                    timeEnded = 2000,
                    comment = "comment1",
                    tags = listOf(tag(tagId)),
                ),
            ),
        )

        // When
        subject.startTimer(
            typeId = typeId,
            tags = emptyList(),
            comment = "",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Then
        verify(runningRecordInteractor, never()).add(any())
        verify(addRecordMediator).add(
            record = eq(
                Record(
                    id = 10L,
                    typeId = typeId,
                    timeStarted = 1000,
                    timeEnded = currentTime,
                    comment = "comment1",
                    tags = listOf(tag(tagId)),
                ),
            ),
            updateNotificationSwitch = eq(true),
        )
    }

    @Test
    fun retroactiveDefaultDuration(): Unit = runBlocking {
        // Given
        `when`(prefsInteractor.getRetroactiveTrackingMode()).thenReturn(true)
        `when`(recordInteractor.getAllPrev(any())).thenReturn(
            listOf(
                Record(
                    id = 10L,
                    typeId = typeId,
                    timeStarted = 1000,
                    timeEnded = 2000,
                    comment = "comment1",
                    tags = listOf(tag(tagId)),
                ),
            ),
        )
        `when`(recordTypeInteractor.get(typeId)).thenReturn(
            type.copy(defaultDuration = 1),
        )

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId2)),
            comment = "comment2",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Then
        verify(runningRecordInteractor, never()).add(any())
        verify(addRecordMediator).add(
            record = eq(
                Record(
                    id = 0L,
                    typeId = typeId,
                    timeStarted = currentTime - 1000,
                    timeEnded = currentTime,
                    comment = "comment2",
                    tags = listOf(tag(tagId2)),
                ),
            ),
            updateNotificationSwitch = eq(true),
        )
    }

    @Test
    fun retroactiveMultitasking(): Unit = runBlocking {
        // Given
        `when`(prefsInteractor.getRetroactiveTrackingMode()).thenReturn(true)
        `when`(prefsInteractor.getAllowMultitasking()).thenReturn(true)
        `when`(recordInteractor.getAllPrev(any())).thenReturn(
            listOf(
                Record(
                    id = 10L,
                    typeId = typeId2,
                    timeStarted = 1000,
                    timeEnded = 3000,
                    comment = "comment1",
                    tags = listOf(tag(tagId)),
                ),
                Record(
                    id = 20L,
                    typeId = typeId3,
                    timeStarted = 2000,
                    timeEnded = 3000,
                    comment = "comment2",
                    tags = listOf(tag(tagId2)),
                ),
            ),
        )
        val types = listOf(
            RecordType(typeId2, "", "", AppColor(1, ""), 0, ""),
            RecordType(typeId3, "", "", AppColor(1, ""), 0, ""),
        )
        `when`(recordTypeInteractor.getAll()).thenReturn(types)

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId)),
            comment = "comment",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Then
        verify(runningRecordInteractor, never()).add(any())
        verify(recordInteractor).updateTimeEnded(
            recordId = eq(10L),
            timeEnded = eq(currentTime),
        )
        verify(recordInteractor).updateTimeEnded(
            recordId = eq(20L),
            timeEnded = eq(currentTime),
        )
        verify(addRecordMediator).add(
            record = eq(
                Record(
                    id = 0L,
                    typeId = typeId,
                    timeStarted = 3000,
                    timeEnded = currentTime,
                    comment = "comment",
                    tags = listOf(tag(tagId)),
                ),
            ),
            updateNotificationSwitch = eq(true),
        )
    }

    @Test
    fun retroactiveMultitaskingMerge(): Unit = runBlocking {
        // Given
        `when`(prefsInteractor.getRetroactiveTrackingMode()).thenReturn(true)
        `when`(prefsInteractor.getAllowMultitasking()).thenReturn(true)
        `when`(recordInteractor.getAllPrev(any())).thenReturn(
            listOf(
                Record(
                    id = 5L,
                    typeId = typeId,
                    timeStarted = 500,
                    timeEnded = 3000,
                    comment = "comment",
                    tags = listOf(tag(tagId)),
                ),
                Record(
                    id = 10L,
                    typeId = typeId2,
                    timeStarted = 1000,
                    timeEnded = 3000,
                    comment = "comment1",
                    tags = listOf(tag(tagId)),
                ),
                Record(
                    id = 20L,
                    typeId = typeId3,
                    timeStarted = 2000,
                    timeEnded = 3000,
                    comment = "comment2",
                    tags = listOf(tag(tagId2)),
                ),
                Record(
                    id = 30L,
                    typeId = typeId4,
                    timeStarted = 3000,
                    timeEnded = 3000,
                    comment = "",
                    tags = emptyList(),
                ),
            ),
        )
        val types = listOf(
            RecordType(typeId2, "", "", AppColor(1, ""), 0, ""),
            RecordType(typeId3, "", "", AppColor(1, ""), 0, ""),
            RecordType(typeId4, "", "", AppColor(1, ""), 1, ""),
        )
        `when`(recordTypeInteractor.getAll()).thenReturn(types)

        // When
        subject.startTimer(
            typeId = typeId,
            tags = listOf(tag(tagId)),
            comment = "comment",
            timeStarted = AddRunningRecordMediator.StartTime.TakeCurrent,
            updateNotificationSwitch = true,
            checkDefaultDuration = true,
        )

        // Then
        verify(runningRecordInteractor, never()).add(any())
        verify(recordInteractor, never()).updateTimeEnded(
            recordId = eq(5L),
            timeEnded = any(),
        )
        verify(recordInteractor).updateTimeEnded(
            recordId = eq(10L),
            timeEnded = eq(currentTime),
        )
        verify(recordInteractor).updateTimeEnded(
            recordId = eq(20L),
            timeEnded = eq(currentTime),
        )
        verify(recordInteractor, never()).updateTimeEnded(
            recordId = eq(30L),
            timeEnded = any(),
        )
        verify(addRecordMediator).add(
            record = eq(
                Record(
                    id = 5L,
                    typeId = typeId,
                    timeStarted = 500,
                    timeEnded = currentTime,
                    comment = "comment",
                    tags = listOf(tag(tagId)),
                ),
            ),
            updateNotificationSwitch = eq(true),
        )
    }

    private fun tag(id: Long): RecordBase.Tag {
        return RecordBase.Tag(id, null)
    }
}
package com.example.util.simpletimetracker.feature_pomodoro.timer.view

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.feature_pomodoro.timer.model.PomodoroButtonState
import com.example.util.simpletimetracker.feature_pomodoro.timer.model.PomodoroTimerState
import com.example.util.simpletimetracker.feature_pomodoro.timer.viewModel.PomodoroViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_pomodoro.databinding.PomodoroFragmentBinding as Binding

@AndroidEntryPoint
class PomodoroFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: PomodoroViewModel by viewModels()

    private var timerUpdateAnimator: ValueAnimator? = null

    override fun initUx() = with(binding) {
        btnPomodoroSettings.setOnClick(viewModel::onSettingsClicked)
        btnPomodoroStart.setOnClick(viewModel::onStartStopClicked)
        btnPomodoroRestart.setOnClick(viewModel::onRestartClicked)
        btnPomodoroNext.setOnClick(viewModel::onNextClicked)
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            buttonState.observe(::setButtonState)
            timerState.observe(::setTimerState)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onHidden()
    }

    private fun setButtonState(state: PomodoroButtonState) = with(binding) {
        ivPomodoroButton.setImageResource(state.iconResId)
        btnPomodoroRestart.visible = state.additionalButtonsVisible
        btnPomodoroNext.visible = state.additionalButtonsVisible
    }

    private fun setTimerState(state: PomodoroTimerState) = with(binding) {
        groupPomodoroTimerHours.visible = state.durationState.hoursIsVisible
        tvPomodoroTimerHours.text = state.durationState.textHours
        tvPomodoroTimerMinutes.text = state.durationState.textMinutes
        tvPomodoroTimerSeconds.text = state.durationState.textSeconds
        tvPomodoroCycleHint.text = state.currentCycleHint

        viewPomodoroTimer.max = state.maxProgress
        val currentProgress = viewPomodoroTimer.progress
        val updateDuration = state.timerUpdateMs
        val from = if (state.progress < currentProgress) 0 else currentProgress
        val to = state.progress
        timerUpdateAnimator?.cancel()
        timerUpdateAnimator = ValueAnimator.ofInt(from, to).apply {
            this.interpolator = LinearInterpolator()
            this.duration = updateDuration
            addUpdateListener {
                val value = it.animatedValue as? Int ?: return@addUpdateListener
                viewPomodoroTimer.progress = value
            }
            start()
        }
    }
}

package com.example.util.simpletimetracker.core.delegates.dateSelector.viewDelegate

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.databinding.DateSelectorLayoutBinding
import com.example.util.simpletimetracker.core.delegates.dateSelector.viewData.DateSelectorButtonsViewData
import com.example.util.simpletimetracker.core.delegates.dateSelector.viewModelDelegate.DateSelectorViewModelDelegate
import com.example.util.simpletimetracker.core.extension.changeDragSensitivity
import com.example.util.simpletimetracker.core.extension.horizontalSmoothScrollWithOffset
import com.example.util.simpletimetracker.feature_base_adapter.InfiniteRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.dateSelector.createDateSelectorDayAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.dateSelector.createDateSelectorRangeAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.dateSelector.createDateSelectorSingleAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.addOnScrollListenerAdapter
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.google.android.material.button.MaterialButton

object DateSelectorViewDelegate {

    interface ViewHolder {
        val adapter: Lazy<InfiniteRecyclerAdapter>
        val snapHelper: LinearSnapHelper
    }

    fun getViewHolder(
        viewModel: DateSelectorViewModelDelegate,
    ): ViewHolder {
        return object : ViewHolder {
            override val adapter: Lazy<InfiniteRecyclerAdapter> = lazy { getAdapter(viewModel) }
            override val snapHelper: LinearSnapHelper = LinearSnapHelper()
        }
    }

    fun initUi(
        fragment: Fragment,
        viewHolder: ViewHolder,
        viewModel: DateSelectorViewModelDelegate,
        binding: DateSelectorLayoutBinding,
    ) {
        fun onDatesScrolled(recyclerView: RecyclerView, newState: Int) {
            onDatesScrolled(
                viewHolder = viewHolder,
                recyclerView = recyclerView,
                newState = newState,
                onScrolledToDate = viewModel::onScrolledToDate,
            )
        }

        binding.rvDatesContainer.apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = RecyclerView.HORIZONTAL
            }
            adapter = viewHolder.adapter.value
            viewHolder.snapHelper.attachToRecyclerView(this)
            setTouchInterceptListener { onDateSelectorTouchIntercepted(fragment, it) }
            addOnScrollListenerAdapter(onScrollStateChanged = ::onDatesScrolled)
            changeDragSensitivity(0.1f)
        }
    }

    fun <T : ViewBinding> initUx(
        fragment: BaseFragment<T>,
        binding: DateSelectorLayoutBinding,
        onRecordAddClick: () -> Unit = {},
        onOptionsClick: () -> Unit = {},
        onOptionsLongClick: () -> Unit = {},
    ) = with(fragment) {
        binding.btnRecordsContainerAdd.setOnClick(throttle(onRecordAddClick))
        binding.btnRecordsContainerOptions.setOnClick(throttle(onOptionsClick))
        binding.btnRecordsContainerOptions.setOnLongClick(throttle(onOptionsLongClick))
    }

    fun <T : ViewBinding> initViewModel(
        fragment: BaseFragment<T>,
        viewHolder: ViewHolder,
        viewModel: DateSelectorViewModelDelegate,
        binding: DateSelectorLayoutBinding,
    ) = with(fragment) {
        viewModel.dateScrollPosition.observe { data ->
            doScrollToPosition(
                viewHolder = viewHolder,
                binding = binding,
                shift = data.position,
                animate = data.animate,
            )
        }
        viewModel.updateDatesViewData.observe {
            updateDatesSelector(
                viewHolder = viewHolder,
            )
        }
        viewModel.borderShadowsVisibility.observe { isVisible ->
            binding.viewRecordsContainerBorderShadowStart.isVisible = isVisible
            binding.viewRecordsContainerBorderShadowEnd.isVisible = isVisible
        }
        viewModel.buttonsViewData.observe {
            setButtonsViewData(
                binding = binding,
                viewData = it,
            )
        }
    }

    private fun getAdapter(
        viewModel: DateSelectorViewModelDelegate,
    ): InfiniteRecyclerAdapter {
        return InfiniteRecyclerAdapter(
            dataProvider = viewModel.dataProvider,
            createDateSelectorDayAdapterDelegate(
                onItemClick = viewModel::onDateClick,
                onItemLongClick = viewModel::onDateLongClick,
            ),
            createDateSelectorRangeAdapterDelegate(
                onItemClick = viewModel::onDateClick,
                onItemLongClick = viewModel::onDateLongClick,
            ),
            createDateSelectorSingleAdapterDelegate(
                onItemClick = viewModel::onDateClick,
            ),
        )
    }

    private fun onDatesScrolled(
        viewHolder: ViewHolder,
        recyclerView: RecyclerView,
        newState: Int,
        onScrolledToDate: (position: Int) -> Unit,
    ) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
            val snapView = viewHolder.snapHelper.findSnapView(layoutManager) ?: return
            val snapPosition = layoutManager?.getPosition(snapView) ?: return
            onScrolledToDate(viewHolder.adapter.value.toShift(snapPosition))
        }
    }

    private fun onDateSelectorTouchIntercepted(
        fragment: Fragment,
        event: MotionEvent,
    ) = with(fragment) {
        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            -> blockParentScroll(true)
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL,
            -> blockParentScroll(false)
        }
    }

    private fun Fragment.blockParentScroll(isBlocked: Boolean) {
        val views = (parentFragment?.view as? ViewGroup)?.children
        val viewPager = views?.filterIsInstance<ViewPager2>()?.firstOrNull()
        viewPager?.isUserInputEnabled = !isBlocked
    }

    private fun doScrollToPosition(
        viewHolder: ViewHolder,
        binding: DateSelectorLayoutBinding,
        shift: Int,
        animate: Boolean,
    ) = with(binding) {
        val actualPosition = viewHolder.adapter.value.toPosition(shift)
        val layoutManager = rvDatesContainer.layoutManager as? LinearLayoutManager ?: return@with

        if (animate) {
            // To long to scroll with animation if scroll distance is long,
            // in this case scrollToPosition closer, and than smoothScroll with animation.
            rvDatesContainer.scrollToPosition(actualPosition)

            rvDatesContainer.post {
                rvDatesContainer.horizontalSmoothScrollWithOffset(
                    snapPreference = LinearSmoothScroller.SNAP_TO_END,
                    position = actualPosition,
                    calculateOffset = { recyclerView, view ->
                        // center of the recycler.
                        -recyclerView.width / 2 + view.width / 2
                    },
                )
            }
        } else {
            rvDatesContainer.post {
                val view = rvDatesContainer.children.firstOrNull() ?: return@post
                val offset = rvDatesContainer.width / 2 - view.width / 2
                layoutManager.scrollToPositionWithOffset(actualPosition, offset)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateDatesSelector(
        viewHolder: ViewHolder,
    ) {
        // TODO do better maybe?
        viewHolder.adapter.value.notifyDataSetChanged()
    }

    private fun setButtonsViewData(
        binding: DateSelectorLayoutBinding,
        viewData: DateSelectorButtonsViewData,
    ) = with(binding) {
        fun setData(
            view: MaterialButton,
            data: DateSelectorButtonsViewData.Button,
        ) {
            when (data) {
                is DateSelectorButtonsViewData.Button.Visible -> {
                    view.isVisible = true
                    view.setIconResource(data.iconResId)
                }
                is DateSelectorButtonsViewData.Button.Hidden -> {
                    view.isVisible = false
                }
            }
        }

        setData(btnRecordsContainerAdd, viewData.addButton)
        setData(btnRecordsContainerOptions, viewData.optionsButton)
    }
}
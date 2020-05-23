package com.example.util.simpletimetracker.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    @get:LayoutRes
    protected abstract val layoutId: Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(layoutId, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDi()
        initUi()
        initUx()
        initViewModel()
    }

    open fun initDi() {
        // Override in subclasses
    }

    open fun initUi() {
        // Override in subclasses
    }

    open fun initUx() {
        // Override in subclasses
    }

    open fun initViewModel() {
        // Override in subclasses
    }
}
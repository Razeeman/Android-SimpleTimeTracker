package com.example.util.simpletimetracker.core.base

import androidx.lifecycle.LiveData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    inline fun <T> LiveData<T>.observe(
        crossinline onChanged: (T) -> Unit
    ) {
        observe(viewLifecycleOwner, { onChanged(it) })
    }
}
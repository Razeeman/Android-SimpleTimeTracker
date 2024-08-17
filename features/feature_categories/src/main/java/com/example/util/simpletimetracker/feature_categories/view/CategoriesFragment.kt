package com.example.util.simpletimetracker.feature_categories.view

import com.example.util.simpletimetracker.feature_categories.databinding.CategoriesFragmentBinding as Binding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAddAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_categories.viewModel.CategoriesViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoriesFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override val insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.rvCategoriesList }

    private val viewModel: CategoriesViewModel by viewModels()

    private val categoriesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createDividerAdapterDelegate(),
            createHintAdapterDelegate(),
            createCategoryAdapterDelegate(onClickWithTransition = throttle(viewModel::onCategoryClick)),
            createCategoryAddAdapterDelegate(throttle(viewModel::onAddCategoryClick)),
        )
    }

    override fun initUi(): Unit = with(binding) {
        postponeEnterTransition()

        rvCategoriesList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = categoriesAdapter
            setHasFixedSize(true)
        }

        setOnPreDrawListener {
            startPostponedEnterTransition()
        }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        categories.observe {
            categoriesAdapter.replace(it.items)
            binding.tvCategoriesEditHint.isVisible = it.showHint
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }
}

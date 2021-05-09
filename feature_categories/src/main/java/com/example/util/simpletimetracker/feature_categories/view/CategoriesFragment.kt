package com.example.util.simpletimetracker.feature_categories.view

import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.feature_categories.R
import com.example.util.simpletimetracker.feature_categories.adapter.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_categories.adapter.createCategoryAddAdapterDelegate
import com.example.util.simpletimetracker.feature_categories.adapter.createCategoryDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_categories.di.CategoriesComponentProvider
import com.example.util.simpletimetracker.feature_categories.viewModel.CategoriesViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.categories_fragment.*
import javax.inject.Inject

class CategoriesFragment : BaseFragment(R.layout.categories_fragment) {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<CategoriesViewModel>

    private val viewModel: CategoriesViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val categoriesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
            createCategoryAddAdapterDelegate(viewModel::onAddCategoryClick),
            createCategoryDividerAdapterDelegate(),
            createHintAdapterDelegate()
        )
    }

    override fun initDi() {
        (activity?.application as CategoriesComponentProvider)
            .categoriesComponent
            ?.inject(this)
    }

    override fun initUi() {
        parentFragment?.postponeEnterTransition()

        rvCategoriesList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = categoriesAdapter
            setHasFixedSize(true)

            viewTreeObserver.addOnPreDrawListener {
                parentFragment?.startPostponedEnterTransition()
                true
            }
        }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        categories.observe(viewLifecycleOwner, categoriesAdapter::replace)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    companion object {
        fun newInstance() = CategoriesFragment()
    }
}

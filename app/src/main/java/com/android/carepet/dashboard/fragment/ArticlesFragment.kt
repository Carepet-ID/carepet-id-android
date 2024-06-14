package com.android.carepet.dashboard.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.carepet.dashboard.adapter.ArticleAdapter
import com.android.carepet.dashboard.viewmodel.ArticleViewModel
import com.android.carepet.dashboard.viewmodel.ArticleViewModelFactory
import com.android.carepet.databinding.FragmentArticlesBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArticlesFragment : Fragment() {

    private lateinit var binding: FragmentArticlesBinding
    private val viewModel: ArticleViewModel by viewModels {
        ArticleViewModelFactory(requireContext())
    }
    private val articleAdapter by lazy { ArticleAdapter(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArticlesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = articleAdapter
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        lifecycleScope.launch {
            viewModel.articles.collectLatest { pagingData ->
                articleAdapter.submitData(pagingData)
            }
        }
    }
}
package uz.ravshanbaxranov.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import uz.ravshanbaxranov.distancetracker.R
import uz.ravshanbaxranov.distancetracker.databinding.FragmentHistoryBinding
import uz.ravshanbaxranov.presentation.adapter.HistoryAdapter
import uz.ravshanbaxranov.presentation.viewmodel.HistoryViewModel


@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.fragment_history) {

    private val viewModel: HistoryViewModel by viewModels()
    private val binding by viewBinding(FragmentHistoryBinding::bind)
    private val adapter by lazy { HistoryAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        binding.historyRv.adapter = adapter
        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        adapter.setOnCLickListener { data, image ->
            val extras = FragmentNavigatorExtras(image to "shared_element")
            findNavController().navigate(
                HistoryFragmentDirections.actionHistoryFragmentToRunDetailFragment(data),
                extras
            )
        }

        lifecycleScope.launchWhenCreated {
            viewModel.historyListFlow.collect {
                binding.noDataTv.isVisible = it.isEmpty()
                adapter.submitList(it)
            }
        }
    }

}
package uz.ravshanbaxranov.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import uz.ravshanbaxranov.distancetracker.R
import uz.ravshanbaxranov.distancetracker.databinding.VerifyBottomSheetBinding
import uz.ravshanbaxranov.presentation.event.SplashEvent
import uz.ravshanbaxranov.presentation.viewmodel.SplashViewModel
import uz.ravshanbaxranov.presentation.viewmodel.UiEvent


@AndroidEntryPoint
class SplashFragment : Fragment(R.layout.fragment_splash) {
    private val viewModel: SplashViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launchWhenCreated {
            viewModel.eventFlow.collect {
                when (it) {
                    is UiEvent.Toast -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    is UiEvent.NavigateHome -> {
                        findNavController().navigate(SplashFragmentDirections.actionPermissionFragmentToHomeFragment())
                    }
                    is UiEvent.OpenBottomSheet -> {
                        openBottomSheet()
                    }
                }
            }
        }
    }


    private fun openBottomSheet() {
        val dialog = BottomSheetDialog(this.requireContext())
        val binding = VerifyBottomSheetBinding.inflate(LayoutInflater.from(requireContext()))
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.show()

        binding.saveButton.setOnClickListener {
            dialog.dismiss()
            viewModel.setNameAndWeight(
                SplashEvent.SetNameAndWeight(
                    name = binding.nameEt.text.toString(),
                    weight = binding.weightEt.text.toString()
                )
            )
        }

    }


}




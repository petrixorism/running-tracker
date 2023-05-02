package uz.ravshanbaxranov.presentation.ui

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import uz.ravshanbaxranov.distancetracker.R
import uz.ravshanbaxranov.distancetracker.databinding.FragmentRunDetailBinding
import uz.ravshanbaxranov.presentation.viewmodel.RunDetailViewModel
import uz.ravshanbaxranov.util.decimalFormat
import java.util.Date

@AndroidEntryPoint
class RunDetailFragment : Fragment(R.layout.fragment_run_detail) {

    private val binding by viewBinding(FragmentRunDetailBinding::bind)
    private val args by navArgs<RunDetailFragmentArgs>()
    private val viewModel: RunDetailViewModel by viewModels()

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        binding.mapIv.setImageBitmap(args.run.image)

        val animation = TransitionInflater.from(requireContext()).inflateTransition(
            android.R.transition.move
        )
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation

        args.run.apply {
            binding.distanceTv.text = "$distanceInMeters meters"
            binding.timeTv.text = SimpleDateFormat("HH:mm dd-MMMM yyyy").format(Date(date))
            binding.durationTv.text = decimalFormat(time / 60000.0f) + " minutes"
            binding.calorieTv.text = "$caloriesBurned kcal"
            binding.paceTv.text = decimalFormat(averageSpeed) + "km/h"
        }
        lifecycleScope.launchWhenCreated {
            animateTextViews()
        }
        lifecycleScope.launchWhenCreated {
            viewModel.deletedFlow.collect {
                findNavController().navigateUp()
            }
        }
        binding.deleteFba.setOnClickListener {
            confirmDeleteDialog()
        }
        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

    }

    private fun confirmDeleteDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Do you want delete this track?")
        alertDialog.setCancelable(false)
        alertDialog.setNegativeButton(
            "Cancel"
        ) { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.setPositiveButton("Confirm") { dialog, _ ->
            dialog.dismiss()
            findNavController().navigateUp()
            viewModel.deleteRunDetail(args.run)
        }
        alertDialog.show()
    }

    private suspend fun animateTextViews() {

        binding.detailsCl.children.forEach { view ->
            view.animate().alpha(1f).start()
            delay(100L)
        }
    }


}

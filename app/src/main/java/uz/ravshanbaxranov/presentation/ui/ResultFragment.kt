package uz.ravshanbaxranov.presentation.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import uz.ravshanbaxranov.distancetracker.R
import uz.ravshanbaxranov.distancetracker.databinding.FragmentResultBinding
import uz.ravshanbaxranov.util.shareResult

class ResultFragment : BottomSheetDialogFragment(R.layout.fragment_result) {

    private val binding by viewBinding(FragmentResultBinding::bind)
    private val args by navArgs<ResultFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.distanceTv.text = args.distance
        binding.timeTv.text = args.time
        binding.speedTv.text = args.speed

        binding.shareBtn.setOnClickListener {
            shareResult(requireContext(), args.time, args.distance)
        }

        binding.root.setBackgroundColor(Color.TRANSPARENT)

    }

}
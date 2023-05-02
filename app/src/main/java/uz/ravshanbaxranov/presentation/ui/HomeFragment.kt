package uz.ravshanbaxranov.presentation.ui


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Gravity.RIGHT
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.NumberPicker
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import uz.ravshanbaxranov.data.model.ChartType
import uz.ravshanbaxranov.data.service.TrackerService
import uz.ravshanbaxranov.distancetracker.R
import uz.ravshanbaxranov.distancetracker.databinding.DialogLocatoinPermissionBinding
import uz.ravshanbaxranov.distancetracker.databinding.FragmentHomeBinding
import uz.ravshanbaxranov.distancetracker.databinding.VerifyBottomSheetBinding
import uz.ravshanbaxranov.presentation.viewmodel.HomeViewModel
import uz.ravshanbaxranov.util.Constants.PRIVACY_POLICY
import uz.ravshanbaxranov.util.Permissions
import java.text.DecimalFormat
import java.util.*


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home),
    EasyPermissions.PermissionCallbacks, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()


    @SuppressLint("SetTextI18n", "RtlHardcoded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)

        binding.navView.setNavigationItemSelectedListener(this)

        if (!Permissions.hasLocationPermission(requireContext())) {
            openPermissionDialog()
        }
        lifecycleScope.launchWhenCreated {
            viewModel.chartData.collect { chartList ->
                setLineChartData(chartList)
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.nameFlow.collect {
                binding.nameTv.text = it
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.totalDataFlow.collect {
                binding.totalDistanceTv.text = it.distance
                binding.totalSpeedTv.text = it.avgSpeed
                binding.totalDurationTv.text = it.duration
                binding.totalCaloriesTv.text = it.calories
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.goalFlow.collect {
                Log.d("TAGDF", it.toString())
                binding.goalProgressLp.max = it.goal
                binding.goalDoneTv.text = DecimalFormat("##.##").format(it.done) + " km"

                if (it.goal - it.done > 0) {
                    binding.goalProgressLp.progress = (it.done).toInt()
                    binding.goalLeftTv.text =
                        DecimalFormat("##.##").format(it.goal - it.done) + " km"
                } else {
                    binding.goalLeftTv.text = "Done ✅"
                    binding.goalProgressLp.progress = it.goal
                }
                binding.weekGoalTv.text = "${it.goal}km"
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.toastFlow.collect {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }


        TrackerService.currentJoggingFlow.asStateFlow().onEach {
            if (it.timer > 0) {
                withContext(Dispatchers.Main) {
                    binding.currentDistanceTv.text =
                        DecimalFormat("##.##").format(it.distance / 1000.0f) + " km"
                    binding.currentTimerTv.base = SystemClock.elapsedRealtime() - it.timer
                }
            }
        }.launchIn(lifecycleScope)



        viewModel.getTotalData()
        binding.startBtn.setOnClickListener {
            if (!Permissions.hasLocationPermission(requireContext())) {
                openPermissionDialog()
            } else {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToMapsFragment())
            }
        }
        binding.drawerBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(RIGHT)
//            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHistoryFragment())
        }

        binding.toHistoryTv.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHistoryFragment())
        }
        binding.chartMoreBtn.setOnClickListener {
            showChartPopUp(it)
        }
        binding.goalMoreBtn.setOnClickListener {
            openDialog()
        }
        binding.activityChart.description = Description().apply {
            text = "Meters of each run"
        }
        binding.tvStat.setOnClickListener {
            showTotalChartPopUp(it)
        }
        binding.closeBtn.setOnClickListener {
            binding.drawerLayout.closeDrawer(RIGHT)
        }


    }

    private fun setLineChartData(chartType: ChartType) {


        val lineDataset = LineDataSet(chartType.lineValues, "").apply {
            setDrawFilled(true)
            setCircleColor(requireContext().getColor(chartType.chartColor))
            valueTextSize = 10f
            formSize = 20f
            fillDrawable = ContextCompat.getDrawable(requireContext(), chartType.chartDrawable)
            circleRadius = 4f
            color = requireContext().getColor(chartType.chartColor)
            lineWidth = 3f
            valueTextColor = Color.BLACK
            mode = LineDataSet.Mode.CUBIC_BEZIER
            valueTextSize
            valueTextColor = ContextCompat.getColor(requireContext(), chartType.chartColor)
        }


        val lineData = LineData(lineDataset)
        //We connect our data to the UI Screen
        binding.activityChart.apply {
            setBorderColor(requireContext().getColor(R.color.purple))
            setBackgroundColor(Color.TRANSPARENT)
            animateXY(1000, 1000, Easing.EaseInCubic)

            axisRight.setDrawGridLines(false)
            xAxis.setDrawGridLines(false)
            legend.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            data = lineData

            description.isEnabled = true

        }


    }

    private fun showChartPopUp(button: View) {
        val popup = PopupMenu(requireContext(), button)
        popup.inflate(R.menu.chart_menu)
        popup.show()
        popup.setOnMenuItemClickListener {
            binding.activityChart.description = Description().apply {
                text = "${it.title} of each run"
            }
            viewModel.chartType(it.title.toString())
            return@setOnMenuItemClickListener false
        }
    }

    private fun showTotalChartPopUp(button: View) {
        val popup = PopupMenu(requireContext(), button)
        popup.inflate(R.menu.chart_duration_menu)
        popup.show()
        popup.setOnMenuItemClickListener {
            binding.tvStat.text = it.title
            viewModel.selectChartDuration(it.title.toString())
            return@setOnMenuItemClickListener false
        }
    }

    private fun openDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_set_goal)

        val numberPicker = dialog.findViewById<NumberPicker>(R.id.goal_picker)
        val saveBtn = dialog.findViewById<Button>(R.id.save_btn)


        numberPicker.minValue = 5
        numberPicker.maxValue = 1000

        saveBtn.setOnClickListener {
            viewModel.changeWeeklyGoal(numberPicker.value)
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, listOf(perms[0]))) {
            SettingsDialog.Builder(requireContext()).build().show()
        } else {
            Permissions.requestLocationAndNotificationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }


    private fun openPermissionDialog() {


        val dialog = Dialog(requireContext())
        val dialogBinding = DialogLocatoinPermissionBinding.inflate(layoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        dialogBinding.allowBtn.setOnClickListener {
            Permissions.requestLocationAndNotificationPermission(this)
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.history -> {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHistoryFragment())
            }

            R.id.settings -> {
                openBottomSheet()
            }

            R.id.privacy_policy -> {
                requireActivity().startActivity(Intent(ACTION_VIEW, Uri.parse(PRIVACY_POLICY)))
            }
        }
        binding.drawerLayout.closeDrawer(RIGHT)
        return true
    }

    private fun openBottomSheet() {
        val dialog = BottomSheetDialog(this.requireContext())
        val binding = VerifyBottomSheetBinding.inflate(LayoutInflater.from(requireContext()))
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.show()

        binding.saveButton.setOnClickListener {
            dialog.dismiss()
            viewModel.setNameAndWeight(
                name = binding.nameEt.text.toString(),
                weight = binding.weightEt.text.toString()
            )
        }

    }


}
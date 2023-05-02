package uz.ravshanbaxranov.presentation.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.graphics.*
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uz.ravshanbaxranov.data.service.TrackerService
import uz.ravshanbaxranov.data.service.TrackerService.Companion.isServiceRunning
import uz.ravshanbaxranov.data.service.TrackerService.Companion.startTime
import uz.ravshanbaxranov.distancetracker.R
import uz.ravshanbaxranov.distancetracker.databinding.FragmentMapsBinding
import uz.ravshanbaxranov.presentation.viewmodel.MapViewModel
import uz.ravshanbaxranov.util.Constants.ACTION_SERVICE_START
import uz.ravshanbaxranov.util.Constants.ACTION_SERVICE_STOP
import uz.ravshanbaxranov.util.Constants.REQUEST_CHECK_SETTINGS
import uz.ravshanbaxranov.util.MapsUtil
import uz.ravshanbaxranov.util.Permissions.hasBackgroundLocationPermission
import uz.ravshanbaxranov.util.Permissions.requestBackgroundLocationPermission
import uz.ravshanbaxranov.util.decimalFormat
import java.text.DecimalFormat
import java.util.*

@AndroidEntryPoint
class MapsFragment : Fragment(R.layout.fragment_maps), OnMapReadyCallback,
    OnMyLocationButtonClickListener,
    EasyPermissions.PermissionCallbacks {


    private lateinit var map: GoogleMap
    private val mapUtil by lazy { MapsUtil() }
    private var locations: MutableList<LatLng> = mutableListOf()
    private var polyline: Polyline? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val viewModel: MapViewModel by viewModels()
    private lateinit var binding: FragmentMapsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentMapsBinding.bind(view)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.startBtn.setOnClickListener {
            if (hasBackgroundLocationPermission(requireContext())) {
                startCountDownTimer()
            } else {
                requestBackgroundLocationPermission(this)
            }
        }

        binding.stopBtn.setOnClickListener {
            sendServiceAction(ACTION_SERVICE_STOP)
        }

        binding.backBtn.setOnClickListener {
            findNavController().navigate(MapsFragmentDirections.actionMapsFragmentToHomeFragment())
        }

        binding.mapStyles.setOnClickListener {
            mapStylePopUp(it)
        }

        binding.resetBtn.setOnClickListener {
            drawPolyLines(emptyList())
            it.isVisible = false
            binding.startBtn.isVisible = true
            binding.timeTv.base = SystemClock.elapsedRealtime() - 0
            binding.paceTv.text = "0"
            binding.distanceTv.text = "0"
            binding.calorieTv.text = "0"
        }


    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {


        map = googleMap
        map.isMyLocationEnabled = true
        map.uiSettings.apply {
            isZoomControlsEnabled = false
            isRotateGesturesEnabled = false
            isCompassEnabled = false
        }
        map.setPadding(0, binding.topView.height, 0, binding.bottomView.height)

        polyline = map.addPolyline(PolylineOptions().apply {
            geodesic(true)
            color(
                ContextCompat.getColor(
                    requireContext(), R.color.purple_400
                )
            )
            width(20f)
            startCap(RoundCap())
        })
        map.setOnMyLocationButtonClickListener(this)
        checkGPS()
        collectors()
        initialViews()
    }

    @SuppressLint("SetTextI18n", "ResourceType")
    private fun collectors() {
        TrackerService.locationListChannel.receiveAsFlow().onEach { pointsList ->
            locations = pointsList.toMutableList()

            if (pointsList.isNotEmpty()) {
                binding.loadingPb.isVisible = false
                binding.stopBtn.isEnabled = true
            }

            drawPolyLines(pointsList)
        }.launchIn(lifecycleScope)


        TrackerService.stopServiceChannel.receiveAsFlow().onEach { stopTime ->
            if (stopTime > 0) {
                map.uiSettings.apply {
                    isScrollGesturesEnabled = false
                    isRotateGesturesEnabled = false
                }
                mapUtil.showBiggerMap(locations = locations, map = map)
                binding.stopBtn.isVisible = false
                binding.resetBtn.isVisible = true
                binding.resetBtn.isEnabled = false

                delay(1500)

                map.snapshot { bitmap ->
                    viewModel.insertRun(
                        stopTime - startTime,
                        mapUtil.calculatePolylineLength(locations),
                        cropBitmap(bitmap)
                    )
                    Toast.makeText(
                        requireContext(),
                        "Your track saved to history",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.resetBtn.isEnabled = true

                map.uiSettings.apply {
                    isScrollGesturesEnabled = true
                    isRotateGesturesEnabled = true
                }
            }
        }.launchIn(lifecycleScope)

        lifecycleScope.launchWhenCreated {
            TrackerService.currentJoggingFlow.collect {

                val pace = (it.distance / 1000.0f) / (it.timer / 1000.0f / 60 / 60)

                if (it.accuracy == 0.0f) {
                    binding.gpsAccuracy.setImageResource(R.drawable.gps_status_no)
                } else if (it.accuracy < 10) {
                    binding.gpsAccuracy.setImageResource(R.drawable.gps_status_strong)
                } else if (it.accuracy < 500) {
                    binding.gpsAccuracy.setImageResource(R.drawable.gps_status_medium)
                } else {
                    binding.gpsAccuracy.setImageResource(R.drawable.gps_status_low)
                }

                if (pace.isFinite() || it.distance > 0) {
                    binding.distanceTv.text = decimalFormat(it.distance / 1000.0f)
                    binding.calorieTv.text = "${it.calorie}"
                    binding.timeTv.base = SystemClock.elapsedRealtime() - it.timer
                    binding.paceTv.text = DecimalFormat("##.#").format(pace)
                }
            }
        }


        lifecycleScope.launchWhenCreated {
            viewModel.mapStyleFlow.collect {
                val theme = mapUtil.changeMapStyle(it)
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), theme.style))
                binding.mapStyles.setImageResource(theme.icon)
                binding.titleTv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        theme.textColor
                    )
                )
            }
        }

    }


    private fun startCountDownTimer() {
        binding.startBtn.isVisible = false
        binding.stopBtn.isVisible = true
        binding.loadingPb.isVisible = true

        val timer = object : CountDownTimer(4000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {

                val currentSecond = millisUntilFinished / 1000
                if (currentSecond.toString() == "0") {
                    binding.timerTv.text = "GO!"
                    binding.timerTv.setTextColor(Color.BLACK)
                } else {
                    binding.timerTv.text = currentSecond.toString()
                    binding.timerTv.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red
                        )
                    )
                }

            }

            override fun onFinish() {
                binding.timerTv.animate().alpha(0f).duration = 500
                binding.timerTv.isVisible = false
                sendServiceAction(ACTION_SERVICE_START)
            }

        }
        timer.start()

    }


    private fun sendServiceAction(action: String) {
        Intent(requireContext(), TrackerService::class.java).apply {
            this.action = action
            requireContext().startService(this)
        }
    }


    private fun checkGPS() {

        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(LocationRequest.create())
            .setAlwaysShow(true)
            .build()


        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        exception.startResolutionForResult(
                            requireActivity(),
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error
                    }
                }
            }
    }

    private fun mapStylePopUp(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.inflate(R.menu.map_style_menu)
        popup.show()
        popup.setOnMenuItemClickListener {
            viewModel.changeMapStyle(it.title.toString())
            return@setOnMenuItemClickListener false
        }
    }


    private fun drawPolyLines(points: List<LatLng>) {
        if (points.isNotEmpty()) {
            val camera: CameraPosition = CameraPosition.Builder()
                .bearing(0f)
                .target(points.last())
                .zoom(18f)
                .build()

            map.animateCamera(CameraUpdateFactory.newCameraPosition(camera))

            polyline?.points = points
        } else {
            polyline?.points = points
        }
    }

    @SuppressLint("MissingPermission")
    private fun initialViews() {

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        if (isServiceRunning) {
            binding.startBtn.isVisible = false
            binding.stopBtn.isVisible = true
        }
    }

    override fun onMyLocationButtonClick(): Boolean {

        if (!isServiceRunning) {
            lifecycleScope.launch {
                delay(1500)
                binding.startBtn.isEnabled = true
            }
        }

        return false
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.permissionPermanentlyDenied(this, perms.first())) {
            SettingsDialog.Builder(requireContext()).build().show()
        } else {
            requestBackgroundLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        startCountDownTimer()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun cropBitmap(bitmap: Bitmap?): Bitmap {

        val cropRect = Rect(
            0,
            binding.topView.height,
            binding.map.width,
            binding.map.height - binding.bottomView.height
        )

        // Create a new bitmap that contains the cropped image
        val croppedBitmap =
            Bitmap.createBitmap(cropRect.width(), cropRect.height(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(croppedBitmap)
        canvas.drawBitmap(
            bitmap!!,
            cropRect,
            Rect(0, 0, cropRect.width(), cropRect.height()),
            null
        )

        return croppedBitmap

    }

}
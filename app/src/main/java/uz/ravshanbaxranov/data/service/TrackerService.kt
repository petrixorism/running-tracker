package uz.ravshanbaxranov.data.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import uz.ravshanbaxranov.data.model.CurrentJoggingData
import uz.ravshanbaxranov.util.Constants.ACTION_SERVICE_START
import uz.ravshanbaxranov.util.Constants.ACTION_SERVICE_STOP
import uz.ravshanbaxranov.util.Constants.NOTIFICATION_CHANNEL_ID
import uz.ravshanbaxranov.util.Constants.NOTIFICATION_CHANNEL_NAME
import uz.ravshanbaxranov.util.Constants.NOTIFICATION_ID
import uz.ravshanbaxranov.util.MapsUtil
import uz.ravshanbaxranov.util.PreferencesKeys.WEIGHT
import javax.inject.Inject


@AndroidEntryPoint
class TrackerService : LifecycleService() {

    @Inject
    lateinit var notification: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var latLongList = arrayListOf<LatLng>()
    private val mapsUtil = MapsUtil()
    private var weight = 0
    private var currentRunData = CurrentJoggingData()

    companion object {
        var startTime: Long = 0L
        var isServiceRunning = false
        val locationListChannel = Channel<List<LatLng>>()
        val stopServiceChannel = Channel<Long>()
        val currentJoggingFlow = MutableStateFlow(CurrentJoggingData())
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)


            result.lastLocation.let {

                if (it?.hasAccuracy() == true) {
                    updateNotification()
                    latLongList.add(LatLng(it.latitude, it.longitude))
                    lifecycleScope.launchWhenCreated {
                        locationListChannel.send(latLongList.toList())
                    }

                    currentRunData = CurrentJoggingData(
                        accuracy = it.accuracy
                    )

                }


            }
        }
    }

    private fun updateNotification() {

        notification.apply {
            setContentTitle("Distance in meters")
            setContentText(mapsUtil.calculatePolylineLength(latLongList).toString() + " meters")
        }
        notificationManager.notify(NOTIFICATION_ID, notification.build())

    }


    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        lifecycleScope.launchWhenCreated {
            dataStore.data.collect { data ->
                weight = data[WEIGHT] ?: 0
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_SERVICE_START -> {
                    lifecycleScope.launch {
                        latLongList.clear()
                        startForegroundService()
                        startCurrentJoggingData()
                    }

                }
                ACTION_SERVICE_STOP -> {
                    lifecycleScope.launch {
                        stopServiceChannel.send(System.currentTimeMillis())
                        stopForegroundService()
                    }

                }
                else -> {

                }

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopForegroundService() {
        isServiceRunning = false
        removeLocationUpdates()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
            NOTIFICATION_ID
        )
        currentJoggingFlow.value = CurrentJoggingData(0, 0, 0)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun startCurrentJoggingData() {
        lifecycleScope.launch {
            while (isServiceRunning) {
                val timeInMills = System.currentTimeMillis() - startTime
                val distance = mapsUtil.calculatePolylineLength(latLongList)
                val calorie = mapsUtil.calculateCaloriesBurned(distance, weight)
                val avgSpeed = mapsUtil.calculateAvgSpeed(latLongList, startTime)

                currentJoggingFlow.emit(
                    currentRunData.copy(
                        distance = distance,
                        timer = timeInMills,
                        speed = avgSpeed,
                        calorie = calorie
                    )
                )
                delay(1000L)
            }
        }
    }

    private fun startForegroundService() {
        createNotificationManagerChannel()
        startForeground(NOTIFICATION_ID, notification.build())
        startUpdateLocation()
        startTime = System.currentTimeMillis()
        isServiceRunning = true
    }

    @SuppressLint("MissingPermission")
    private fun startUpdateLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .setWaitForAccurateLocation(true)
            .build()

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }


    private fun createNotificationManagerChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }


}
package uz.ravshanbaxranov.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Gravity.RIGHT
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import uz.ravshanbaxranov.distancetracker.R
import uz.ravshanbaxranov.distancetracker.databinding.ActivityMainBinding
import uz.ravshanbaxranov.util.Constants.ACTION_SHOW_TRACKING_FRAGMENT


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        if (intent.getBooleanExtra("notification", false)) {
            navController.navigate(R.id.toMapFragment)
        }



    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            binding.navHostFragment.findNavController().navigate(R.id.toTrackingFragment)
        }
    }
}
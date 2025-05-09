package com.example.myroadr.activities

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myroadr.R
import com.example.myroadr.fragments.FavorisFragment
import com.example.myroadr.fragments.HomeFragment
import com.example.myroadr.fragments.MapsFragment
import com.example.myroadr.fragments.ProfileFragment
import me.ibrahimsn.lib.SmoothBottomBar

class MainActivity : AppCompatActivity() {

    private lateinit var bottomBar: SmoothBottomBar
    private val LOCATION_PERMISSION_CODE = 1001

    private val gpsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

                if (isGpsEnabled) {
                    Toast.makeText(this@MainActivity, "GPS activé", Toast.LENGTH_SHORT).show()
                    reloadCurrentFragment()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(gpsReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gpsReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        bottomBar = findViewById(R.id.bottomBar)

        checkLocationPermissionsAndServices()

        loadFragment(HomeFragment())

        bottomBar.setOnItemSelectedListener { index ->
            checkLocationPermissionsAndServices()
            val fragment: Fragment = when (index) {
                0 -> HomeFragment()
                1 -> MapsFragment()
                2 -> FavorisFragment()
                3 -> ProfileFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame, fragment)
            .commit()
    }

    private fun reloadCurrentFragment() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frame)
        currentFragment?.let {
            supportFragmentManager.beginTransaction()
                .detach(it)
                .attach(it)
                .commit()
        }
    }

    private fun checkLocationPermissionsAndServices() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!isGPSEnabled) {
            showGPSDialog()
        }

        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showGPSDialog() {
        AlertDialog.Builder(this)
            .setTitle("Activer la localisation")
            .setMessage("La localisation est désactivée. Veuillez l'activer pour continuer.")
            .setPositiveButton("Activer") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}

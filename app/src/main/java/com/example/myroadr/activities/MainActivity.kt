package com.example.myroadr.activities

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myroadr.R
import com.example.myroadr.fragments.FavorisFragment
import com.example.myroadr.fragments.HomeFragment
import com.example.myroadr.fragments.MapsFragment
import com.example.myroadr.fragments.ProfileFragment
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
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
        dbRef = FirebaseDatabase.getInstance().getReference("Events")

        createNotificationChannel()
        monitorNewEvents()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }
        FirebaseMessaging.getInstance().subscribeToTopic("allUsers")

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
    /////////notifiction settings
    private lateinit var dbRef: DatabaseReference
    private val lastSnapshotKeys = mutableSetOf<String>() // <-- this is what was missing
    private var lastEventCount = 0
    private val channelId = "events_channel"

    private fun listenToNewEvents() {
        dbRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val eventId = snapshot.key ?: return

                if (!lastSnapshotKeys.contains(eventId)) {
                    lastSnapshotKeys.add(eventId)
                    val eventTitle = snapshot.child("title").getValue(String::class.java) ?: "New Event"
                    showNotification("🚴 Event Added", "$eventTitle is now available")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun monitorNewEvents() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentCount = snapshot.childrenCount.toInt()

                if (lastEventCount != 0 && currentCount > lastEventCount) {
                    showNotification("🚴 New cycling event added!", "Check it out on the map!")
                }

                lastEventCount = currentCount
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showNotification(title: String, content: String) {
        val builder = NotificationCompat.Builder(this, "events_channel")
            .setSmallIcon(R.drawable.icon_app) // Make sure this icon exists!
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "events_channel",
                "Cycling Events",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notifies when a new cycling event is added"
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

}

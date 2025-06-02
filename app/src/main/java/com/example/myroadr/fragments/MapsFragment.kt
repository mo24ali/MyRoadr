package com.example.myroadr.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.myroadr.R
import com.example.myroadr.activities.ShowDetailsEvent
import com.example.myroadr.databinding.InfoCardBinding
import com.example.myroadr.models.CyclingEvent
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private lateinit var binding: InfoCardBinding
    private lateinit var searchEditText: EditText

    private var userLocation: Location? = null
    private var currentEvent: CyclingEvent? = null
    private var currentEventId: String? = null
    private var currentEventDistance: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.fmap) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding = InfoCardBinding.bind(view.findViewById(R.id.infoCard))
        searchEditText = view.findViewById(R.id.searchCityEditText)

        searchEditText.setOnEditorActionListener { _, _, _ ->
            val cityQuery = searchEditText.text.toString().trim()
            if (cityQuery.isNotEmpty()) {
                filterEventsByCity(cityQuery)
            }
            true
        }

        binding.hideCard.setOnClickListener {
            if (currentEvent != null && currentEventId != null && currentEventDistance != null) {
                val intent = Intent(requireContext(), ShowDetailsEvent::class.java)
                intent.putExtra("title", currentEvent!!.title)
                intent.putExtra("description", currentEvent!!.description)
                intent.putExtra("distance", currentEventDistance)
                intent.putExtra("id", currentEventId)
                intent.putExtra("imageResId", R.drawable.bike_haibike)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Données non disponibles", Toast.LENGTH_SHORT).show()
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        database = FirebaseDatabase.getInstance().reference
        checkGPSStatus()
        updateUserLocation()
    }

    private fun checkGPSStatus() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder(requireContext())
                .setTitle("Localisation désactivée")
                .setMessage("Le GPS est désactivé. Voulez-vous l'activer ?")
                .setPositiveButton("Oui") { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                .setNegativeButton("Non") { dialog, _ -> dialog.dismiss() }
                .create().show()
        }
    }

    private fun updateUserLocation() {
        val hasFine = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        if (hasFine != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                userLocation = location
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    if (::googleMap.isInitialized) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        googleMap.addMarker(MarkerOptions().position(latLng).title("Moi").icon(BitmapDescriptorFactory.fromResource(R.drawable.newmarker)))
                        googleMap.isMyLocationEnabled = true
                    }
                    showEventMarkers()
                }
            }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        try {
            val success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
            if (!success) Log.e("MAP_STYLE", "Erreur parsing style")
        } catch (e: Resources.NotFoundException) {
            Log.e("MAP_STYLE", "Fichier style introuvable", e)
        }

        googleMap.setOnMarkerClickListener { marker ->
            showEventDetails(marker.title ?: "")
            true
        }
    }

    private fun showEventMarkers() {
        database.child("Events").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val event = child.getValue(CyclingEvent::class.java)
                    event?.let {
                        val marker = googleMap.addMarker(
                            MarkerOptions()
                                .position(LatLng(it.latitude, it.longitude))
                                .title(it.title)
                                .snippet(it.description)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.newmarker))
                        )
                        marker?.tag = child.key
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Erreur chargement événements", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterEventsByCity(cityName: String) {
        val loadingDialog = AlertDialog.Builder(requireContext(), R.style.LoadingDialogTheme)
            .setView(LayoutInflater.from(requireContext()).inflate(R.layout.search_dialog, null))
            .setCancelable(false).create()
        loadingDialog.show()

        googleMap.clear()
        database.child("Events").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var found = false
                var firstLatLng: LatLng? = null
                for (child in snapshot.children) {
                    val event = child.getValue(CyclingEvent::class.java)
                    event?.let {
                        if (it.locationName.contains(cityName, ignoreCase = true)) {
                            found = true
                            val latLng = LatLng(it.latitude, it.longitude)
                            if (firstLatLng == null) firstLatLng = latLng
                            val marker = googleMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title(it.title)
                                    .snippet(it.description)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.newmarker))
                            )
                            marker?.tag = child.key
                        }
                    }
                }
                loadingDialog.dismiss()
                if (found && firstLatLng != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 15f))
                } else {
                    Toast.makeText(requireContext(), "Aucun événement trouvé à \"$cityName\"", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                loadingDialog.dismiss()
                Toast.makeText(requireContext(), "Erreur filtrage", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showEventDetails(title: String) {
        database.child("Events").orderByChild("title").equalTo(title)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val event = child.getValue(CyclingEvent::class.java)
                        event?.let {
                            currentEvent = it
                            currentEventId = child.key

                            // 🔥 Appeler la fonction pour calculer la distance avec l'eventId
                            if (currentEventId != null) {
                                calculateDistanceForEvent(currentEventId!!)
                            }

                            binding.root.visibility = View.VISIBLE
                            binding.titleTv.text = it.title
                            binding.titleTvdesc.text = it.description
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Erreur chargement info", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun calculateDistanceForEvent(eventId: String) {
        database.child("Events").child(eventId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val event = snapshot.getValue(CyclingEvent::class.java)
                    event?.let {
                        val eventLocation = Location("").apply {
                            latitude = it.latitude
                            longitude = it.longitude
                        }

                        if (userLocation != null) {
                            val distanceInMeters = userLocation!!.distanceTo(eventLocation)
                            val distanceInKm = distanceInMeters / 1000
                            currentEventDistance = String.format("%.2f km", distanceInKm)
                        } else {
                            currentEventDistance = "Inconnue"
                        }

                        // Optionnel : Mettre à jour un champ visuel pour la distance
                        // binding.distanceText.text = currentEventDistance
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Erreur calcul distance", Toast.LENGTH_SHORT).show()
                }
            })
    }




    private val gpsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(context, "GPS activé", Toast.LENGTH_SHORT).show()
                    updateUserLocation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(gpsReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(gpsReceiver)
    }
}
